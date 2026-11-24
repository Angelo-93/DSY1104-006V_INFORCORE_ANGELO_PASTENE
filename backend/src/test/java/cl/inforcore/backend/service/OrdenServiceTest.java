package cl.inforcore.backend.service;

import cl.inforcore.backend.dto.NuevaOrdenRequest;
import cl.inforcore.backend.dto.OrdenDTO;
import cl.inforcore.backend.exception.DatosInvalidosException;
import cl.inforcore.backend.model.EstadoOrden;
import cl.inforcore.backend.repository.OrdenRepository;
import cl.inforcore.backend.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la lógica de la compra (checkout). Se prueba el servicio directo
 * porque el endpoint para pagar llega con la parte de seguridad: necesita saber
 * quién compra, y eso lo dirá el token JWT.
 */
@SpringBootTest
@Transactional
class OrdenServiceTest {

    private static final String FRANCISCA = "francisca.munoz@gmail.com";

    @Autowired
    private OrdenService ordenService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    private NuevaOrdenRequest pedido(boolean simularRechazo, NuevaOrdenRequest.Item... items) {
        return new NuevaOrdenRequest("Francisca", "Muñoz Díaz", FRANCISCA, "Av. San Martín 220", null,
                "Región de Valparaíso", "Viña del Mar", null, List.of(items), simularRechazo);
    }

    private NuevaOrdenRequest.Item item(String codigo, int cantidad) {
        return new NuevaOrdenRequest.Item(codigo, cantidad);
    }

    private int stock(String codigo) {
        return productoRepository.findByCodigo(codigo).orElseThrow().getStock();
    }

    @Test
    void unaCompraPagadaCobraElPrecioVigenteYDescuentaElStock() {
        OrdenDTO orden = ordenService.crear(FRANCISCA, pedido(false, item("MN-HPP24G5", 2)));

        assertEquals(EstadoOrden.PAGADA, orden.estado());
        assertTrue(orden.numero() > 1006, "La secuencia sigue después de las órdenes iniciales");
        assertEquals(84990, orden.items().get(0).precioUnitario(), "Se cobra el precio de oferta");
        assertEquals(2 * 84990, orden.total());
        assertEquals("201112222", orden.cliente().run(), "La orden queda ligada a la cuenta que compró");
        assertEquals(32 - 2, stock("MN-HPP24G5"));
    }

    @Test
    void sinStockSuficienteLaOrdenQuedaRechazadaYNoDescuentaNada() {
        OrdenDTO orden = ordenService.crear(FRANCISCA, pedido(false, item("NB-HP250G10", 1), item("AU-POLYP5", 1)));

        assertEquals(EstadoOrden.RECHAZADA, orden.estado());
        assertTrue(orden.motivoRechazo().contains("Poly Studio P5 (pediste 1, quedan 0)"));
        assertEquals(18, stock("NB-HP250G10"), "Todo o nada: tampoco se descuenta el producto que sí tenía stock");
        assertEquals(7, ordenRepository.count(), "La orden rechazada también queda registrada");
    }

    @Test
    void elRechazoSimuladoNoDescuentaStock() {
        OrdenDTO orden = ordenService.crear(FRANCISCA, pedido(true, item("NB-HP250G10", 1)));

        assertEquals(EstadoOrden.RECHAZADA, orden.estado());
        assertTrue(orden.motivoRechazo().contains("rechazo simulado"));
        assertEquals(18, stock("NB-HP250G10"));
    }

    @Test
    void comprarJustoElStockQueQuedaLoDejaEnCeroYCritico() {
        OrdenDTO orden = ordenService.crear(FRANCISCA, pedido(false, item("IM-HPLJM428FDW", 2)));

        assertEquals(EstadoOrden.PAGADA, orden.estado());
        assertEquals(0, stock("IM-HPLJM428FDW"));
        assertTrue(productoRepository.findByCodigo("IM-HPLJM428FDW").orElseThrow().esCritico());
    }

    @Test
    void lasLineasRepetidasDelMismoProductoSeSuman() {
        OrdenDTO orden = ordenService.crear(FRANCISCA, pedido(false, item("NB-HP250G10", 1), item("nb-hp250g10", 2)));

        assertEquals(1, orden.items().size());
        assertEquals(3, orden.items().get(0).cantidad());
        assertEquals(18 - 3, stock("NB-HP250G10"));
    }

    @Test
    void unProductoQueYaNoExisteSeRechazaComoDatoInvalido() {
        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> ordenService.crear(FRANCISCA, pedido(false, item("NO-EXISTE", 1))));
        assertEquals("items", error.getCampo());
    }

    @Test
    void laComunaDebePertenecerALaRegion() {
        NuevaOrdenRequest datos = new NuevaOrdenRequest("Francisca", "Muñoz Díaz", FRANCISCA, "Calle 1", null,
                "Región del Biobío", "Maipú", null, List.of(item("NB-HP250G10", 1)), false);
        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> ordenService.crear(FRANCISCA, datos));
        assertEquals("comuna", error.getCampo());
    }

    @Test
    void elCorreoDeLaBoletaDebeTenerUnDominioPermitido() {
        NuevaOrdenRequest datos = new NuevaOrdenRequest("Francisca", "Muñoz Díaz", "fran@hotmail.com", "Calle 1", null,
                "Región de Valparaíso", "Viña del Mar", null, List.of(item("NB-HP250G10", 1)), false);
        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> ordenService.crear(FRANCISCA, datos));
        assertEquals("correo", error.getCampo());
    }
}
