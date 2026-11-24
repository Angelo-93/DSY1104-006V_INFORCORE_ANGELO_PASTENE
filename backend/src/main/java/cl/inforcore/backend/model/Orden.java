package cl.inforcore.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Orden de compra: lo que la boleta muestra. En la EP2 vivía en localStorage
 * (clave inforcore_ordenes); ahora es una tabla relacionada con el usuario que
 * compró, la comuna de entrega y los productos vendidos (a través del detalle).
 */
@Entity
@Table(name = "inforcore_ordenes")
@Getter
@Setter
@NoArgsConstructor
public class Orden {

    /**
     * Número de la orden, el mismo que se muestra en la boleta. Lo entrega una
     * secuencia de la base de datos que parte en 1001, igual que en la EP2.
     * allocationSize = 1: cada orden pide el número siguiente, sin saltos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orden_seq")
    @SequenceGenerator(name = "orden_seq", sequenceName = "inforcore_ordenes_seq",
            initialValue = 1001, allocationSize = 1)
    private Long id;

    /** Fecha con la zona horaria de Chile (ej: 2026-09-12T12:05-03:00), como en la EP2. */
    @Column(nullable = false)
    private OffsetDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrden estado;

    /** Por qué se rechazó el pago. null en las órdenes pagadas. */
    @Column(length = 500)
    private String motivoRechazo;

    // FK usuario_id: quién compró. En la EP3 comprar exige haber iniciado sesión.
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Datos del cliente COPIADOS al comprar: la boleta debe seguir mostrando lo
    // que se emitió aunque después el usuario cambie su nombre o su correo.
    @Column(nullable = false, length = 50)
    private String nombreCliente;

    @Column(nullable = false, length = 100)
    private String apellidosCliente;

    @Column(nullable = false, length = 100)
    private String correoCliente;

    // Dirección de entrega de ESTA orden (puede ser distinta a la del perfil).
    @Column(nullable = false, length = 300)
    private String calle;

    @Column(length = 50)
    private String departamento;

    @ManyToOne
    @JoinColumn(name = "comuna_id", nullable = false)
    private Comuna comuna;

    @Column(length = 300)
    private String indicaciones;

    /** Total en pesos. Lo calcula el servidor con los precios del catálogo, no el navegador. */
    @Column(nullable = false)
    private Integer total;

    // Líneas de la boleta. cascade = ALL: al guardar la orden se guardan sus
    // líneas. orphanRemoval = true: una línea no puede existir sin su orden.
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<DetalleOrden> items = new ArrayList<>();

    /** Agrega una línea y deja enlazados los dos lados de la relación. */
    public void agregarItem(DetalleOrden item) {
        item.setOrden(this);
        items.add(item);
    }
}
