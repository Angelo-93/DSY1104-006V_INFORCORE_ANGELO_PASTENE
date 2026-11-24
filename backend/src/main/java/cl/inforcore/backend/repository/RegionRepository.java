package cl.inforcore.backend.repository;

import cl.inforcore.backend.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionRepository extends JpaRepository<Region, Long> {

    // Spring Data arma la consulta a partir del nombre del método (clase del 02-11):
    // "findAllByOrderByIdAsc" = SELECT * FROM inforcore_regiones ORDER BY id ASC.
    // Por id = de norte a sur, que es el orden en que se cargaron.
    List<Region> findAllByOrderByIdAsc();
}
