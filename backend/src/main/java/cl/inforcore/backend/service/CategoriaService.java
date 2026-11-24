package cl.inforcore.backend.service;

import cl.inforcore.backend.dto.CategoriaDTO;
import cl.inforcore.backend.dto.CategoriaRequest;
import cl.inforcore.backend.exception.ConflictoException;
import cl.inforcore.backend.exception.RecursoNoEncontradoException;
import cl.inforcore.backend.model.Categoria;
import cl.inforcore.backend.repository.CategoriaRepository;
import cl.inforcore.backend.repository.ProductoRepository;
import cl.inforcore.backend.util.Textos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reglas de negocio de las categorías (las mismas de la EP2):
 * - El nombre no se repite (sin importar mayúsculas, tildes ni espacios).
 * - El código se genera desde el nombre al crear y después no cambia.
 * - No se elimina una categoría que todavía tiene productos.
 */
@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar() {
        return categoriaRepository.findAllByOrderByIdAsc().stream()
                .map(categoria -> CategoriaDTO.desde(categoria, productoRepository.countByCategoria(categoria)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDTO obtener(String codigo) {
        Categoria categoria = buscar(codigo);
        return CategoriaDTO.desde(categoria, productoRepository.countByCategoria(categoria));
    }

    @Transactional
    public CategoriaDTO crear(CategoriaRequest datos) {
        String nombre = datos.nombre().trim();
        if (nombreRepetido(nombre, null)) {
            throw new ConflictoException("nombre", "Ya existe la categoría \"" + nombre + "\".");
        }
        Categoria categoria = new Categoria(generarCodigoUnico(nombre), nombre, Textos.limpiarOpcional(datos.descripcion()));
        return CategoriaDTO.desde(categoriaRepository.save(categoria), 0);
    }

    /**
     * Cambia nombre y descripción. El código se mantiene: los productos y la URL
     * de la categoría siguen funcionando aunque cambie el nombre.
     */
    @Transactional
    public CategoriaDTO actualizar(String codigo, CategoriaRequest datos) {
        Categoria categoria = buscar(codigo);
        String nombre = datos.nombre().trim();
        if (nombreRepetido(nombre, categoria.getId())) {
            throw new ConflictoException("nombre", "Ya existe la categoría \"" + nombre + "\".");
        }
        categoria.setNombre(nombre);
        categoria.setDescripcion(Textos.limpiarOpcional(datos.descripcion()));
        Categoria guardada = categoriaRepository.save(categoria);
        return CategoriaDTO.desde(guardada, productoRepository.countByCategoria(guardada));
    }

    /**
     * Regla de negocio: no se elimina una categoría con productos, porque esos
     * productos quedarían sin categoría (y es obligatoria). La base también lo
     * impide con la FK; aquí se revisa antes para responder un mensaje claro.
     */
    @Transactional
    public void eliminar(String codigo) {
        Categoria categoria = buscar(codigo);
        long cantidad = productoRepository.countByCategoria(categoria);
        if (cantidad > 0) {
            throw new ConflictoException("No se puede eliminar: la categoría tiene "
                    + Textos.pluralizar(cantidad, "producto", "productos") + ". Muévelos a otra categoría primero.");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscar(String codigo) {
        return categoriaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoría " + codigo + "."));
    }

    /** "Monitores" y "monitores " son la misma categoría para una persona. */
    private boolean nombreRepetido(String nombre, Long idExcluido) {
        String buscado = Textos.normalizar(nombre);
        return categoriaRepository.findAll().stream()
                .anyMatch(c -> !c.getId().equals(idExcluido) && Textos.normalizar(c.getNombre()).equals(buscado));
    }

    /** El código sale del nombre ("Redes y WiFi" da "redes-y-wifi"); si ya existe, se le agrega un número. */
    private String generarCodigoUnico(String nombre) {
        String base = Textos.generarSlug(nombre);
        if (base.isEmpty()) {
            base = "categoria";
        }
        String codigo = base;
        int contador = 2;
        while (categoriaRepository.existsByCodigo(codigo)) {
            codigo = base + "-" + contador;
            contador++;
        }
        return codigo;
    }
}
