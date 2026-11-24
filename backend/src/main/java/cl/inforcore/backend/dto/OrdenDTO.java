package cl.inforcore.backend.dto;

import cl.inforcore.backend.model.DetalleOrden;
import cl.inforcore.backend.model.EstadoOrden;
import cl.inforcore.backend.model.Orden;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Lo que la API entrega de una orden: todo lo que muestra la boleta. Tiene la
 * misma forma que las órdenes de la EP2 (numero, cliente, direccion, items,
 * total), así el frontend cambia lo mínimo al pasar de localStorage a la API.
 *
 * Los records internos (Cliente, Direccion, Item) agrupan los datos como en la
 * boleta. Son records anidados: viven aquí porque solo tienen sentido dentro
 * de una orden.
 */
public record OrdenDTO(
        Long numero,
        OffsetDateTime fecha,
        EstadoOrden estado,
        String motivoRechazo,
        Cliente cliente,
        Direccion direccion,
        List<Item> items,
        Integer total) {

    /** Datos del cliente tal como quedaron en la boleta; run es el de la cuenta que compró. */
    public record Cliente(String run, String nombre, String apellidos, String correo) {
    }

    public record Direccion(String calle, String departamento, String region, String comuna, String indicaciones) {
    }

    /** Una línea de la boleta: precio y nombre son los COPIADOS al momento de la compra. */
    public record Item(String codigo, String codigoCategoria, String nombre, Integer precioUnitario,
                       Integer cantidad, Integer subtotal) {

        static Item desde(DetalleOrden detalle) {
            return new Item(
                    detalle.getProducto().getCodigo(),
                    detalle.getProducto().getCategoria().getCodigo(),
                    detalle.getNombreProducto(),
                    detalle.getPrecioUnitario(),
                    detalle.getCantidad(),
                    detalle.getSubtotal());
        }
    }

    public static OrdenDTO desde(Orden orden) {
        return new OrdenDTO(
                orden.getId(),
                orden.getFecha(),
                orden.getEstado(),
                orden.getMotivoRechazo(),
                new Cliente(orden.getUsuario().getRun(), orden.getNombreCliente(),
                        orden.getApellidosCliente(), orden.getCorreoCliente()),
                new Direccion(orden.getCalle(), orden.getDepartamento(),
                        orden.getComuna().getRegion().getNombre(), orden.getComuna().getNombre(),
                        orden.getIndicaciones()),
                orden.getItems().stream().map(Item::desde).toList(),
                orden.getTotal());
    }
}
