package cl.inforcore.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de los datos de solo lectura que antes estaban "en duro" en el frontend. */
@SpringBootTest
@AutoConfigureMockMvc
class RegionYBlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void entregaLas16RegionesDeNorteASurConSusComunas() throws Exception {
        mockMvc.perform(get("/api/regiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(16))
                .andExpect(jsonPath("$[0].nombre").value("Región de Arica y Parinacota"))
                .andExpect(jsonPath("$[0].comunas.length()").value(4))
                .andExpect(jsonPath("$[15].nombre").value("Región de Magallanes y de la Antártica Chilena"));
    }

    @Test
    void entregaLosArticulosDelBlogConSusParrafos() throws Exception {
        mockMvc.perform(get("/api/blogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].parrafos.length()").value(3));
        mockMvc.perform(get("/api/blogs/laboratorio-escolar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etiqueta").value("Caso curioso #2"));
        mockMvc.perform(get("/api/blogs/no-existe"))
                .andExpect(status().isNotFound());
    }
}
