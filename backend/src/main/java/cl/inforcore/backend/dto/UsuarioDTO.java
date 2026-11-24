package cl.inforcore.backend.dto;

import cl.inforcore.backend.model.Rol;
import cl.inforcore.backend.model.Usuario;

import java.time.LocalDate;

/**
 * Lo que la API entrega de un usuario. NO incluye la contraseña (ni siquiera
 * su hash): es justamente para esto que existen los DTO (clase del 10-11).
 * Si se enviara la entidad Usuario completa, el hash viajaría por la red.
 */
public record UsuarioDTO(
        String run,
        String nombre,
        String apellidos,
        String correo,
        LocalDate fechaNacimiento,
        Rol rol,
        String region,
        String comuna,
        String direccion) {

    public static UsuarioDTO desde(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getRun(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getFechaNacimiento(),
                usuario.getRol(),
                usuario.getComuna().getRegion().getNombre(),
                usuario.getComuna().getNombre(),
                usuario.getDireccion());
    }
}
