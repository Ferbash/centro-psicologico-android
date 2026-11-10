package com.example.centropsicologicoandroid;

import com.google.gson.annotations.SerializedName;

public class Turno {

    public static final String ESTADO_ACEPTADO = "aceptado";
    public static final String ESTADO_REPROGRAMADO = "reprogramado";
    public static final String ESTADO_CANCELADO = "cancelado";

    private int id;

    @SerializedName("paciente_id")
    private int pacienteId;

    @SerializedName("profesional_id")
    private int profesionalId;

    @SerializedName("fecha_hora_solicitada")
    private String fechaHoraSolicitada;

    @SerializedName("opcion_horario_alternativo")
    private String opcionHorarioAlternativo;

    private String estado;

    @SerializedName("nombre_profesional")
    private String nombreProfesional;

    @SerializedName("nombre_paciente")
    private String nombrePaciente;

    public int getId() { return id; }
    public int getPacienteId() { return pacienteId; }
    public int getProfesionalId() { return profesionalId; }
    public String getFechaHoraSolicitada() { return fechaHoraSolicitada; }
    public String getOpcionHorarioAlternativo() { return opcionHorarioAlternativo; }
    public String getEstado() { return estado != null ? estado : "pendiente"; }
    public String getNombreProfesional() { return nombreProfesional; }
    public String getNombrePaciente() { return nombrePaciente; }
}
