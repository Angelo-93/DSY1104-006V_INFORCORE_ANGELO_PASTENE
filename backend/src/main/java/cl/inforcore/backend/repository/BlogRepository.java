package cl.inforcore.backend.repository;

import cl.inforcore.backend.model.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    Optional<Blog> findByCodigo(String codigo);

    List<Blog> findAllByOrderByIdAsc();
}
