package cl.inforcore.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprueba que Swagger documenta los endpoints CRUD (indicador IE3.2.1:
 * "mostrando los endpoints en Swagger").
 */
@SpringBootTest
@AutoConfigureMockMvc
class DocumentacionSwaggerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void elDocumentoOpenApiIncluyeLosEndpointsCrud() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API REST INFORCORE"))
                .andExpect(jsonPath("$.paths['/api/productos'].get").exists())
                .andExpect(jsonPath("$.paths['/api/productos'].post").exists())
                .andExpect(jsonPath("$.paths['/api/productos/{codigo}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/productos/{codigo}'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/categorias/{codigo}'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/usuarios/{run}'].put").exists());
    }

    @Test
    void laInterfazDeSwaggerEstaDisponible() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }
}
