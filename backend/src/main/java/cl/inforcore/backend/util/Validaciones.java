package cl.inforcore.backend.util;

import java.util.List;
import java.util.Locale;

/**
 * Reglas de validación que no caben en una anotación (@NotBlank, @Size...).
 * Son las mismas de frontend/src/utils/validaciones.js: el frontend valida para
 * avisarle al usuario al tiro, y el backend vuelve a validar porque es el único
 * que no se puede saltar (alguien podría llamar a la API directo con Postman).
 */
public final class Validaciones {

    public static final List<String> DOMINIOS_PERMITIDOS = List.of("duoc.cl", "profesor.duoc.cl", "gmail.com");

    public static final String MENSAJE_DOMINIO = "Solo se aceptan correos @duoc.cl, @profesor.duoc.cl o @gmail.com.";

    private Validaciones() {
        // Solo métodos estáticos: no se instancia.
    }

    /**
     * Valida un RUN chileno sin puntos ni guion (ej: "19011029K") con el
     * algoritmo módulo 11: cada dígito del cuerpo, de derecha a izquierda, se
     * multiplica por 2, 3, 4, 5, 6, 7, 2, 3...; el dígito verificador es 11 menos
     * el resto de dividir la suma por 11 (11 da "0" y 10 da "K").
     */
    public static boolean runEsValido(String runSinFormato) {
        if (runSinFormato == null) {
            return false;
        }
        String run = runSinFormato.trim().toUpperCase(Locale.ROOT);
        // El primer dígito no puede ser 0: sin esa regla "0000000" pasaría el módulo 11.
        if (!run.matches("^[1-9][0-9]*[0-9K]$") || run.length() < 7 || run.length() > 9) {
            return false;
        }

        String cuerpo = run.substring(0, run.length() - 1);
        char dvIngresado = run.charAt(run.length() - 1);

        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }

        int resto = 11 - (suma % 11);
        char dvEsperado = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        return dvIngresado == dvEsperado;
    }

    /** true si el dominio del correo (lo que va después de la @) está permitido. */
    public static boolean correoTieneDominioPermitido(String correo) {
        if (correo == null) {
            return false;
        }
        String[] partes = correo.trim().split("@", -1);
        if (partes.length != 2 || partes[0].isEmpty()) {
            return false;
        }
        return DOMINIOS_PERMITIDOS.contains(partes[1].toLowerCase(Locale.ROOT));
    }
}
