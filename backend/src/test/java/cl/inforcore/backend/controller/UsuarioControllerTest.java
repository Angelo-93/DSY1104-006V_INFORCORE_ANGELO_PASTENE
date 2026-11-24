package cl.inforcore.backend.controller;

import cl.inforcore.backend.model.Usuario;
import cl.inforcore.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Pruebas de integración de la API de usuarios (CRUD y reglas de negocio de la EP2). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerTest {

    /** Usuario válido. Cada prueba cambia solo el dato que quiere probar. */
    private static final String USUARIO_NUEVO = """
            {"run": "19011029k", "nombre": "Ana", "apellidos": "Pérez Soto", "correo": "Ana.Perez@Gmail.com",
             "contrasena": "clave1", "fechaNacimiento": "2000-05-10", "rol": "VENDEDOR",
             "region": "Región Metropolitana de Santiago", "comuna": "Maipú", "direccion": "Calle 123"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private org.springframework.test.web.servlet.ResultActions crear(String json) throws Exception {
        return mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void listaLosUsuariosSinContrasena() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[0].rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$[0].contrasena").doesNotExist());
    }

    @Test
    void obtieneUnUsuarioConSuRegionYComuna() throws Exception {
        mockMvc.perform(get("/api/usuarios/201112222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Francisca"))
                .andExpect(jsonPath("$.region").value("Región de Valparaíso"))
                .andExpect(jsonPath("$.comuna").value("Viña del Mar"));
    }

    @Test
    void creaUnUsuarioYGuardaLaContrasenaComoHash() throws Exception {
        crear(USUARIO_NUEVO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.run").value("19011029K"))
                .andExpect(jsonPath("$.correo").value("ana.perez@gmail.com"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());

        Usuario guardado = usuarioRepository.findByRun("19011029K").orElseThrow();
        assertNotEquals("clave1", guardado.getContrasena());
        assertTrue(passwordEncoder.matches("clave1", guardado.getContrasena()));
    }

    @Test
    void rechazaUnRunConDigitoVerificadorIncorrecto() throws Exception {
        crear(USUARIO_NUEVO.replace("19011029k", "190110290"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.run").value("El RUN no es válido. Verifica el dígito verificador."));
    }

    @Test
    void rechazaUnCorreoConDominioNoPermitido() throws Exception {
        crear(USUARIO_NUEVO.replace("Ana.Perez@Gmail.com", "ana@hotmail.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").value("Solo se aceptan correos @duoc.cl, @profesor.duoc.cl o @gmail.com."));
    }

    @Test
    void rechazaUnRunYaRegistrado() throws Exception {
        crear(USUARIO_NUEVO.replace("19011029k", "182345679"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errores.run").value("Ya existe un usuario con ese RUN."));
    }

    @Test
    void rechazaUnCorreoYaRegistradoSinImportarMayusculas() throws Exception {
        crear(USUARIO_NUEVO.replace("Ana.Perez@Gmail.com", "Francisca.Munoz@gmail.com"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errores.correo").value("Ese correo ya está registrado."));
    }

    @Test
    void alCrearLaContrasenaEsObligatoriaYDe4a10Caracteres() throws Exception {
        crear(USUARIO_NUEVO.replace("\"clave1\"", "\"\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.contrasena").value("La contraseña es obligatoria."));
        crear(USUARIO_NUEVO.replace("\"clave1\"", "\"abc\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.contrasena").value("La contraseña debe tener entre 4 y 10 caracteres."));
        crear(USUARIO_NUEVO.replace("\"clave1\"", "\"clave12345678\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.contrasena").value("La contraseña debe tener entre 4 y 10 caracteres."));
    }

    @Test
    void rechazaUnaComunaQueNoPerteneceALaRegion() throws Exception {
        crear(USUARIO_NUEVO.replace("Maipú", "Viña del Mar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.comuna").value("La comuna seleccionada no pertenece a la región."));
    }

    @Test
    void alCrearElRolEsObligatorioYDebeExistir() throws Exception {
        crear(USUARIO_NUEVO.replace("\"rol\": \"VENDEDOR\",", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.rol").value("Selecciona un tipo de usuario."));
        // Un rol inventado ni siquiera se puede leer: el enum solo acepta los tres roles.
        crear(USUARIO_NUEVO.replace("VENDEDOR", "SUPERADMIN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void alEditarSinContrasenaSeMantieneLaActual() throws Exception {
        String hashAntes = usuarioRepository.findByRun("156789011").orElseThrow().getContrasena();
        String datos = """
                {"run": "156789011", "nombre": "Tomás", "apellidos": "Herrera Vidal", "correo": "tomas.herrera@duoc.cl",
                 "contrasena": "", "fechaNacimiento": "", "region": "Región del Biobío", "comuna": "Concepción",
                 "direccion": "Nueva dirección 77"}
                """;
        mockMvc.perform(put("/api/usuarios/156789011").contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("Nueva dirección 77"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
        assertEquals(hashAntes, usuarioRepository.findByRun("156789011").orElseThrow().getContrasena());
    }

    @Test
    void noSePuedeQuitarElRolAlUnicoAdministrador() throws Exception {
        String datos = """
                {"run": "182345679", "nombre": "Camila", "apellidos": "Fuentes Rojas",
                 "correo": "camila.fuentes@profesor.duoc.cl", "rol": "CLIENTE",
                 "region": "Región Metropolitana de Santiago", "comuna": "Providencia", "direccion": "Av. Ejemplo 1234"}
                """;
        mockMvc.perform(put("/api/usuarios/182345679").contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errores.rol").value("No se puede quitar el rol al único Administrador."));
    }

    @Test
    void eliminaUnUsuarioSinCompras() throws Exception {
        mockMvc.perform(delete("/api/usuarios/210987657"))
                .andExpect(status().isNoContent());
        assertFalse(usuarioRepository.existsByRun("210987657"));
    }

    @Test
    void noEliminaUnUsuarioConCompras() throws Exception {
        mockMvc.perform(delete("/api/usuarios/201112222"))
                .andExpect(status().isConflict());
    }

    @Test
    void noEliminaAlUnicoAdministrador() throws Exception {
        mockMvc.perform(delete("/api/usuarios/182345679"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("No se puede eliminar al único Administrador."));
    }

    @Test
    void entregaElHistorialDeComprasDeUnUsuario() throws Exception {
        mockMvc.perform(get("/api/usuarios/201112222/ordenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].numero").value(1004))
                .andExpect(jsonPath("$[1].numero").value(1001));
    }
}
