package cl.inforcore.backend.service;

import cl.inforcore.backend.dto.NuevaOrdenRequest;
import cl.inforcore.backend.dto.OrdenDTO;
import cl.inforcore.backend.exception.DatosInvalidosException;
import cl.inforcore.backend.exception.RecursoNoEncontradoException;
import cl.inforcore.backend.model.*;
import cl.inforcore.backend.repository.OrdenRepository;
import cl.inforcore.backend.repository.ProductoRepository;
import cl.inforcore.backend.repository.UsuarioRepository;
import cl.inforcore.backend.util.Textos;
import cl.inforcore.backend.util.Validaciones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Órdenes de compra (boletas). Se crean al pagar el carrito y después solo se
 * consultan: no hay editar ni eliminar a propósito, porque una boleta emitida es
 * un registro contable (igual que en la EP2).
 */
@Service
public class OrdenService {

    private static final ZoneId ZONA_CHILE = ZoneId.of("America/Santiago");

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RegionService regionService;

    /** Todas las órdenes, las más recientes primero (vista Órdenes del panel). */
    @Transactional(readOnly = true)
    public List<OrdenDTO> listar() {
        return ordenRepository.findAllByOrderByFechaDesc().stream().map(OrdenDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public OrdenDTO obtener(Long numero) {
        return ordenRepository.findById(numero)
                .map(OrdenDTO::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la orden N° " + numero + "."));
    }

    /** Historial de compras de un usuario. */
    @Transactional(readOnly = true)
    public List<OrdenDTO> listarDeUsuario(String run) {
        Usuario usuario = usuarioRepository.findByRun(run.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con RUN " + run + "."));
        return ordenRepository.findByUsuarioOrderByFechaDesc(usuario).stream().map(OrdenDTO::desde).toList();
    }

    /**
     * Procesa el pago del carrito. Toda la compra es una sola transacción: o se
     * guarda la orden Y se descuenta el stock, o no pasa nada.
     *
     * El pago se rechaza por una causa REAL (algún producto ya no tiene stock
     * suficiente) o SIMULADA (el selector del checkout). La orden rechazada
     * también se guarda: el panel la muestra y el cliente puede reintentar.
     *
     * @param correoComprador Correo de quien compra. En la parte de seguridad lo
     *                        entrega el token JWT; aquí el servicio solo lo usa.
     */
    @Transactional
    public OrdenDTO crear(String correoComprador, NuevaOrdenRequest datos) {
        Usuario comprador = usuarioRepository.findByCorreoIgnoreCase(correoComprador)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + correoComprador + "."));

        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);
        if (!Validaciones.correoTieneDominioPermitido(correo)) {
            throw new DatosInvalidosException("correo", Validaciones.MENSAJE_DOMINIO);
        }
        Comuna comuna = regionService.buscarComuna(datos.region(), datos.comuna());

        // 1. Junta líneas repetidas del mismo producto (por si el carrito trae dos veces el mismo código).
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (NuevaOrdenRequest.Item item : datos.items()) {
            cantidades.merge(item.codigo().trim().toUpperCase(Locale.ROOT), item.cantidad(), Integer::sum);
        }

        // 2. Arma las líneas con el precio y el nombre del catálogo de HOY, no con
        //    lo que diga el carrito, y revisa el stock en este momento.
        Orden orden = new Orden();
        List<String> faltantes = new ArrayList<>();
        cantidades.forEach((codigo, cantidad) -> {
            Producto producto = productoRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new DatosInvalidosException("items",
                            "El producto " + codigo + " ya no existe en el catálogo. Quítalo del carrito."));
            if (cantidad > producto.getStock()) {
                faltantes.add(producto.getNombre() + " (pediste " + cantidad + ", quedan " + producto.getStock() + ")");
            }
            orden.agregarItem(new DetalleOrden(producto, producto.getPrecioVigente(), cantidad));
        });

        // 3. Resultado del pago.
        String motivoRechazo = null;
        if (!faltantes.isEmpty()) {
            motivoRechazo = "No hay stock suficiente: " + String.join("; ", faltantes)
                    + ". Ajusta tu carrito e inténtalo de nuevo.";
        } else if (datos.simularRechazo()) {
            motivoRechazo = "El medio de pago rechazó la transacción (rechazo simulado para la demostración).";
        }

        // 4. Solo una compra pagada descuenta stock. Basta con cambiar el objeto:
        //    al confirmar la transacción, Hibernate guarda el nuevo stock.
        if (motivoRechazo == null) {
            orden.getItems().forEach(item ->
                    item.getProducto().setStock(item.getProducto().getStock() - item.getCantidad()));
        }

        orden.setEstado(motivoRechazo == null ? EstadoOrden.PAGADA : EstadoOrden.RECHAZADA);
        orden.setMotivoRechazo(motivoRechazo);
        orden.setFecha(OffsetDateTime.now(ZONA_CHILE));
        orden.setUsuario(comprador);
        orden.setNombreCliente(datos.nombre().trim());
        orden.setApellidosCliente(datos.apellidos().trim());
        orden.setCorreoCliente(correo);
        orden.setCalle(datos.calle().trim());
        orden.setDepartamento(Textos.limpiarOpcional(datos.departamento()));
        orden.setComuna(comuna);
        orden.setIndicaciones(Textos.limpiarOpcional(datos.indicaciones()));
        // El total lo calcula el servidor, nunca se acepta desde el navegador.
        orden.setTotal(orden.getItems().stream().mapToInt(DetalleOrden::getSubtotal).sum());

        return OrdenDTO.desde(ordenRepository.save(orden));
    }
}
