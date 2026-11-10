package com.example.centropsicologicoandroid;

/** Respuesta de GET gestionar_turnos.php?id=X: { exito, turno: {...} }. */
public class RespuestaTurno {
    private boolean exito;
    private String mensaje;
    private Turno turno;

    public boolean isExito() { return exito; }
    public String getMensaje() { return mensaje; }
    public Turno getTurno() { return turno; }
}
