package cl.inforcore.backend.controller;

import cl.inforcore.backend.dto.ErrorRespuesta;
import cl.inforcore.backend.dto.OrdenDTO;
import cl.inforcore.backend.dto.UsuarioDTO;
import cl.inforcore.backend.dto.UsuarioRequest;
import cl.inforcore.backend.service.OrdenService;
import cl.inforcore.backend.service.UsuarioService;
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

/** Endpoints del mantenedor de usuarios del panel: CRUD completo e historial de compras. */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Mantenedor de usuarios (CRUD). Las respuestas nunca incluyen la contraseña.")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private OrdenService ordenService;

    @GetMapping
    @Operation(summary = "Listar usuarios")
    public List<UsuarioDTO> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{run}")
    @Operation(summary = "Obtener un usuario por su RUN", description = "RUN sin puntos ni guion (ej: 182345679).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public UsuarioDTO obtener(@PathVariable String run) {
        return usuarioService.obtener(run);
    }

    @GetMapping("/{run}/ordenes")
    @Operation(summary = "Historial de compras de un usuario", description = "Sus órdenes, las más recientes primero.")
    public List<OrdenDTO> listarOrdenes(@PathVariable String run) {
        return ordenService.listarDeUsuario(run);
    }

    @PostMapping
    @Operation(summary = "Crear un usuario", description = "La contraseña se guarda como hash BCrypt.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (RUN, dominio del correo, contraseña...)",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "El RUN o el correo ya están registrados",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<UsuarioDTO> crear(@Valid @RequestBody UsuarioRequest datos) {
        UsuarioDTO creado = usuarioService.crear(datos);
        return ResponseEntity.created(URI.create("/api/usuarios/" + creado.run())).body(creado);
    }

    @PutMapping("/{run}")
    @Operation(summary = "Actualizar un usuario",
            description = "El RUN no cambia. Contraseña vacía = se mantiene la actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Correo repetido o se le quitaría el rol al único Administrador",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public UsuarioDTO actualizar(@PathVariable String run, @Valid @RequestBody UsuarioRequest datos) {
        return usuarioService.actualizar(run, datos);
    }

    @DeleteMapping("/{run}")
    @Operation(summary = "Eliminar un usuario",
            description = "No se elimina al único Administrador ni a un usuario con compras registradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "409", description = "Es el único Administrador o tiene compras",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public ResponseEntity<Void> eliminar(@PathVariable String run) {
        usuarioService.eliminar(run);
        return ResponseEntity.noContent().build();
    }
}
