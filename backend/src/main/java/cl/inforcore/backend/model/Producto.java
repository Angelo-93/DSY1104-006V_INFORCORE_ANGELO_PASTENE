package cl.inforcore.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Producto del catálogo. Las reglas de negocio (código mínimo 3 caracteres,
 * precio de oferta menor al precio, etc.) se validan en el servicio; aquí van
 * las restricciones que debe cumplir la tabla (obligatorios, únicos, largos).
 */
@Entity
@Table(name = "inforcore_productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código del catálogo (ej: "NB-HP250G10"). Único: va en la URL del producto. */
    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    /** Línea corta de la tarjeta (ej: "Intel Core i5 · 16 GB RAM"). Opcional. */
    @Column(length = 200)
    private String especificaciones;

    @Column(length = 500)
    private String descripcion;

    /** Pesos chilenos: no tienen decimales, por eso Integer y no double. */
    @Column(nullable = false)
    private Integer precio;

    /** null = sin oferta. Si existe, debe ser menor que el precio (lo valida el servicio). */
    private Integer precioOferta;

    @Column(nullable = false)
    private Integer stock;

    /** null = sin alerta. El producto es crítico cuando stock <= stockCritico. */
    private Integer stockCritico;

    // FK categoria_id: todo producto pertenece a una categoría. Por esta FK la
    // base de datos no deja eliminar una categoría que todavía tiene productos.
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // Reglas de negocio del producto (eran utils/calculos.js en la EP2). Viven en
    // la entidad porque solo dependen de sus propios datos; así el servicio, los
    // DTO y la compra usan exactamente la misma regla. No son columnas: se calculan.

    /** Tiene una oferta válida: precio de oferta definido y menor que el precio normal. */
    public boolean estaEnOferta() {
        return precioOferta != null && precioOferta >= 0 && precioOferta < precio;
    }

    /** Precio que paga el cliente hoy: el de oferta si hay una válida; si no, el normal. */
    public int getPrecioVigente() {
        return estaEnOferta() ? precioOferta : precio;
    }

    /**
     * Stock crítico: el stock llegó al umbral o bajó de él. Se usa <= y no <:
     * con stock 4 y crítico 4 ya hay que reponer. Sin umbral, nunca es crítico.
     */
    public boolean esCritico() {
        return stockCritico != null && stock <= stockCritico;
    }
}
