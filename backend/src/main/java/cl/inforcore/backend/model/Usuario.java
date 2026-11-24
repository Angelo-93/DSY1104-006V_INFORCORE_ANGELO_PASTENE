package cl.inforcore.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Usuario del sistema (Administrador, Vendedor o Cliente).
 *
 * Esta entidad NUNCA se envía tal cual al frontend: tiene el hash de la
 * contraseña. La API responde con un DTO que deja fuera ese dato (clase del 10-11).
 */
@Entity
@Table(name = "inforcore_usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** RUN sin puntos ni guion (ej: "182345679"). Único: identifica a la persona. */
    @Column(nullable = false, unique = true, length = 9)
    private String run;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellidos;

    /** Único: es el dato con que se inicia sesión. */
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    /**
     * Hash BCrypt de la contraseña (siempre 60 caracteres), nunca el texto
     * plano: quien lea la tabla no obtiene las contraseñas (clase del 09-11).
     */
    @Column(nullable = false, length = 60)
    private String contrasena;

    /** Opcional, como en la EP2. */
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    // FK comuna_id. La región no se guarda aparte: sale de comuna.getRegion().
    @ManyToOne
    @JoinColumn(name = "comuna_id", nullable = false)
    private Comuna comuna;

    @Column(nullable = false, length = 300)
    private String direccion;
}
