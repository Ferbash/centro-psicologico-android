package com.example.centropsicologicoandroid;

public class RespuestaLogin {
    private boolean exito;
    private String mensaje;
    private String token;
    private Usuario usuario;

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getToken() {
        return token;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
