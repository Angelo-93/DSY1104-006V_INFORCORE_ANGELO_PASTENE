package cl.inforcore.backend.controller;

import cl.inforcore.backend.dto.BlogDTO;
import cl.inforcore.backend.dto.ErrorRespuesta;
import cl.inforcore.backend.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Artículos del blog de la tienda (solo lectura). */
@RestController
@RequestMapping("/api/blogs")
@Tag(name = "Blog", description = "Artículos del blog (solo lectura).")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @GetMapping
    @Operation(summary = "Listar artículos")
    public List<BlogDTO> listar() {
        return blogService.listar();
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener un artículo por su código", description = "Ej: renovar-equipos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artículo encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe el artículo",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public BlogDTO obtener(@PathVariable String codigo) {
        return blogService.obtener(codigo);
    }
}
