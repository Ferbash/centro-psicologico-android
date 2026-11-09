package com.example.centropsicologicoandroid;

import com.google.gson.annotations.SerializedName;

public class Usuario {
    private int id;

    @SerializedName("perfil_id")
    private int perfilId;

    private String nombre;
    private String email;
    private String rol;

    public int getId() {
        return id;
    }

    public int getPerfilId() {
        return perfilId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }
}
