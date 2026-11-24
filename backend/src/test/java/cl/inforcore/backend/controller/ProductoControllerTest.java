package cl.inforcore.backend.controller;

import cl.inforcore.backend.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración de la API de productos: cada prueba hace una petición
 * HTTP de verdad (con MockMvc, sin abrir un puerto) y revisa el código HTTP y
 * el JSON de la respuesta. Recorren las capas completas: controlador → servicio
 * → repositorio → base H2 en memoria. @Transactional deshace los cambios de
 * cada prueba al terminar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductoControllerTest {

    private static final String PRODUCTO_NUEVO = """
            {"codigo": "nb-test01", "nombre": "Notebook de prueba", "codigoCategoria": "notebooks",
             "especificaciones": "Core i5", "descripcion": "  ", "precio": 500000, "precioOferta": 450000,
             "stock": 3, "stockCritico": 5}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void listaTodoElCatalogo() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(14))
                .andExpect(jsonPath("$[0].codigo").value("NB-HP250G10"));
    }

    @Test
    void entregaLosDatosCalculadosPorElBackend() throws Exception {
        mockMvc.perform(get("/api/productos/MN-HPP24G5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCategoria").value("Monitores"))
                .andExpect(jsonPath("$.enOferta").value(true))
                .andExpect(jsonPath("$.precioVigente").value(84990))
                .andExpect(jsonPath("$.critico").value(false));
    }

    @Test
    void elCodigoEnMinusculasTambienEncuentraElProducto() throws Exception {
        mockMvc.perform(get("/api/productos/mn-hpp24g5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("MN-HPP24G5"));
    }

    @Test
    void unProductoInexistenteResponde404() throws Exception {
        mockMvc.perform(get("/api/productos/NO-EXISTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe el producto NO-EXISTE."));
    }

    @Test
    void filtraPorCategoria() throws Exception {
        mockMvc.perform(get("/api/productos").param("categoria", "monitores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void buscaTextoEnElNombreSinImportarMayusculas() throws Exception {
        mockMvc.perform(get("/api/productos").param("buscar", "laserjet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listaLasOfertas() throws Exception {
        mockMvc.perform(get("/api/productos/ofertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void listaLosProductosCriticos() throws Exception {
        mockMvc.perform(get("/api/productos/criticos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].codigo").value("AU-POLYP5"));
    }

    @Test
    void creaUnProductoYResponde201ConSuUbicacion() throws Exception {
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/productos/NB-TEST01"))
                .andExpect(jsonPath("$.codigo").value("NB-TEST01"))
                .andExpect(jsonPath("$.descripcion").doesNotExist())
                .andExpect(jsonPath("$.critico").value(true));
    }

    @Test
    void rechazaUnaOfertaMayorOIgualAlPrecio() throws Exception {
        String datos = PRODUCTO_NUEVO.replace("\"precioOferta\": 450000", "\"precioOferta\": 500000");
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.precioOferta").value("El precio de oferta debe ser menor que el precio normal."));
    }

    @Test
    void rechazaDatosQueNoCumplenLasValidaciones() throws Exception {
        String datos = """
                {"codigo": "A B", "nombre": "", "codigoCategoria": "notebooks", "precio": -1, "stock": 5}
                """;
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigo").exists())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio."))
                .andExpect(jsonPath("$.errores.precio").value("El precio no puede ser negativo."));
    }

    @Test
    void rechazaUnCodigoRepetido() throws Exception {
        String datos = PRODUCTO_NUEVO.replace("nb-test01", "NB-HP250G10");
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errores.codigo").value("Ya existe un producto con el código NB-HP250G10."));
    }

    @Test
    void rechazaUnaCategoriaQueNoExiste() throws Exception {
        String datos = PRODUCTO_NUEVO.replace("\"codigoCategoria\": \"notebooks\"", "\"codigoCategoria\": \"no-existe\"");
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigoCategoria").value("La categoría seleccionada no existe."));
    }

    @Test
    void actualizaUnProductoSinCambiarSuCodigo() throws Exception {
        String datos = PRODUCTO_NUEVO.replace("nb-test01", "OTRO-CODIGO").replace("Notebook de prueba", "Dock actualizado");
        mockMvc.perform(put("/api/productos/AC-HPDOCKG5").contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("AC-HPDOCKG5"))
                .andExpect(jsonPath("$.nombre").value("Dock actualizado"));
        assertFalse(productoRepository.existsByCodigo("OTRO-CODIGO"));
    }

    @Test
    void eliminaUnProductoSinVentas() throws Exception {
        mockMvc.perform(delete("/api/productos/AC-HPDOCKG5"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/productos/AC-HPDOCKG5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void noEliminaUnProductoConVentas() throws Exception {
        mockMvc.perform(delete("/api/productos/NB-HP250G10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("tiene ventas registradas")));
    }
}
