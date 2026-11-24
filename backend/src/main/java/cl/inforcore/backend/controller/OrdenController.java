package cl.inforcore.backend.controller;

import cl.inforcore.backend.dto.ErrorRespuesta;
import cl.inforcore.backend.dto.OrdenDTO;
import cl.inforcore.backend.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de órdenes (boletas). Por ahora solo consulta: crear una orden
 * (pagar el carrito) necesita saber QUIÉN compra, y eso lo entrega el token JWT
 * de la parte de seguridad. Editar y eliminar no existen a propósito: una
 * boleta emitida es un registro contable.
 */
@RestController
@RequestMapping("/api/ordenes")
@Tag(name = "Órdenes", description = "Órdenes de compra (boletas): consulta.")
public class OrdenController {

    @Autowired
    private OrdenService ordenService;

    @GetMapping
    @Operation(summary = "Listar órdenes", description = "Todas, las más recientes primero.")
    public List<OrdenDTO> listar() {
        return ordenService.listar();
    }

    @GetMapping("/{numero}")
    @Operation(summary = "Obtener una orden por su número", description = "Lo que muestra la boleta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe la orden",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    public OrdenDTO obtener(@PathVariable Long numero) {
        return ordenService.obtener(numero);
    }
}
