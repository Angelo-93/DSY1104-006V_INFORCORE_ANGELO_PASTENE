package cl.inforcore.backend.repository;

import cl.inforcore.backend.model.Orden;
import cl.inforcore.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    /** Todas las órdenes, las más recientes primero (vista Órdenes del panel). */
    List<Orden> findAllByOrderByFechaDesc();

    /** Compras de un usuario (historial del cliente y del panel). */
    List<Orden> findByUsuarioOrderByFechaDesc(Usuario usuario);

    /** Para la regla "no se elimina un usuario que tiene compras". */
    boolean existsByUsuario(Usuario usuario);
}
