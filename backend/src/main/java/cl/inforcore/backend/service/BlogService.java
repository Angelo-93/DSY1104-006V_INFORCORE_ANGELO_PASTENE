package cl.inforcore.backend.service;

import cl.inforcore.backend.dto.BlogDTO;
import cl.inforcore.backend.exception.RecursoNoEncontradoException;
import cl.inforcore.backend.repository.BlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Artículos del blog: solo lectura. */
@Service
public class BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Transactional(readOnly = true)
    public List<BlogDTO> listar() {
        return blogRepository.findAllByOrderByIdAsc().stream().map(BlogDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public BlogDTO obtener(String codigo) {
        return blogRepository.findByCodigo(codigo)
                .map(BlogDTO::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el artículo " + codigo + "."));
    }
}
