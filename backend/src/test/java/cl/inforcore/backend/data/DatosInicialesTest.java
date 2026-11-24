package cl.inforcore.backend.data;

import cl.inforcore.backend.model.DetalleOrden;
import cl.inforcore.backend.model.Orden;
import cl.inforcore.backend.model.Producto;
import cl.inforcore.backend.model.Usuario;
import cl.inforcore.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la carga inicial: que los datos lleguen completos a la base y que
 * los casos especiales diseñados para la presentación existan de verdad.
 *
 * @SpringBootTest levanta la aplicación completa con la base en memoria de
 * src/test/resources. @Transactional: cada prueba se deshace al terminar, así
 * ninguna afecta a la siguiente.
 */
@SpringBootTest
@Transactional
class DatosInicialesTest {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ComunaRepository comunaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private DatosIniciales datosIniciales;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void cargaTodosLosDatosIniciales() {
        assertEquals(16, regionRepository.count());
        assertEquals(346, comunaRepository.count());
        assertEquals(7, categoriaRepository.count());
        assertEquals(14, productoRepository.count());
        assertEquals(7, usuarioRepository.count());
        assertEquals(2, blogRepository.count());
        assertEquals(6, ordenRepository.count());
    }

    @Test
    void lasOrdenesSeNumeranDesde1001() {
        List<Long> numeros = ordenRepository.findAll(Sort.by("id")).stream().map(Orden::getId).toList();
        assertEquals(List.of(1001L, 1002L, 1003L, 1004L, 1005L, 1006L), numeros);
    }

    @Test
    void lasContrasenasSeGuardanComoHashBCrypt() {
        Usuario camila = usuarioRepository.findByCorreoIgnoreCase("camila.fuentes@profesor.duoc.cl").orElseThrow();
        assertNotEquals("admin123", camila.getContrasena());
        assertTrue(camila.getContrasena().startsWith("$2"), "Todo hash BCrypt empieza con $2");
        assertTrue(passwordEncoder.matches("admin123", camila.getContrasena()));
    }

    @Test
    void losProductosCriticosSonLosDisenadosParaLaDemo() {
        List<String> codigos = productoRepository.findCriticos().stream().map(Producto::getCodigo).toList();
        // Ordenados por stock: agotado (0), bajo el crítico (2 < 3) y en el límite (4 = 4).
        // AC-HPDOCKG5 no aparece: no tiene stock crítico definido.
        assertEquals(List.of("AU-POLYP5", "IM-HPLJM428FDW", "NB-HPEB640G11"), codigos);
    }

    @Test
    void laBoletaGuardaElPrecioCobradoYNoElActual() {
        Orden orden = ordenRepository.findById(1002L).orElseThrow();
        DetalleOrden linea = orden.getItems().get(0);
        assertEquals(99990, linea.getPrecioUnitario());
        assertEquals(84990, linea.getProducto().getPrecioOferta());
        assertEquals(2 * 99990, orden.getTotal());
    }

    @Test
    void laRegionSeObtieneDesdeLaComuna() {
        Usuario tomas = usuarioRepository.findByRun("156789011").orElseThrow();
        assertEquals("Concepción", tomas.getComuna().getNombre());
        assertEquals("Región del Biobío", tomas.getComuna().getRegion().getNombre());
        assertNull(tomas.getFechaNacimiento(), "La fecha de nacimiento es opcional");
    }

    @Test
    void noDuplicaLosDatosSiSeEjecutaOtraVez() throws Exception {
        datosIniciales.run();
        assertEquals(14, productoRepository.count());
        assertEquals(7, usuarioRepository.count());
        assertEquals(6, ordenRepository.count());
    }
}
