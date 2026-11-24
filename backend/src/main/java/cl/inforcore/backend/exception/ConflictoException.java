package cl.inforcore.backend.exception;

/**
 * La operación choca con los datos que ya existen (ej: código repetido, eliminar
 * una categoría que tiene productos). Se convierte en 409 Conflict: los datos
 * enviados pueden estar bien escritos, pero el estado actual no lo permite.
 * El campo es opcional: se usa cuando el conflicto es de un dato del formulario.
 */
public class ConflictoException extends RuntimeException {

    private final String campo;

    public ConflictoException(String mensaje) {
        this(null, mensaje);
    }

    public ConflictoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
