package com.example.centropsicologicoandroid;

/** Resultado de un pedido a la API, para que la pantalla lo muestre sin conocer Retrofit. */
public final class Estado<T> {

    public enum Tipo { CARGANDO, OK, ERROR_API, SIN_CONEXION, SESION_EXPIRADA }

    public final Tipo tipo;
    public final T datos;
    public final String mensaje;
    private boolean consumido = false;

    private Estado(Tipo tipo, T datos, String mensaje) {
        this.tipo = tipo;
        this.datos = datos;
        this.mensaje = mensaje;
    }

    public static <T> Estado<T> cargando() { return new Estado<>(Tipo.CARGANDO, null, null); }
    public static <T> Estado<T> ok(T datos) { return new Estado<>(Tipo.OK, datos, null); }
    /** La API respondió con un error (400, 404, 422...): mensaje es el que mandó la API. */
    public static <T> Estado<T> errorApi(String mensaje) { return new Estado<>(Tipo.ERROR_API, null, mensaje); }
    /** No hubo respuesta (modo avión, servidor caído). */
    public static <T> Estado<T> sinConexion(String mensaje) { return new Estado<>(Tipo.SIN_CONEXION, null, mensaje); }
    public static <T> Estado<T> sesionExpirada() { return new Estado<>(Tipo.SESION_EXPIRADA, null, null); }

    public boolean esCargando() { return tipo == Tipo.CARGANDO; }

    /**
     * Para efectos de una sola vez (un Toast, un diálogo): devuelve true la primera vez y false
     * después, así al girar la pantalla el LiveData no repite el efecto.
     */
    public boolean consumir() {
        if (consumido) return false;
        consumido = true;
        return true;
    }
}
