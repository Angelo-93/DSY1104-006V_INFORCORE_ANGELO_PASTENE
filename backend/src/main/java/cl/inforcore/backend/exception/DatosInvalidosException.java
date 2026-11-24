package cl.inforcore.backend.exception;

/**
 * Los datos no cumplen una regla de negocio (ej: precio de oferta mayor o igual
 * al precio, RUN con dígito verificador incorrecto). Se convierte en 400 Bad
 * Request. Lleva el campo del formulario al que corresponde el error, para que
 * el frontend pueda mostrar el mensaje justo bajo ese campo.
 */
public class DatosInvalidosException extends RuntimeException {

    private final String campo;

    public DatosInvalidosException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
