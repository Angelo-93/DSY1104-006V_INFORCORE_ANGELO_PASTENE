package cl.inforcore.backend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de las validaciones (no levantan Spring: son funciones puras).
 * @ParameterizedTest ejecuta la misma prueba con cada valor de la lista.
 */
class ValidacionesTest {

    @ParameterizedTest
    @ValueSource(strings = {"182345679", "167890121", "201112222", "156789011", "19011029K", "19011029k", "98765433", "10000013K"})
    void aceptaRunsValidos(String run) {
        assertTrue(Validaciones.runEsValido(run));
    }

    @ParameterizedTest
    @ValueSource(strings = {"182345678", "0000000", "12345", "1234567890", "18.234.567-9", "18234567-9", "1823456K9"})
    void rechazaRunsInvalidos(String run) {
        assertFalse(Validaciones.runEsValido(run));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rechazaRunVacio(String run) {
        assertFalse(Validaciones.runEsValido(run));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@duoc.cl", "profe@profesor.duoc.cl", "cliente@GMAIL.com"})
    void aceptaLosDominiosPermitidos(String correo) {
        assertTrue(Validaciones.correoTieneDominioPermitido(correo));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@hotmail.com", "sin-arroba", "@gmail.com", "a@b@gmail.com", "ana@duoc.cl.com"})
    void rechazaLosDemasCorreos(String correo) {
        assertFalse(Validaciones.correoTieneDominioPermitido(correo));
    }

    @Test
    void rechazaCorreoNulo() {
        assertFalse(Validaciones.correoTieneDominioPermitido(null));
    }
}
