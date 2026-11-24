package cl.inforcore.backend.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Ayudantes para texto escrito por personas. Son los mismos de
 * frontend/src/utils/texto.js, para que el front y el back comparen igual.
 */
public final class Textos {

    private Textos() {
        // Solo métodos estáticos: no se instancia.
    }

    /**
     * Minúsculas, sin tildes y sin espacios sobrantes, para comparar sin que
     * importe cómo se escribió: "Impresión", "impresion" e "IMPRESIÓN" quedan iguales.
     * NFD separa cada letra de su tilde ("ó" pasa a "o" + "´") y \p{M} borra esas tildes sueltas.
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim();
    }

    /** Texto apto para una URL: "Audio y Videoconferencia" pasa a "audio-y-videoconferencia". */
    public static String generarSlug(String texto) {
        return normalizar(texto)
                .replaceAll("[^a-z0-9]+", "-") // todo lo que no sea letra o número pasa a guion
                .replaceAll("^-+|-+$", "");    // sin guiones al inicio ni al final
    }

    /** pluralizar(1, "producto", "productos") da "1 producto"; con 3 da "3 productos". */
    public static String pluralizar(long cantidad, String singular, String plural) {
        return cantidad + " " + (cantidad == 1 ? singular : plural);
    }

    /** Quita los espacios sobrantes; un texto vacío queda en null (campos opcionales). */
    public static String limpiarOpcional(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
