package cl.inforcore.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Línea de una orden: qué producto, a qué precio y cuántas unidades.
 * Es la tabla intermedia entre órdenes y productos (una orden tiene muchos
 * productos y un producto aparece en muchas órdenes).
 */
@Entity
@Table(name = "inforcore_detalle_ordenes")
@Getter
@Setter
@NoArgsConstructor
public class DetalleOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK orden_id: la orden a la que pertenece esta línea.
    @ManyToOne
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    // FK producto_id: qué se vendió. Por esta FK la base de datos no deja
    // eliminar un producto que ya tiene ventas.
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // Nombre y precio COPIADOS al momento de la compra, como en la EP2: si
    // mañana el producto cambia de precio o de nombre, la boleta no cambia.
    @Column(nullable = false, length = 100)
    private String nombreProducto;

    @Column(nullable = false)
    private Integer precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    public DetalleOrden(Producto producto, Integer precioUnitario, Integer cantidad) {
        this.producto = producto;
        this.nombreProducto = producto.getNombre();
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    /** Precio por cantidad. No es columna: se calcula cuando se necesita. */
    public int getSubtotal() {
        return precioUnitario * cantidad;
    }
}
