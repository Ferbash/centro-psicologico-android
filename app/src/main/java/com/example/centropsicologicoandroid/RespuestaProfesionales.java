package com.example.centropsicologicoandroid;

import java.util.List;

public class RespuestaProfesionales {
    private boolean exito;
    private String mensaje;
    private List<Profesional> profesionales;

    public boolean isExito() { return exito; }
    public String getMensaje() { return mensaje; }
    public List<Profesional> getProfesionales() { return profesionales; }
}