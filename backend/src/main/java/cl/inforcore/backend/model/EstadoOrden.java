package cl.inforcore.backend.model;

/**
 * Resultado del pago de una orden. En la EP2 eran los textos 'pagada' y
 * 'rechazada'; como enum, la base de datos no acepta otro valor.
 */
public enum EstadoOrden {

    PAGADA,

    RECHAZADA
}
