package cl.inforcore.backend.repository;

import cl.inforcore.backend.model.DetalleOrden;
import cl.inforcore.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {

    /** Para la regla "no se elimina un producto que ya tiene ventas". */
    boolean existsByProducto(Producto producto);
}
