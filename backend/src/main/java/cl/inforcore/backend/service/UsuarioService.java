package cl.inforcore.backend.service;

import cl.inforcore.backend.dto.UsuarioDTO;
import cl.inforcore.backend.dto.UsuarioRequest;
import cl.inforcore.backend.exception.ConflictoException;
import cl.inforcore.backend.exception.DatosInvalidosException;
import cl.inforcore.backend.exception.RecursoNoEncontradoException;
import cl.inforcore.backend.model.Rol;
import cl.inforcore.backend.model.Usuario;
import cl.inforcore.backend.repository.OrdenRepository;
import cl.inforcore.backend.repository.UsuarioRepository;
import cl.inforcore.backend.util.Validaciones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

/**
 * Reglas de negocio de los usuarios (las de la EP2, ahora en el servidor):
 * - RUN válido con módulo 11, único y que no cambia (se guarda en mayúsculas).
 * - Correo con dominio permitido y único (se guarda en minúsculas).
 * - Contraseña de 4 a 10 caracteres, guardada siempre como hash BCrypt.
 * - Siempre debe quedar al menos un Administrador.
 * - Nuevo en la EP3: no se elimina un usuario que tiene compras (sus boletas se conservan).
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private RegionService regionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listar() {
        return usuarioRepository.findAllByOrderByIdAsc().stream().map(UsuarioDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtener(String run) {
        return UsuarioDTO.desde(buscar(run));
    }

    @Transactional
    public UsuarioDTO crear(UsuarioRequest datos) {
        String run = normalizarRun(datos.run());
        if (!Validaciones.runEsValido(run)) {
            throw new DatosInvalidosException("run", "El RUN no es válido. Verifica el dígito verificador.");
        }
        if (usuarioRepository.existsByRun(run)) {
            throw new ConflictoException("run", "Ya existe un usuario con ese RUN.");
        }
        if (datos.rol() == null) {
            throw new DatosInvalidosException("rol", "Selecciona un tipo de usuario.");
        }
        if (estaVacio(datos.contrasena())) {
            throw new DatosInvalidosException("contrasena", "La contraseña es obligatoria.");
        }

        Usuario usuario = new Usuario();
        usuario.setRun(run);
        usuario.setRol(datos.rol());
        aplicarDatos(usuario, datos, null);
        return UsuarioDTO.desde(usuarioRepository.save(usuario));
    }

    /**
     * El RUN no cambia (identifica al usuario y va en la URL): se toma el de la
     * URL. Si la contraseña viene vacía se mantiene la actual, y si no viene rol
     * se mantiene el actual.
     */
    @Transactional
    public UsuarioDTO actualizar(String run, UsuarioRequest datos) {
        Usuario usuario = buscar(run);
        Rol rolNuevo = datos.rol() != null ? datos.rol() : usuario.getRol();

        if (usuario.getRol() == Rol.ADMINISTRADOR && rolNuevo != Rol.ADMINISTRADOR
                && usuarioRepository.countByRol(Rol.ADMINISTRADOR) == 1) {
            throw new ConflictoException("rol", "No se puede quitar el rol al único Administrador.");
        }

        aplicarDatos(usuario, datos, usuario.getId());
        usuario.setRol(rolNuevo);
        return UsuarioDTO.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(String run) {
        Usuario usuario = buscar(run);
        // Debe quedar al menos un Administrador: si no, nadie podría volver a entrar al panel.
        if (usuario.getRol() == Rol.ADMINISTRADOR && usuarioRepository.countByRol(Rol.ADMINISTRADOR) == 1) {
            throw new ConflictoException("No se puede eliminar al único Administrador.");
        }
        if (ordenRepository.existsByUsuario(usuario)) {
            throw new ConflictoException("No se puede eliminar: el usuario tiene compras registradas "
                    + "y sus boletas deben conservarse.");
        }
        usuarioRepository.delete(usuario);
    }

    /**
     * Datos comunes de crear y editar. idExcluido: al editar, el propio usuario
     * no cuenta como "otro" que ya usa ese correo.
     */
    private void aplicarDatos(Usuario usuario, UsuarioRequest datos, Long idExcluido) {
        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);
        if (!Validaciones.correoTieneDominioPermitido(correo)) {
            throw new DatosInvalidosException("correo", Validaciones.MENSAJE_DOMINIO);
        }
        usuarioRepository.findByCorreoIgnoreCase(correo)
                .filter(otro -> !otro.getId().equals(idExcluido))
                .ifPresent(otro -> {
                    throw new ConflictoException("correo", "Ese correo ya está registrado.");
                });

        if (!estaVacio(datos.contrasena())) {
            if (datos.contrasena().length() < 4) {
                throw new DatosInvalidosException("contrasena", "La contraseña debe tener entre 4 y 10 caracteres.");
            }
            // Nunca se guarda la contraseña tal cual: solo su hash.
            usuario.setContrasena(passwordEncoder.encode(datos.contrasena()));
        }

        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setFechaNacimiento(convertirFecha(datos.fechaNacimiento()));
        usuario.setComuna(regionService.buscarComuna(datos.region(), datos.comuna()));
        usuario.setDireccion(datos.direccion().trim());
    }

    private Usuario buscar(String run) {
        return usuarioRepository.findByRun(normalizarRun(run))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con RUN " + run + "."));
    }

    /** Los RUN se guardan en mayúsculas: "19011029k" en una URL encuentra al mismo usuario. */
    private String normalizarRun(String run) {
        return run == null ? "" : run.trim().toUpperCase(Locale.ROOT);
    }

    /** "AAAA-MM-DD" a fecha; vacío queda en null (la fecha de nacimiento es opcional). */
    private LocalDate convertirFecha(String texto) {
        if (estaVacio(texto)) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new DatosInvalidosException("fechaNacimiento", "La fecha debe tener el formato AAAA-MM-DD.");
        }
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
