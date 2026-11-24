package cl.inforcore.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Lo que la API recibe al pagar el carrito (checkout): datos de entrega y las
 * líneas del carrito.
 *
 * De cada línea llegan SOLO el código y la cantidad. El precio no se recibe: el
 * carrito vive en el navegador y cualquiera podría editarlo; el backend toma el
 * precio del catálogo en el momento de pagar.
 *
 * simularRechazo: el selector "Simulación del medio de pago" del checkout de la
 * EP2, para poder mostrar un pago rechazado en la presentación.
 */
public record NuevaOrdenRequest(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50, message = "Máximo 50 caracteres.")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String correo,

        @NotBlank(message = "La calle es obligatoria.")
        @Size(max = 300, message = "Máximo 300 caracteres.")
        String calle,

        @Size(max = 50, message = "Máximo 50 caracteres.")
        String departamento,

        @NotBlank(message = "Selecciona una región.")
        String region,

        @NotBlank(message = "Selecciona una comuna.")
        String comuna,

        @Size(max = 300, message = "Máximo 300 caracteres.")
        String indicaciones,

        // @Valid: también se validan las anotaciones de cada línea.
        @NotEmpty(message = "El carrito está vacío.")
        @Valid
        List<Item> items,

        boolean simularRechazo) {

    /** Una línea del carrito: qué producto y cuántas unidades. */
    public record Item(

            @NotBlank(message = "Falta el código del producto.")
            String codigo,

            @NotNull(message = "Falta la cantidad.")
            @Min(value = 1, message = "La cantidad mínima es 1.")
            Integer cantidad) {
    }
}
