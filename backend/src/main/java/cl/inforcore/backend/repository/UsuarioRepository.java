package cl.inforcore.backend.repository;

import cl.inforcore.backend.model.Rol;
import cl.inforcore.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Para el login: el correo se compara sin importar mayúsculas. */
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByRun(String run);

    // Métodos "exists" (clase del 10-11): responden true/false sin traer el
    // registro completo. Se usan para no registrar dos veces el mismo RUN o correo.
    boolean existsByRun(String run);

    boolean existsByCorreoIgnoreCase(String correo);

    /** Para la regla "no se puede eliminar ni quitar el rol al último Administrador". */
    long countByRol(Rol rol);

    List<Usuario> findAllByOrderByIdAsc();
}
