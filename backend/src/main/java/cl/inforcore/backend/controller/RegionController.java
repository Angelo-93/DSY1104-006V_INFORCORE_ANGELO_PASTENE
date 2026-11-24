package cl.inforcore.backend.controller;

import cl.inforcore.backend.dto.RegionDTO;
import cl.inforcore.backend.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Regiones y comunas de Chile para los selectores de los formularios (solo lectura). */
@RestController
@RequestMapping("/api/regiones")
@Tag(name = "Regiones", description = "Regiones de Chile con sus comunas (solo lectura).")
public class RegionController {

    @Autowired
    private RegionService regionService;

    @GetMapping
    @Operation(summary = "Listar regiones con sus comunas", description = "De norte a sur.")
    public List<RegionDTO> listar() {
        return regionService.listar();
    }
}
