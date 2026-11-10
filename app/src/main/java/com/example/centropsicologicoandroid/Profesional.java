package com.example.centropsicologicoandroid;

import com.google.gson.annotations.SerializedName;

public class Profesional {
    @SerializedName(value = "profesional_id", alternate = {"id"})
    private int id;
    private String nombre;
    private String especialidad;

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        String n = nombre != null ? nombre : "Profesional";
        return (especialidad != null && !especialidad.isEmpty()) ? n + " - " + especialidad : n;
    }
}