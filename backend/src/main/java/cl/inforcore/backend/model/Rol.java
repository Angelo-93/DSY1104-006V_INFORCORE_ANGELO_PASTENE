package cl.inforcore.backend.model;

/**
 * Roles del sistema: los mismos tres de la EP1 y la EP2.
 *
 * Es un enum, como en la clase del 10-11, para que solo existan estos tres
 * valores: no se puede registrar un rol inventado. En la entidad se guarda con
 * {@code @Enumerated(EnumType.STRING)}, es decir, la columna guarda el nombre
 * ("CLIENTE") y no la posición (2): si un día se agrega un rol en medio de la
 * lista, los datos que ya existen no cambian de significado.
 */
public enum Rol {

    /** Acceso total al panel: crea, edita y elimina. */
    ADMINISTRADOR,

    /** Entra al panel solo para consultar productos y órdenes. */
    VENDEDOR,

    /** Solo la tienda: compra y revisa su historial. */
    CLIENTE
}
