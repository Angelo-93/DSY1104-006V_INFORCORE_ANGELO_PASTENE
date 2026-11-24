package cl.inforcore.backend.dto;

import cl.inforcore.backend.model.Comuna;
import cl.inforcore.backend.model.Region;

import java.util.List;

/**
 * Región con los nombres de sus comunas. Misma forma que el arreglo REGIONES
 * de la EP2 ({ id, nombre, comunas: [...] }), así los selectores encadenados
 * región → comuna del frontend se alimentan igual que antes.
 */
public record RegionDTO(Long id, String nombre, List<String> comunas) {

    public static RegionDTO desde(Region region) {
        return new RegionDTO(region.getId(), region.getNombre(),
                region.getComunas().stream().map(Comuna::getNombre).toList());
    }
}
