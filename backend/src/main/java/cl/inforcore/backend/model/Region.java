package cl.inforcore.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Región de Chile. Es un dato de referencia: el sistema solo lo lee, para los
 * selectores región → comuna del registro, el checkout y el mantenedor de
 * usuarios. En la EP2 era una lista escrita en el frontend.
 */
@Entity
@Table(name = "inforcore_regiones")
@Getter
@Setter
@NoArgsConstructor
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    // Relación bidireccional: la FK (region_id) está en la tabla de comunas, por
    // eso "mappedBy". Aquí solo se navega de la región a sus comunas.
    // CascadeType.ALL: al guardar la región se guardan también sus comunas.
    // @OrderBy("id"): se devuelven en el orden en que se cargaron (el de la EP1).
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<Comuna> comunas = new ArrayList<>();

    public Region(String nombre) {
        this.nombre = nombre;
    }

    /** Agrega una comuna y deja enlazados los dos lados de la relación. */
    public void agregarComuna(Comuna comuna) {
        comuna.setRegion(this);
        comunas.add(comuna);
    }
}
