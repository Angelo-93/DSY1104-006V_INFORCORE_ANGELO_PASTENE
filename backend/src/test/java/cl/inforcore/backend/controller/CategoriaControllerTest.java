package cl.inforcore.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Pruebas de integración de la API de categorías (CRUD y reglas de negocio). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listaLasCategoriasConSuCantidadDeProductos() throws Exception {
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[0].codigo").value("notebooks"))
                .andExpect(jsonPath("$[0].cantidadProductos").value(4))
                .andExpect(jsonPath("$[6].codigo").value("servidores"))
                .andExpect(jsonPath("$[6].cantidadProductos").value(0));
    }

    @Test
    void obtieneUnaCategoria() throws Exception {
        mockMvc.perform(get("/api/categorias/monitores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Monitores"))
                .andExpect(jsonPath("$.cantidadProductos").value(2));
        mockMvc.perform(get("/api/categorias/no-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    void creaUnaCategoriaYGeneraSuCodigoDesdeElNombre() throws Exception {
        mockMvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Redes y Wi-Fi\", \"descripcion\": \"Routers y access points.\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/categorias/redes-y-wi-fi"))
                .andExpect(jsonPath("$.codigo").value("redes-y-wi-fi"))
                .andExpect(jsonPath("$.cantidadProductos").value(0));
    }

    @Test
    void siElCodigoYaExisteLeAgregaUnNumero() throws Exception {
        // "Notebooks!" es otro nombre, pero su código base sería "notebooks", que ya existe.
        mockMvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Notebooks!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("notebooks-2"));
    }

    @Test
    void rechazaUnNombreRepetidoSinImportarMayusculasNiTildes() throws Exception {
        mockMvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"  IMPRESION \"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errores.nombre").value("Ya existe la categoría \"IMPRESION\"."));
    }

    @Test
    void rechazaUnNombreVacio() throws Exception {
        mockMvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio."));
    }

    @Test
    void alRenombrarSeMantieneElCodigo() throws Exception {
        mockMvc.perform(put("/api/categorias/impresion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Impresoras\", \"descripcion\": \"Láser y multifuncionales.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("impresion"))
                .andExpect(jsonPath("$.nombre").value("Impresoras"))
                .andExpect(jsonPath("$.cantidadProductos").value(2));
    }

    @Test
    void noPermiteRenombrarConElNombreDeOtraCategoria() throws Exception {
        mockMvc.perform(put("/api/categorias/impresion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"monitores\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminaUnaCategoriaSinProductos() throws Exception {
        mockMvc.perform(delete("/api/categorias/servidores"))
                .andExpect(status().isNoContent());
    }

    @Test
    void noEliminaUnaCategoriaConProductos() throws Exception {
        mockMvc.perform(delete("/api/categorias/notebooks"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(
                        "No se puede eliminar: la categoría tiene 4 productos. Muévelos a otra categoría primero."));
    }
}
