package cl.inforcore.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Lo que la API RECIBE para crear o editar un producto (cuerpo JSON del POST y del PUT).
 *
 * Las anotaciones son validaciones de un solo campo (Bean Validation): Spring
 * las revisa antes de llamar al servicio cuando el controlador usa @Valid, y si
 * alguna falla responde 400 con el mensaje de cada campo. Son las mismas reglas
 * de la EP2. La regla que cruza dos campos (oferta menor que el precio) la
 * revisa el servicio.
 */
public record ProductoRequest(

        @NotBlank(message = "El código es obligatorio.")
        @Size(min = 3, max = 40, message = "El código debe tener entre 3 y 40 caracteres.")
        @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Solo letras, números y guiones (sin espacios).")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String nombre,

        @NotBlank(message = "Selecciona una categoría.")
        String codigoCategoria,

        @Size(max = 200, message = "Máximo 200 caracteres.")
        String especificaciones,

        @Size(max = 500, message = "Máximo 500 caracteres.")
        String descripcion,

        @NotNull(message = "El precio es obligatorio.")
        @Min(value = 0, message = "El precio no puede ser negativo.")
        Integer precio,

        @Min(value = 0, message = "Debe ser un número entero, mínimo 0.")
        Integer precioOferta,

        @NotNull(message = "El stock es obligatorio.")
        @Min(value = 0, message = "El stock debe ser un número entero, mínimo 0.")
        Integer stock,

        @Min(value = 0, message = "Debe ser un número entero, mínimo 0.")
        Integer stockCritico) {
}
