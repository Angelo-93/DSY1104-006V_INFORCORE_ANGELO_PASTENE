package cl.inforcore.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Pruebas de integración de la consulta de órdenes y de los errores comunes de la API. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrdenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listaLasOrdenesDeLaMasRecienteALaMasAntigua() throws Exception {
        mockMvc.perform(get("/api/ordenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].numero").value(1006))
                .andExpect(jsonPath("$[5].numero").value(1001));
    }

    @Test
    void entregaTodoLoQueMuestraLaBoleta() throws Exception {
        mockMvc.perform(get("/api/ordenes/1006"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADA"))
                .andExpect(jsonPath("$.fecha").value("2026-10-05T15:45:00-03:00"))
                .andExpect(jsonPath("$.cliente.run").value("135792462"))
                .andExpect(jsonPath("$.direccion.comuna").value("San Joaquín"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(3 * 479990))
                .andExpect(jsonPath("$.total").value(3 * 479990 + 3 * 189990));
    }

    @Test
    void unaOrdenRechazadaTraeElMotivo() throws Exception {
        mockMvc.perform(get("/api/ordenes/1005"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.motivoRechazo", containsString("rechazó la transacción")));
    }

    @Test
    void unaOrdenInexistenteResponde404() throws Exception {
        mockMvc.perform(get("/api/ordenes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la orden N° 9999."));
    }

    @Test
    void unNumeroDeOrdenQueNoEsNumeroResponde400() throws Exception {
        mockMvc.perform(get("/api/ordenes/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El parámetro 'numero' no tiene el formato esperado."));
    }

    @Test
    void unaBoletaNoSeEdita() throws Exception {
        // No existe PUT para órdenes: Spring responde 405 y el manejador lo deja en el formato de errores.
        mockMvc.perform(put("/api/ordenes/1001").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.estado").value(405));
    }

    @Test
    void unaRutaInexistenteResponde404EnElMismoFormato() throws Exception {
        mockMvc.perform(get("/api/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la ruta solicitada."));
    }
}
