package cl.inforcore.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Lo que la API recibe para crear o editar una categoría. No trae código: el
 * backend lo genera a partir del nombre al crearla y después no cambia.
 */
public record CategoriaRequest(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50, message = "Máximo 50 caracteres.")
        String nombre,

        @Size(max = 200, message = "Máximo 200 caracteres.")
        String descripcion) {
}
