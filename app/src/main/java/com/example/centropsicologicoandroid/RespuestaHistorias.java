package com.example.centropsicologicoandroid;

import java.util.List;

public class RespuestaHistorias {
    private boolean exito;
    private String mensaje;
    private List<Historia> historias;

    public boolean isExito() { return exito; }
    public String getMensaje() { return mensaje; }
    public List<Historia> getHistorias() { return historias; }
}
