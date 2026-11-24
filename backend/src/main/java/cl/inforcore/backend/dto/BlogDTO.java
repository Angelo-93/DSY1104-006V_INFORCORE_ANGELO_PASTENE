package cl.inforcore.backend.dto;

import cl.inforcore.backend.model.Blog;

import java.util.Arrays;
import java.util.List;

/**
 * Artículo del blog. En la base los párrafos están en una sola columna,
 * separados por una línea en blanco; aquí se entregan como lista, igual que
 * el arreglo "parrafos" de la EP2.
 */
public record BlogDTO(
        String codigo,
        String etiqueta,
        String titulo,
        String resumen,
        String categoriaIlustracion,
        List<String> parrafos) {

    public static BlogDTO desde(Blog blog) {
        return new BlogDTO(blog.getCodigo(), blog.getEtiqueta(), blog.getTitulo(), blog.getResumen(),
                blog.getCategoriaIlustracion(), Arrays.asList(blog.getContenido().split("\n\n")));
    }
}
