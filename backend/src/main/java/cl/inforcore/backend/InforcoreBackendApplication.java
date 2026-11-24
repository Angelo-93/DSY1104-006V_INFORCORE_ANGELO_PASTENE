package cl.inforcore.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del backend de INFORCORE (generado con Spring Initializr).
 *
 * Capas, como en clase: controller (recibe las peticiones HTTP) → service
 * (reglas de negocio) → repository (acceso a datos) → base de datos H2.
 */
@SpringBootApplication
public class InforcoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(InforcoreBackendApplication.class, args);
    }
}
