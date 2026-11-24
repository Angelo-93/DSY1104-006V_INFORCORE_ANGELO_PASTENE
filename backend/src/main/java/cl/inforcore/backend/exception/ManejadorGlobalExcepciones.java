package cl.inforcore.backend.exception;

import cl.inforcore.backend.dto.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo de excepciones en un solo lugar (@ControllerAdvice, clase 3.1.1).
 *
 * Los servicios lanzan excepciones con significado (no encontrado, datos
 * inválidos, conflicto) y aquí cada una se traduce a su código HTTP y al
 * formato ErrorRespuesta. Así ningún controlador necesita try/catch y todos
 * los errores de la API se ven iguales. La versión "Rest" de la anotación hace
 * que la respuesta se escriba como JSON.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    /** 404: el recurso pedido no existe. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest peticion) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of(), peticion);
    }

    /** 400: una regla de negocio rechazó los datos (ej: precio de oferta mayor al precio). */
    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorRespuesta> datosInvalidos(DatosInvalidosException ex, HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), errorDeCampo(ex.getCampo(), ex.getMessage()), peticion);
    }

    /** 409: la operación choca con los datos existentes (ej: código repetido). */
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorRespuesta> conflicto(ConflictoException ex, HttpServletRequest peticion) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), errorDeCampo(ex.getCampo(), ex.getMessage()), peticion);
    }

    /**
     * 400: falló una anotación de validación del DTO (@NotBlank, @Size, @Min...).
     * Spring la lanza cuando el controlador recibe @Valid @RequestBody. Se junta
     * un mensaje por campo para mostrarlo bajo cada campo del formulario.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException ex, HttpServletRequest peticion) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Hay datos inválidos: revisa los campos marcados.", errores, peticion);
    }

    /** 400: el cuerpo no es un JSON válido o un valor no calza con su tipo (ej: rol inexistente). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no tiene el formato JSON esperado.", Map.of(), peticion);
    }

    /** 400: un parámetro de la URL no tiene el tipo esperado (ej: /api/ordenes/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' no tiene el formato esperado.", Map.of(), peticion);
    }

    /**
     * 409: red de seguridad. Si alguna restricción de la base (FK o unique) se
     * viola sin que el servicio lo haya revisado antes, no se responde 500.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> integridad(DataIntegrityViolationException ex, HttpServletRequest peticion) {
        log.warn("Restricción de la base de datos violada en {}: {}", peticion.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT,
                "La operación no se puede realizar porque dejaría datos inconsistentes.", Map.of(), peticion);
    }

    /**
     * Todo lo demás. Las excepciones propias de Spring MVC (ruta inexistente,
     * método HTTP no permitido...) ya traen su código, así que se respeta; el
     * resto es un error inesperado: 500, y el detalle queda en el log del
     * servidor, no en la respuesta (no se le muestran detalles internos al cliente).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> inesperado(Exception ex, HttpServletRequest peticion) {
        if (ex instanceof ErrorResponse errorDeSpring) {
            HttpStatus estado = HttpStatus.valueOf(errorDeSpring.getStatusCode().value());
            return responder(estado, mensajeSegunEstado(estado), Map.of(), peticion);
        }
        log.error("Error inesperado en {}", peticion.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor.", Map.of(), peticion);
    }

    private String mensajeSegunEstado(HttpStatus estado) {
        return switch (estado) {
            case NOT_FOUND -> "No existe la ruta solicitada.";
            case METHOD_NOT_ALLOWED -> "Esta ruta no acepta ese método HTTP.";
            case UNSUPPORTED_MEDIA_TYPE -> "Tipo de contenido no soportado: envía JSON.";
            default -> estado.getReasonPhrase();
        };
    }

    private Map<String, String> errorDeCampo(String campo, String mensaje) {
        return campo == null ? Map.of() : Map.of(campo, mensaje);
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, String mensaje, Map<String, String> errores,
                                                     HttpServletRequest peticion) {
        return ResponseEntity.status(estado).body(ErrorRespuesta.de(estado, mensaje, errores, peticion.getRequestURI()));
    }
}
