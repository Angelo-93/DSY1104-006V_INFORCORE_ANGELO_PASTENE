package cl.inforcore.backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Pruebas unitarias de los ayudantes de texto. */
class TextosTest {

    @Test
    void normalizaSinMayusculasTildesNiEspacios() {
        assertEquals("impresion", Textos.normalizar("  IMPRESIÓN "));
        assertEquals("nunoa", Textos.normalizar("Ñuñoa"));
        assertEquals("", Textos.normalizar(null));
    }

    @Test
    void generaCodigosAptosParaUrl() {
        assertEquals("audio-y-videoconferencia", Textos.generarSlug("Audio y Videoconferencia"));
        assertEquals("desktops-y-aio", Textos.generarSlug("Desktops y AIO"));
        assertEquals("redes-wi-fi", Textos.generarSlug("  ¡Redes & Wi-Fi!  "));
        assertEquals("", Textos.generarSlug("!!!"));
    }

    @Test
    void pluralizaSegunLaCantidad() {
        assertEquals("1 producto", Textos.pluralizar(1, "producto", "productos"));
        assertEquals("4 productos", Textos.pluralizar(4, "producto", "productos"));
        assertEquals("0 productos", Textos.pluralizar(0, "producto", "productos"));
    }

    @Test
    void unOpcionalVacioQuedaEnNull() {
        assertNull(Textos.limpiarOpcional("   "));
        assertNull(Textos.limpiarOpcional(null));
        assertEquals("Depto 42", Textos.limpiarOpcional("  Depto 42 "));
    }
}
