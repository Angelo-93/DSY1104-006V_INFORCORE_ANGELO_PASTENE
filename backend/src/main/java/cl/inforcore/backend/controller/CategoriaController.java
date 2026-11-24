package cl.inforcore.backend.controller;

import cl.inforcore.backend.dto.CategoriaDTO;
import cl.inforcore.backend.dto.CategoriaRequest;
import cl.inforcore.backend.dto.ErrorRespuesta;
import cl.inforcore.backend.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** Endpoints de categorías: CRUD completo. */
@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Categorías del catálogo (CRUD). El código se genera desde el nombre.")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Cada una con su cantidad de productos.")
    public List<CategoriaDTO> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener una categoría por su código")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe la categoría",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public CategoriaDTO obtener(@PathVariable String codigo) {
        return categoriaService.obtener(codigo);
    }

    @PostMapping
    @Operation(summary = "Crear una categoría", description = "El código se genera desde el nombre (ej: \"Redes y WiFi\" → redes-y-wifi).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<CategoriaDTO> crear(@Valid @RequestBody CategoriaRequest datos) {
        CategoriaDTO creada = categoriaService.crear(datos);
        return ResponseEntity.created(URI.create("/api/categorias/" + creada.codigo())).body(creada);
    }

    @PutMapping("/{codigo}")
    @Operation(summary = "Actualizar una categoría", description = "Cambia nombre y descripción; el código se mantiene.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "404", description = "No existe la categoría",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "El nombre ya lo usa otra categoría",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public CategoriaDTO actualizar(@PathVariable String codigo, @Valid @RequestBody CategoriaRequest datos) {
        return categoriaService.actualizar(codigo, datos);
    }

    @DeleteMapping("/{codigo}")
    @Operation(summary = "Eliminar una categoría", description = "Solo si no tiene productos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe la categoría",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "La categoría tiene productos",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        categoriaService.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }
}
