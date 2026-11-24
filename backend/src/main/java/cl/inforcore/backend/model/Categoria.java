package cl.inforcore.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Categoría del catálogo (Notebooks, Monitores...). Editable desde el panel.
 */
@Entity
@Table(name = "inforcore_categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código legible (ej: "notebooks"): es el "id" de texto que la categoría
     * tenía en la EP2. Va en la URL de la tienda (/categorias/notebooks) y no
     * cambia aunque se renombre la categoría.
     *
     * Es unique pero NO es la clave primaria (clase del 10-11): unique impide
     * que se repita, pero las relaciones con otras tablas se hacen con el id.
     */
    @Column(nullable = false, unique = true, length = 60)
    private String codigo;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    public Categoria(String codigo, String nombre, String descripcion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
}
