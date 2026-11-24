package cl.inforcore.backend.dto;

import cl.inforcore.backend.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Lo que la API recibe para crear o editar un usuario desde el panel.
 *
 * - contrasena: obligatoria al crear; al editar, vacía significa "no cambiarla"
 *   (el administrador no conoce las contraseñas). Por eso aquí solo se limita el
 *   largo máximo y el servicio revisa el resto.
 * - fechaNacimiento: texto "AAAA-MM-DD" o vacío (es opcional).
 * - rol: obligatorio al crear; al editar, si no viene se mantiene el actual.
 */
public record UsuarioRequest(

        @NotBlank(message = "El RUN es obligatorio.")
        @Size(min = 7, max = 9, message = "Debe tener entre 7 y 9 caracteres, sin puntos ni guion.")
        String run,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50, message = "Máximo 50 caracteres.")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String correo,

        @Size(max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres.")
        String contrasena,

        String fechaNacimiento,

        Rol rol,

        @NotBlank(message = "Selecciona una región.")
        String region,

        @NotBlank(message = "Selecciona una comuna.")
        String comuna,

        @NotBlank(message = "La dirección es obligatoria.")
        @Size(max = 300, message = "Máximo 300 caracteres.")
        String direccion) {
}
