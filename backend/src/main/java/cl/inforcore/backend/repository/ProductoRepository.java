package cl.inforcore.backend.repository;

import cl.inforcore.backend.model.Categoria;
import cl.inforcore.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    /** Para la regla "no se elimina una categoría que tiene productos". */
    boolean existsByCategoria(Categoria categoria);

    List<Producto> findAllByOrderByIdAsc();

    /** "CategoriaCodigo" navega la relación: producto.categoria.codigo. */
    List<Producto> findByCategoriaCodigoOrderByIdAsc(String codigoCategoria);

    /** Productos en oferta: los que tienen precio de oferta. */
    List<Producto> findByPrecioOfertaIsNotNullOrderByIdAsc();

    /**
     * Productos críticos: tienen stock crítico definido y su stock es igual o
     * menor. Aquí se usa @Query (JPQL) porque hay que comparar DOS columnas
     * entre sí (stock <= stockCritico), y eso no se puede expresar solo con el
     * nombre del método. JPQL se escribe con las clases y atributos de Java,
     * no con los nombres de las tablas.
     */
    @Query("select p from Producto p where p.stockCritico is not null and p.stock <= p.stockCritico order by p.stock asc")
    List<Producto> findCriticos();
}
