package com.example.centropsicologicoandroid;

import com.google.gson.annotations.SerializedName;

/** Una nota de historia clínica, tal como la devuelve GET historias_clinicas.php. */
public class Historia {
    private int id;

    @SerializedName("paciente_id")
    private int pacienteId;

    @SerializedName("profesional_id")
    private int profesionalId;

    private String notas;

    @SerializedName("fecha_hora")
    private String fechaHora;

    @SerializedName("nombre_paciente")
    private String nombrePaciente;

    @SerializedName("nombre_profesional")
    private String nombreProfesional;

    public int getId() { return id; }
    public String getNotas() { return notas != null ? notas : ""; }
    public String getFechaHora() { return fechaHora != null ? fechaHora : ""; }
    public String getNombrePaciente() { return nombrePaciente; }
    public String getNombreProfesional() { return nombreProfesional; }
}
