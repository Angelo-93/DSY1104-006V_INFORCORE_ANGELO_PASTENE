package cl.inforcore.backend.repository;

import cl.inforcore.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    /** IgnoreCase: "monitores" y "Monitores" cuentan como el mismo nombre. */
    boolean existsByNombreIgnoreCase(String nombre);

    /** En orden de creación, como se mostraban en la EP2. */
    List<Categoria> findAllByOrderByIdAsc();
}
