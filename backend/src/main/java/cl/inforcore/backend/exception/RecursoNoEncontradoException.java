package cl.inforcore.backend.exception;

/**
 * Lo pedido no existe (ej: GET /api/productos/NO-EXISTE). El manejador global
 * la convierte en una respuesta 404 Not Found. Es la versión en español de la
 * ResourceNotFoundException de la diapositiva de la clase 3.1.1.
 *
 * Hereda de RuntimeException (unchecked, clase del 09-11): no obliga a escribir
 * try/catch en cada capa; sube sola hasta el manejador global.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
