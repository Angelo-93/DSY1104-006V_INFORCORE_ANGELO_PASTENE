package cl.inforcore.backend.dto;

import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Formato único de los errores de la API: todos los errores responden con la
 * misma forma, así el frontend los lee siempre igual.
 *
 * Ejemplo (400):
 * { "estado": 400, "error": "Bad Request", "mensaje": "Hay datos inválidos...",
 *   "errores": { "precioOferta": "El precio de oferta debe ser menor..." },
 *   "ruta": "/api/productos", "fecha": "2026-11-24T10:00:00-03:00" }
 *
 * @param errores Mensaje por campo del formulario (vacío si el error no es de un campo).
 */
public record ErrorRespuesta(
        int estado,
        String error,
        String mensaje,
        Map<String, String> errores,
        String ruta,
        OffsetDateTime fecha) {

    public static ErrorRespuesta de(HttpStatus estado, String mensaje, Map<String, String> errores, String ruta) {
        return new ErrorRespuesta(estado.value(), estado.getReasonPhrase(), mensaje,
                errores == null ? Map.of() : errores, ruta, OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }
}
