package com.example.centropsicologicoandroid;

import java.util.List;

public class RespuestaTurnos {
    private boolean exito;
    private String mensaje;
    private List<Turno> turnos;

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public List<Turno> getTurnos() {
        return turnos;
    }
}
