package cl.inforcore.backend.service;

import cl.inforcore.backend.dto.RegionDTO;
import cl.inforcore.backend.exception.DatosInvalidosException;
import cl.inforcore.backend.model.Comuna;
import cl.inforcore.backend.repository.ComunaRepository;
import cl.inforcore.backend.repository.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regiones y comunas: solo lectura. También resuelve la comuna que eligen los
 * formularios (registro, usuarios del panel y checkout).
 */
@Service
public class RegionService {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ComunaRepository comunaRepository;

    /**
     * @Transactional(readOnly = true): la lectura ocurre dentro de una
     * transacción, así se pueden recorrer las comunas de cada región (una lista
     * que Hibernate carga recién cuando se usa) mientras se arma el DTO.
     */
    @Transactional(readOnly = true)
    public List<RegionDTO> listar() {
        return regionRepository.findAllByOrderByIdAsc().stream().map(RegionDTO::desde).toList();
    }

    /**
     * Busca la comuna elegida dentro de la región elegida. Si no calzan (alguien
     * manipuló la petición), se rechaza: no se guarda una comuna en otra región.
     */
    @Transactional(readOnly = true)
    public Comuna buscarComuna(String region, String comuna) {
        return comunaRepository.findByNombreAndRegionNombre(comuna.trim(), region.trim())
                .orElseThrow(() -> new DatosInvalidosException("comuna",
                        "La comuna seleccionada no pertenece a la región."));
    }
}
