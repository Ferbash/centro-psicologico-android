package com.example.centropsicologicoandroid;

import com.google.gson.annotations.SerializedName;

public class Turno {
    private int id;

    @SerializedName("paciente_id")
    private int pacienteId;

    @SerializedName("profesional_id")
    private int profesionalId;

    @SerializedName("fecha_hora_solicitada")
    private String fechaHoraSolicitada;

    private String estado;

    @SerializedName("nombre_profesional")
    private String nombreProfesional;

    public int getId() { return id; }
    public String getFechaHoraSolicitada() { return fechaHoraSolicitada; }
    public String getEstado() { return estado != null ? estado : "pendiente"; }
    public String getNombreProfesional() { return nombreProfesional != null ? nombreProfesional : "Profesional"; }
}