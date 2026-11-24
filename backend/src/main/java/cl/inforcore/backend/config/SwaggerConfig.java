package cl.inforcore.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Datos generales de la documentación Swagger (título, versión, descripción),
 * con @OpenAPIDefinition como en la diapositiva "Personalización adicional"
 * de la clase 3.1.1.
 *
 * Con springdoc-openapi no hace falta nada más: lee los controladores y genera
 * la documentación sola. Cada endpoint se describe en su controlador con
 * @Tag (grupo) y @Operation (qué hace).
 *
 * Interfaz:  http://localhost:8080/swagger-ui.html
 * JSON:      http://localhost:8080/v3/api-docs
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API REST INFORCORE",
        version = "1.0",
        description = "Backend de la tienda online INFORCORE (EP3 DSY1104, Grupo 3): catálogo de productos, "
                + "categorías, usuarios, órdenes de compra, regiones y blog."))
public class SwaggerConfig {
}
