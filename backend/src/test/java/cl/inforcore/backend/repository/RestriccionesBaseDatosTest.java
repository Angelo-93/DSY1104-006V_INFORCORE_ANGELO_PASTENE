package cl.inforcore.backend.repository;

import cl.inforcore.backend.model.Categoria;
import cl.inforcore.backend.model.Producto;
import cl.inforcore.backend.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del modelo de datos: las claves foráneas y los campos únicos los hace
 * cumplir la propia base de datos, aunque alguien se salte el servicio.
 *
 * flush() obliga a Hibernate a enviar el SQL en ese momento; sin él, el error
 * aparecería recién al final de la transacción y no dentro de la prueba.
 */
@SpringBootTest
@Transactional
class RestriccionesBaseDatosTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void noSeEliminaUnaCategoriaQueTieneProductos() {
        Categoria notebooks = categoriaRepository.findByCodigo("notebooks").orElseThrow();
        assertThrows(DataIntegrityViolationException.class, () -> {
            categoriaRepository.delete(notebooks);
            categoriaRepository.flush();
        });
    }

    @Test
    void unaCategoriaSinProductosSiSeElimina() {
        Categoria servidores = categoriaRepository.findByCodigo("servidores").orElseThrow();
        categoriaRepository.delete(servidores);
        categoriaRepository.flush();
        assertFalse(categoriaRepository.existsByCodigo("servidores"));
    }

    @Test
    void noSeEliminaUnProductoQueTieneVentas() {
        Producto vendido = productoRepository.findByCodigo("NB-HP250G10").orElseThrow();
        assertThrows(DataIntegrityViolationException.class, () -> {
            productoRepository.delete(vendido);
            productoRepository.flush();
        });
    }

    @Test
    void elCodigoDeProductoNoSePuedeRepetir() {
        Producto copia = new Producto();
        copia.setCodigo("NB-HP250G10");
        copia.setNombre("Copia");
        copia.setPrecio(1000);
        copia.setStock(1);
        copia.setCategoria(categoriaRepository.findByCodigo("notebooks").orElseThrow());
        assertThrows(DataIntegrityViolationException.class, () -> productoRepository.saveAndFlush(copia));
    }

    @Test
    void elCorreoDeUsuarioNoSePuedeRepetir() {
        Usuario camila = usuarioRepository.findByRun("182345679").orElseThrow();
        Usuario copia = new Usuario();
        copia.setRun("111111111");
        copia.setNombre("Copia");
        copia.setApellidos("De Prueba");
        copia.setCorreo(camila.getCorreo());
        copia.setContrasena(camila.getContrasena());
        copia.setRol(camila.getRol());
        copia.setComuna(camila.getComuna());
        copia.setDireccion("Calle 1");
        assertThrows(DataIntegrityViolationException.class, () -> usuarioRepository.saveAndFlush(copia));
    }
}
