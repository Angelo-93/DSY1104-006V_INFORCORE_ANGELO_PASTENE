package cl.inforcore.backend.data;

import cl.inforcore.backend.model.*;
import cl.inforcore.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Carga inicial de datos (datos semilla). Reemplaza a los arreglos de
 * frontend/src/data de la EP2: ahora los datos viven en la base de datos y el
 * frontend los pide a la API.
 *
 * Al ser un CommandLineRunner, Spring ejecuta run() apenas termina de arrancar.
 * Solo carga si la base está vacía: como H2 está en modo archivo, al reiniciar
 * el servidor los datos siguen ahí y no se duplican. Para volver al estado
 * inicial basta con detener el servidor y borrar la carpeta backend/data.
 *
 * Los datos están armados a propósito para que cada caso especial exista y se
 * pueda mostrar en la presentación sin inventarlo:
 *
 * - NB-HPEB640G11: stock crítico justo en el límite (stock 4 = crítico 4).
 * - IM-HPLJM428FDW: stock bajo el crítico (2 menor que 3).
 * - AU-POLYP5: producto agotado (stock 0).
 * - AC-HPDOCKG5: sin stock crítico (nunca es crítico) y sin ventas (se puede eliminar).
 * - NB-HP240G10, MN-HPP24G5, AU-POLYSYNC20 y AC-HP235COMBO: productos en oferta.
 * - Categoría Servidores: sin productos, es la única que se puede eliminar.
 * - Valentina Reyes: cliente sin compras, se puede eliminar y su historial está vacío.
 * - Tomás Herrera: sin fecha de nacimiento (campo opcional).
 * - Orden 1002: se cobró 99.990 por el monitor, que hoy está en oferta a 84.990
 *   (la boleta guarda el precio que se cobró, no el actual).
 * - Orden 1005: pago rechazado.
 * - Orden 1006: varias unidades de dos productos.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ComunaRepository comunaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Una línea de orden de la carga: qué producto, a qué precio se cobró y cuántas unidades. */
    private record Linea(Producto producto, int precioUnitario, int cantidad) {
    }

    /**
     * @Transactional: toda la carga es una sola transacción. Si algo falla a la
     * mitad, se deshace todo y la base no queda con datos a medias.
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("La base de datos ya tiene datos: se omite la carga inicial.");
            return;
        }
        cargarRegiones();
        Map<String, Categoria> categorias = cargarCategorias();
        Map<String, Producto> productos = cargarProductos(categorias);
        Map<String, Usuario> usuarios = cargarUsuarios();
        cargarBlogs();
        cargarOrdenes(usuarios, productos);

        log.info("Carga inicial lista: {} regiones, {} comunas, {} categorías, {} productos, {} usuarios, {} blogs, {} órdenes.",
                regionRepository.count(), comunaRepository.count(), categoriaRepository.count(),
                productoRepository.count(), usuarioRepository.count(), blogRepository.count(),
                ordenRepository.count());
    }

    /* ---------------------------------------------------------------------- */
    /* Regiones y comunas                                                      */
    /* ---------------------------------------------------------------------- */

    private void cargarRegiones() {
        RegionesChile.regionesConComunas().forEach((nombreRegion, nombresComunas) -> {
            Region region = new Region(nombreRegion);
            nombresComunas.forEach(nombreComuna -> region.agregarComuna(new Comuna(nombreComuna)));
            // Una sola llamada guarda la región y sus comunas (CascadeType.ALL en Region).
            regionRepository.save(region);
        });
    }

    /* ---------------------------------------------------------------------- */
    /* Categorías y productos                                                  */
    /* ---------------------------------------------------------------------- */

    private Map<String, Categoria> cargarCategorias() {
        List<Categoria> categorias = List.of(
                new Categoria("notebooks", "Notebooks",
                        "Equipos portátiles para oficina, ejecutivos y aulas."),
                new Categoria("desktops-y-aio", "Desktops y AIO",
                        "Torres, equipos compactos y All-in-One para puestos fijos y laboratorios."),
                new Categoria("monitores", "Monitores",
                        "Pantallas Full HD y Quad HD para productividad y análisis."),
                new Categoria("audio-y-videoconferencia", "Audio y Videoconferencia",
                        "Speakerphones y cámaras para reuniones híbridas."),
                new Categoria("impresion", "Impresión",
                        "Impresoras láser y multifuncionales para volúmenes de oficina."),
                new Categoria("accesorios", "Accesorios",
                        "Docks, teclados y mouse para completar cada puesto de trabajo."),
                // Nueva en la EP3, sin productos a propósito: es la que permite
                // mostrar que una categoría vacía sí se puede eliminar.
                new Categoria("servidores", "Servidores",
                        "Servidores de torre y rack para pequeñas y medianas empresas."));

        // Se devuelven en un mapa por código para armar los productos sin buscar en la base.
        Map<String, Categoria> porCodigo = new LinkedHashMap<>();
        categoriaRepository.saveAll(categorias).forEach(c -> porCodigo.put(c.getCodigo(), c));
        return porCodigo;
    }

    private Map<String, Producto> cargarProductos(Map<String, Categoria> c) {
        List<Producto> productos = List.of(
                producto("NB-HP250G10", "HP 250 G10", c.get("notebooks"),
                        "Intel Core i5 · 16 GB RAM · 512 GB SSD · 15.6\" FHD",
                        "Notebook empresarial liviano, pensado para trabajo de oficina diario con buena autonomía de batería.",
                        549990, null, 18, 5),
                producto("NB-HP240G10", "HP 240 G10", c.get("notebooks"),
                        "Intel Core i3 · 8 GB RAM · 256 GB SSD · 14\" HD",
                        "Notebook de entrada ideal para tareas administrativas y uso educativo básico.",
                        399990, 349990, 24, 6),
                producto("NB-HPPB440G11", "HP ProBook 440 G11", c.get("notebooks"),
                        "Intel Core i7 · 16 GB RAM · 512 GB SSD · 14\" FHD",
                        "Notebook profesional de gama media-alta, orientado a ejecutivos y equipos técnicos.",
                        899990, null, 9, 4),
                producto("NB-HPEB640G11", "HP EliteBook 640 G11", c.get("notebooks"),
                        "Intel Core i7 · 32 GB RAM · 1 TB SSD · 14\" FHD",
                        "Notebook premium para cargas de trabajo exigentes, con seguridad y gestión de nivel empresarial.",
                        1190000, null, 4, 4),
                producto("DT-HPPD400G9", "HP ProDesk 400 G9", c.get("desktops-y-aio"),
                        "Intel Core i5 · 16 GB RAM · 512 GB SSD · Torre",
                        "Equipo de escritorio compacto para estaciones de trabajo corporativas o laboratorios.",
                        479990, null, 14, 5),
                producto("AIO-HPPO440G9", "HP ProOne 440 G9 AIO", c.get("desktops-y-aio"),
                        "Intel Core i5 · 16 GB RAM · 512 GB SSD · 23.8\" FHD",
                        "All-in-One que integra pantalla y equipo en un solo cuerpo, ideal para espacios reducidos.",
                        649990, null, 7, 3),
                producto("MN-HPP24G5", "Monitor HP P24 G5", c.get("monitores"),
                        "23.8\" · Full HD · Panel IPS",
                        "Monitor de oficina con panel IPS para buena reproducción de color y ángulos de visión amplios.",
                        99990, 84990, 32, 8),
                producto("MN-HPE27G5", "Monitor HP E27 G5", c.get("monitores"),
                        "27\" · Quad HD · Panel IPS",
                        "Monitor de mayor resolución para tareas de diseño, análisis de datos y multitarea.",
                        189990, null, 11, 4),
                producto("AU-POLYSYNC20", "Poly Sync 20", c.get("audio-y-videoconferencia"),
                        "Speakerphone USB/Bluetooth · Cancelación de ruido",
                        "Speakerphone portátil para salas de reunión pequeñas y videollamadas individuales.",
                        89990, 74990, 20, 6),
                producto("AU-POLYP5", "Poly Studio P5", c.get("audio-y-videoconferencia"),
                        "Webcam Full HD · Micrófono integrado",
                        "Cámara web profesional con corrección automática de luz, pensada para videoconferencias.",
                        69990, null, 0, 6),
                producto("IM-HPLJM404DN", "HP LaserJet Pro M404dn", c.get("impresion"),
                        "Láser monocromática · Dúplex automático · Red",
                        "Impresora láser de alto rendimiento para volúmenes de impresión medios en oficina.",
                        259990, null, 10, 3),
                producto("IM-HPLJM428FDW", "HP LaserJet Pro MFP M428fdw", c.get("impresion"),
                        "Multifuncional láser · Wi-Fi · Escáner dúplex",
                        "Equipo multifuncional que imprime, escanea y copia, con conectividad inalámbrica.",
                        389990, null, 2, 3),
                producto("AC-HPDOCKG5", "HP USB-C Dock G5", c.get("accesorios"),
                        "Docking station · USB-C · Múltiples salidas de video",
                        "Estación de conexión que expande los puertos del notebook para trabajar con doble monitor.",
                        79990, null, 15, null),
                producto("AC-HP235COMBO", "HP 235 Wireless Combo", c.get("accesorios"),
                        "Teclado y mouse inalámbricos",
                        "Combo inalámbrico de teclado y mouse para equipar puestos de trabajo de forma económica.",
                        24990, 19990, 40, 10));

        Map<String, Producto> porCodigo = new LinkedHashMap<>();
        productoRepository.saveAll(productos).forEach(p -> porCodigo.put(p.getCodigo(), p));
        return porCodigo;
    }

    private Producto producto(String codigo, String nombre, Categoria categoria, String especificaciones,
                              String descripcion, int precio, Integer precioOferta, int stock,
                              Integer stockCritico) {
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setEspecificaciones(especificaciones);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setPrecioOferta(precioOferta);
        producto.setStock(stock);
        producto.setStockCritico(stockCritico);
        return producto;
    }

    /* ---------------------------------------------------------------------- */
    /* Usuarios                                                                */
    /* ---------------------------------------------------------------------- */

    /**
     * Los mismos usuarios de la EP2 (datos ficticios: el repositorio es público).
     * Nuevos en la EP3: Javiera y Rodrigo, que en la EP2 compraban como invitados
     * (ahora comprar exige cuenta), y Valentina, una clienta sin compras.
     */
    private Map<String, Usuario> cargarUsuarios() {
        List<Usuario> usuarios = List.of(
                usuario("182345679", "Camila", "Fuentes Rojas", "camila.fuentes@profesor.duoc.cl", "admin123",
                        "1990-04-12", Rol.ADMINISTRADOR,
                        "Región Metropolitana de Santiago", "Providencia", "Av. Ejemplo 1234, Of. 56"),
                usuario("167890121", "Matías", "Soto Lagos", "matias.soto@duoc.cl", "vende123",
                        "1995-08-23", Rol.VENDEDOR,
                        "Región Metropolitana de Santiago", "Ñuñoa", "Los Leones 456"),
                usuario("201112222", "Francisca", "Muñoz Díaz", "francisca.munoz@gmail.com", "cliente1",
                        "2001-01-30", Rol.CLIENTE,
                        "Región de Valparaíso", "Viña del Mar", "Av. San Martín 220"),
                usuario("156789011", "Tomás", "Herrera Vidal", "tomas.herrera@duoc.cl", "cliente2",
                        null, Rol.CLIENTE,
                        "Región del Biobío", "Concepción", "Barros Arana 890"),
                usuario("204567891", "Javiera", "Rojas Pinto", "javiera.rojas@gmail.com", "cliente3",
                        "1998-06-15", Rol.CLIENTE,
                        "Región Metropolitana de Santiago", "Maipú", "Av. Pajaritos 1450"),
                usuario("135792462", "Rodrigo", "Paredes Soto", "rodrigo.paredes@profesor.duoc.cl", "cliente4",
                        "1985-11-03", Rol.CLIENTE,
                        "Región Metropolitana de Santiago", "San Joaquín", "Av. Ejemplo 500"),
                usuario("210987657", "Valentina", "Reyes Castro", "valentina.reyes@gmail.com", "cliente5",
                        "2003-03-21", Rol.CLIENTE,
                        "Región de Los Lagos", "Puerto Montt", "Av. Diego Portales 1200"));

        Map<String, Usuario> porCorreo = new LinkedHashMap<>();
        usuarioRepository.saveAll(usuarios).forEach(u -> porCorreo.put(u.getCorreo(), u));
        return porCorreo;
    }

    private Usuario usuario(String run, String nombre, String apellidos, String correo, String contrasena,
                            String fechaNacimiento, Rol rol, String region, String comuna, String direccion) {
        Usuario usuario = new Usuario();
        usuario.setRun(run);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        // Se guarda el hash, nunca la contraseña tal cual (ver PasswordConfig).
        usuario.setContrasena(passwordEncoder.encode(contrasena));
        usuario.setFechaNacimiento(fechaNacimiento == null ? null : LocalDate.parse(fechaNacimiento));
        usuario.setRol(rol);
        usuario.setComuna(buscarComuna(region, comuna));
        usuario.setDireccion(direccion);
        return usuario;
    }

    private Comuna buscarComuna(String region, String comuna) {
        return comunaRepository.findByNombreAndRegionNombre(comuna, region)
                .orElseThrow(() -> new IllegalStateException(
                        "Comuna no encontrada en la carga inicial: " + comuna + " (" + region + ")"));
    }

    /* ---------------------------------------------------------------------- */
    /* Blogs                                                                   */
    /* ---------------------------------------------------------------------- */

    private void cargarBlogs() {
        blogRepository.saveAll(List.of(
                blog("renovar-equipos", "Caso curioso #1",
                        "5 señales de que es hora de renovar los equipos de tu empresa",
                        "Rendimiento lento, fallas frecuentes y software que ya no corre bien son más caros de lo que parecen. Te contamos qué mirar antes de decidir.",
                        "notebooks",
                        "Un notebook que ya tiene varios años no siempre avisa que está fallando con una pantalla azul. Muchas veces avisa con algo más silencioso: minutos perdidos cada mañana esperando que arranque, reuniones que se atrasan porque el equipo se congela, o un técnico que visita la oficina cada cierto tiempo para \"dejarlo andando de nuevo\".",
                        "Estas son cinco señales concretas que conviene revisar: tiempos de carga que superan varios minutos, batería que no dura ni la mitad de una jornada, software de trabajo que ya no es compatible con el sistema operativo instalado, ventiladores que hacen ruido constante por acumulación de polvo interno, y soporte técnico del fabricante que ya no está disponible porque el modelo salió de garantía hace tiempo.",
                        "Ninguna de estas señales por sí sola obliga a comprar equipos nuevos de inmediato, pero cuando aparecen dos o tres juntas, el costo real ya no es el precio del notebook: es el tiempo del equipo completo esperando a que la máquina responda. En INFORCORE podemos revisar el parque de equipos de tu empresa y proponer una renovación por etapas, priorizando los puestos donde el impacto es mayor."),
                blog("laboratorio-escolar", "Caso curioso #2",
                        "Cómo equipar un laboratorio de computación escolar sin pasarte del presupuesto",
                        "Priorizar durabilidad, garantía y soporte por sobre el precio más bajo suele salir más barato en el tiempo. Revisamos un caso real.",
                        "desktops-y-aio",
                        "Cuando un colegio necesita renovar su laboratorio de computación, la tentación habitual es buscar el equipo más barato disponible y comprar la mayor cantidad posible con el presupuesto asignado. El problema aparece uno o dos años después, cuando varios equipos empiezan a fallar al mismo tiempo, justo cuando ya no queda presupuesto para reemplazarlos.",
                        "Un colegio que trabajó con nosotros el año pasado decidió cambiar el enfoque: en vez de maximizar la cantidad de equipos, priorizó desktops con garantía extendida y soporte técnico incluido, aunque eso significó comprar algunos equipos menos de los que tenía presupuestados en un principio. El resultado fue un laboratorio que se mantuvo operativo durante todo el año escolar, sin interrupciones de clases por equipos en reparación.",
                        "La recomendación práctica es simple: define primero cuántos puestos de trabajo son realmente indispensables, y con lo que sobre del presupuesto, prioriza garantía y soporte antes que unidades adicionales. Un laboratorio con menos equipos pero todos funcionando es más útil que uno completo con la mitad de las máquinas en el taller.")));
    }

    private Blog blog(String codigo, String etiqueta, String titulo, String resumen,
                      String categoriaIlustracion, String... parrafos) {
        Blog blog = new Blog();
        blog.setCodigo(codigo);
        blog.setEtiqueta(etiqueta);
        blog.setTitulo(titulo);
        blog.setResumen(resumen);
        blog.setCategoriaIlustracion(categoriaIlustracion);
        // Los párrafos se guardan en una sola columna, separados por una línea en blanco.
        blog.setContenido(String.join("\n\n", parrafos));
        return blog;
    }

    /* ---------------------------------------------------------------------- */
    /* Órdenes                                                                 */
    /* ---------------------------------------------------------------------- */

    /**
     * Las seis órdenes de la EP2, en orden cronológico para que la secuencia
     * les asigne los números 1001 a 1006. Las fechas llevan la zona horaria de
     * Chile: -04:00 hasta el cambio de hora de septiembre y -03:00 después.
     */
    private void cargarOrdenes(Map<String, Usuario> u, Map<String, Producto> p) {
        Usuario francisca = u.get("francisca.munoz@gmail.com");
        Usuario tomas = u.get("tomas.herrera@duoc.cl");
        Usuario javiera = u.get("javiera.rojas@gmail.com");
        Usuario rodrigo = u.get("rodrigo.paredes@profesor.duoc.cl");

        orden(francisca, "2026-08-21T10:15:00-04:00", EstadoOrden.PAGADA, null,
                "Av. San Martín 220", null, "Región de Valparaíso", "Viña del Mar", null,
                new Linea(p.get("NB-HP250G10"), 549990, 1),
                new Linea(p.get("AC-HP235COMBO"), 24990, 1));

        orden(tomas, "2026-09-03T18:40:00-04:00", EstadoOrden.PAGADA, null,
                "Barros Arana 890", "Depto 42", "Región del Biobío", "Concepción", "Dejar en conserjería.",
                new Linea(p.get("MN-HPP24G5"), 99990, 2));

        orden(javiera, "2026-09-12T12:05:00-03:00", EstadoOrden.PAGADA, null,
                "Av. Pajaritos 1450", null, "Región Metropolitana de Santiago", "Maipú", null,
                new Linea(p.get("IM-HPLJM404DN"), 259990, 1));

        orden(francisca, "2026-09-26T09:30:00-03:00", EstadoOrden.PAGADA, null,
                "Av. San Martín 220", null, "Región de Valparaíso", "Viña del Mar", null,
                new Linea(p.get("AU-POLYSYNC20"), 74990, 1));

        orden(tomas, "2026-10-02T20:10:00-03:00", EstadoOrden.RECHAZADA,
                "El medio de pago rechazó la transacción.",
                "Barros Arana 890", "Depto 42", "Región del Biobío", "Concepción", "Dejar en conserjería.",
                new Linea(p.get("NB-HPEB640G11"), 1190000, 1));

        orden(rodrigo, "2026-10-05T15:45:00-03:00", EstadoOrden.PAGADA, null,
                "Av. Ejemplo 500", null, "Región Metropolitana de Santiago", "San Joaquín",
                "Entregar en recepción del laboratorio.",
                new Linea(p.get("DT-HPPD400G9"), 479990, 3),
                new Linea(p.get("MN-HPE27G5"), 189990, 3));
    }

    private void orden(Usuario cliente, String fecha, EstadoOrden estado, String motivoRechazo,
                       String calle, String departamento, String region, String comuna,
                       String indicaciones, Linea... lineas) {
        Orden orden = new Orden();
        orden.setFecha(OffsetDateTime.parse(fecha));
        orden.setEstado(estado);
        orden.setMotivoRechazo(motivoRechazo);
        orden.setUsuario(cliente);
        orden.setNombreCliente(cliente.getNombre());
        orden.setApellidosCliente(cliente.getApellidos());
        orden.setCorreoCliente(cliente.getCorreo());
        orden.setCalle(calle);
        orden.setDepartamento(departamento);
        orden.setComuna(buscarComuna(region, comuna));
        orden.setIndicaciones(indicaciones);
        for (Linea linea : lineas) {
            orden.agregarItem(new DetalleOrden(linea.producto(), linea.precioUnitario(), linea.cantidad()));
        }
        orden.setTotal(orden.getItems().stream().mapToInt(DetalleOrden::getSubtotal).sum());
        // Se guarda una por una para que la secuencia numere en este orden (1001, 1002...).
        ordenRepository.save(orden);
    }
}
