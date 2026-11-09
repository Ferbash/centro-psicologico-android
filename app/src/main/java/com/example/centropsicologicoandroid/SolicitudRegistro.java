package com.example.centropsicologicoandroid;

public class SolicitudRegistro {
    private final String nombre;
    private final String email;
    private final String password;
    private final String ciudad;
    private final String rol;
    private final String telefono;

    public SolicitudRegistro(String nombre, String email, String password, String ciudad, String rol, String telefono) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.ciudad = ciudad;
        this.rol = rol;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getRol() {
        return rol;
    }

    public String getTelefono() {
        return telefono;
    }
}
