package cl.inforcore.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Codificador de contraseñas. Se declara como @Bean para que Spring entregue
 * el mismo objeto a quien lo necesite: la carga inicial (hoy), el registro de
 * usuarios y el login (partes 2 y 3).
 */
@Configuration
public class PasswordConfig {

    /**
     * BCrypt convierte la contraseña en un hash de 60 caracteres que no se puede
     * revertir. Cada hash lleva una "sal" aleatoria (la misma contraseña da un
     * hash distinto cada vez) y el cálculo es lento a propósito, para que probar
     * millones de contraseñas por fuerza bruta sea muy costoso.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
