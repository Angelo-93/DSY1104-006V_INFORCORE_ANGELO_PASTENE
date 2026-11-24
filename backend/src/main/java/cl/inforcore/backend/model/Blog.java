package cl.inforcore.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Artículo del blog. En la EP2 estaba escrito en el frontend (src/data/blogs.js);
 * el profe pidió que no quedaran listas "en duro", así que ahora viene de la API.
 */
@Entity
@Table(name = "inforcore_blogs")
@Getter
@Setter
@NoArgsConstructor
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código legible para la URL (/blogs/renovar-equipos). */
    @Column(nullable = false, unique = true, length = 60)
    private String codigo;

    /** Texto corto sobre el título (ej: "Caso curioso #1"). */
    @Column(nullable = false, length = 50)
    private String etiqueta;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 300)
    private String resumen;

    /**
     * Código de la categoría cuyo ícono ilustra el artículo. Es solo una
     * referencia visual, no una relación de negocio, por eso es texto y no FK:
     * un artículo nunca impide eliminar o renombrar una categoría.
     */
    @Column(length = 60)
    private String categoriaIlustracion;

    /** Párrafos separados por una línea en blanco; la API los entrega como lista. */
    @Column(nullable = false, length = 5000)
    private String contenido;
}
