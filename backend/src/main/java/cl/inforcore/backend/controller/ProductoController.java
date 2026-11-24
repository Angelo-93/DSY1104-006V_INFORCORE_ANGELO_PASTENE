package cl.inforcore.backend.controller;

import cl.inforcore.backend.dto.ErrorRespuesta;
import cl.inforcore.backend.dto.ProductoDTO;
import cl.inforcore.backend.dto.ProductoRequest;
import cl.inforcore.backend.service.ProductoService;
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

/**
 * Endpoints de productos. El controlador es la puerta de entrada (clase del
 * 02-11): recibe la petición HTTP, se la pasa al servicio y devuelve la
 * respuesta. No tiene reglas de negocio.
 *
 * Verbos HTTP: GET lee, POST crea, PUT actualiza y DELETE elimina (CRUD).
 */
@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Catálogo de la tienda: consulta, búsqueda, ofertas, stock crítico y mantenedor (CRUD).")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar productos",
            description = "Sin parámetros devuelve todo el catálogo. Con ?categoria=notebooks filtra por categoría; "
                    + "con ?buscar=texto busca en el nombre y el código.")
    public List<ProductoDTO> listar(@RequestParam(required = false) String buscar,
                                    @RequestParam(required = false) String categoria) {
        return productoService.listar(buscar, categoria);
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Listar productos en oferta", description = "Los que tienen un precio de oferta menor al precio normal.")
    public List<ProductoDTO> listarOfertas() {
        return productoService.listarOfertas();
    }

    @GetMapping("/criticos")
    @Operation(summary = "Listar productos en stock crítico", description = "Stock igual o menor a su stock crítico.")
    public List<ProductoDTO> listarCriticos() {
        return productoService.listarCriticos();
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener un producto por su código")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un producto con ese código",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ProductoDTO obtener(@PathVariable String codigo) {
        return productoService.obtener(codigo);
    }

    @PostMapping
    @Operation(summary = "Crear un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (ej: precio de oferta mayor o igual al precio)",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese código",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<ProductoDTO> crear(@Valid @RequestBody ProductoRequest datos) {
        ProductoDTO creado = productoService.crear(datos);
        // 201 Created (clase del 02-11) + cabecera Location con la URL del recurso nuevo.
        return ResponseEntity.created(URI.create("/api/productos/" + creado.codigo())).body(creado);
    }

    @PutMapping("/{codigo}")
    @Operation(summary = "Actualizar un producto", description = "El código no se modifica: identifica al producto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "No existe un producto con ese código",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ProductoDTO actualizar(@PathVariable String codigo, @Valid @RequestBody ProductoRequest datos) {
        return productoService.actualizar(codigo, datos);
    }

    @DeleteMapping("/{codigo}")
    @Operation(summary = "Eliminar un producto", description = "No se puede eliminar si ya tiene ventas registradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe un producto con ese código",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "El producto tiene ventas registradas",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        productoService.eliminar(codigo);
        // 204 No Content: se eliminó y no hay nada que devolver.
        return ResponseEntity.noContent().build();
    }
}
