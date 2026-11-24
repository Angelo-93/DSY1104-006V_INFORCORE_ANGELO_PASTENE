package cl.inforcore.backend.dto;

import cl.inforcore.backend.model.Categoria;

/**
 * Lo que la API entrega de una categoría. cantidadProductos sirve a la tienda
 * (mostrar cuántos hay) y al panel (desactivar "Eliminar" si tiene productos).
 */
public record CategoriaDTO(
        String codigo,
        String nombre,
        String descripcion,
        long cantidadProductos) {

    public static CategoriaDTO desde(Categoria categoria, long cantidadProductos) {
        return new CategoriaDTO(categoria.getCodigo(), categoria.getNombre(), categoria.getDescripcion(), cantidadProductos);
    }
}
