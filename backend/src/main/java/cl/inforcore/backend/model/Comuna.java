package cl.inforcore.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Comuna de Chile. Los usuarios y las órdenes apuntan a una comuna con una
 * clave foránea; la región se obtiene desde la comuna (comuna.getRegion()), así
 * no se guarda dos veces ni puede quedar una comuna en la región equivocada.
 */
@Entity
@Table(name = "inforcore_comunas")
@Getter
@Setter
@NoArgsConstructor
public class Comuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    // FK region_id: cada comuna pertenece a una región.
    @ManyToOne
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    public Comuna(String nombre) {
        this.nombre = nombre;
    }
}
