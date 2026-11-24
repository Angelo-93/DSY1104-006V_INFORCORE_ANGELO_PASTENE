package cl.inforcore.backend.repository;

import cl.inforcore.backend.model.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComunaRepository extends JpaRepository<Comuna, Long> {

    /**
     * Busca una comuna por su nombre dentro de una región. "RegionNombre"
     * navega la relación: comuna.region.nombre. Se pide la región también
     * para no aceptar una comuna que no pertenece a la región elegida.
     */
    Optional<Comuna> findByNombreAndRegionNombre(String nombre, String nombreRegion);
}
