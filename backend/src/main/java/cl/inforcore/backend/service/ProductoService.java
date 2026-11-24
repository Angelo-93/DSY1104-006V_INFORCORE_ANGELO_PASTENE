package cl.inforcore.backend.service;

import cl.inforcore.backend.dto.ProductoDTO;
import cl.inforcore.backend.dto.ProductoRequest;
import cl.inforcore.backend.exception.ConflictoException;
import cl.inforcore.backend.exception.DatosInvalidosException;
import cl.inforcore.backend.exception.RecursoNoEncontradoException;
import cl.inforcore.backend.model.Categoria;
import cl.inforcore.backend.model.Producto;
import cl.inforcore.backend.repository.CategoriaRepository;
import cl.inforcore.backend.repository.DetalleOrdenRepository;
import cl.inforcore.backend.repository.ProductoRepository;
import cl.inforcore.backend.util.Textos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Lógica de negocio de los productos. El controlador solo recibe y responde;
 * las reglas viven aquí (clase del 26-10: el Service es la capa de la lógica de
 * negocio) y cada método entrega DTO, nunca entidades.
 *
 * Reglas (de la EP2, ahora protegidas por el servidor):
 * - El código se guarda en mayúsculas, no se repite y no se modifica.
 * - El precio de oferta, si existe, es menor que el precio.
 * - La categoría debe existir.
 * - Nuevo en la EP3: no se elimina un producto que ya tiene ventas.
 */
@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private DetalleOrdenRepository detalleOrdenRepository;

    /**
     * Sin filtros devuelve todo el catálogo. Con "categoria" devuelve los de esa
     * categoría; si no, con "buscar" busca el texto en el nombre o el código.
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO> listar(String buscar, String codigoCategoria) {
        List<Producto> productos;
        if (codigoCategoria != null && !codigoCategoria.isBlank()) {
            productos = productoRepository.findByCategoriaCodigoOrderByIdAsc(codigoCategoria.trim());
        } else if (buscar != null && !buscar.isBlank()) {
            String texto = buscar.trim();
            productos = productoRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCaseOrderByIdAsc(texto, texto);
        } else {
            productos = productoRepository.findAllByOrderByIdAsc();
        }
        return productos.stream().map(ProductoDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtener(String codigo) {
        return ProductoDTO.desde(buscar(codigo));
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarOfertas() {
        return productoRepository.findByPrecioOfertaIsNotNullOrderByIdAsc().stream()
                .filter(Producto::estaEnOferta)
                .map(ProductoDTO::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarCriticos() {
        return productoRepository.findCriticos().stream().map(ProductoDTO::desde).toList();
    }

    @Transactional
    public ProductoDTO crear(ProductoRequest datos) {
        String codigo = datos.codigo().trim().toUpperCase(Locale.ROOT);
        if (productoRepository.existsByCodigo(codigo)) {
            throw new ConflictoException("codigo", "Ya existe un producto con el código " + codigo + ".");
        }
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        aplicarDatos(producto, datos);
        return ProductoDTO.desde(productoRepository.save(producto));
    }

    /**
     * El código no se puede cambiar (lo usan las URL, el carrito y las boletas):
     * se toma el de la URL y se ignora el que venga en el cuerpo.
     */
    @Transactional
    public ProductoDTO actualizar(String codigo, ProductoRequest datos) {
        Producto producto = buscar(codigo);
        aplicarDatos(producto, datos);
        return ProductoDTO.desde(productoRepository.save(producto));
    }

    /**
     * Un producto con ventas no se elimina: sus boletas apuntan a él con una FK y
     * deben conservarse. Si ya no se vende, basta con dejar su stock en 0.
     */
    @Transactional
    public void eliminar(String codigo) {
        Producto producto = buscar(codigo);
        if (detalleOrdenRepository.existsByProducto(producto)) {
            throw new ConflictoException("No se puede eliminar: el producto tiene ventas registradas. "
                    + "Si ya no se vende, deja su stock en 0.");
        }
        productoRepository.delete(producto);
    }

    /** Copia los datos del formulario al producto, revisando las reglas que cruzan datos. */
    private void aplicarDatos(Producto producto, ProductoRequest datos) {
        // Regla que compara DOS campos: no se puede expresar con una anotación de un solo campo.
        if (datos.precioOferta() != null && datos.precioOferta() >= datos.precio()) {
            throw new DatosInvalidosException("precioOferta", "El precio de oferta debe ser menor que el precio normal.");
        }
        Categoria categoria = categoriaRepository.findByCodigo(datos.codigoCategoria().trim())
                .orElseThrow(() -> new DatosInvalidosException("codigoCategoria", "La categoría seleccionada no existe."));

        producto.setNombre(datos.nombre().trim());
        producto.setCategoria(categoria);
        producto.setEspecificaciones(Textos.limpiarOpcional(datos.especificaciones()));
        producto.setDescripcion(Textos.limpiarOpcional(datos.descripcion()));
        producto.setPrecio(datos.precio());
        producto.setPrecioOferta(datos.precioOferta());
        producto.setStock(datos.stock());
        producto.setStockCritico(datos.stockCritico());
    }

    private Producto buscar(String codigo) {
        return productoRepository.findByCodigo(codigo.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto " + codigo + "."));
    }
}
