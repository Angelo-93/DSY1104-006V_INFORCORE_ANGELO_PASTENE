package cl.inforcore.backend.dto;

import cl.inforcore.backend.model.Producto;

/**
 * Lo que la API ENTREGA de un producto.
 *
 * Es un record (clase del 10-11): inmutable, sin setters. No se envía la
 * entidad Producto directamente para controlar exactamente qué datos salen.
 * Además de los datos guardados, trae tres datos calculados por el backend
 * (precioVigente, enOferta y critico): así el frontend no repite las reglas
 * de negocio, solo las muestra.
 */
public record ProductoDTO(
        String codigo,
        String nombre,
        String codigoCategoria,
        String nombreCategoria,
        String especificaciones,
        String descripcion,
        Integer precio,
        Integer precioOferta,
        Integer precioVigente,
        boolean enOferta,
        Integer stock,
        Integer stockCritico,
        boolean critico) {

    /** Convierte la entidad (lo que hay en la base) en el DTO (lo que viaja al frontend). */
    public static ProductoDTO desde(Producto producto) {
        return new ProductoDTO(
                producto.getCodigo(),
                producto.getNombre(),
                producto.getCategoria().getCodigo(),
                producto.getCategoria().getNombre(),
                producto.getEspecificaciones(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getPrecioOferta(),
                producto.getPrecioVigente(),
                producto.estaEnOferta(),
                producto.getStock(),
                producto.getStockCritico(),
                producto.esCritico());
    }
}
