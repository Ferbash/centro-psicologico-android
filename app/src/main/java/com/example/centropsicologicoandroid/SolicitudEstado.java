package com.example.centropsicologicoandroid;

import com.google.gson.annotations.SerializedName;

public class SolicitudEstado {
    @SerializedName("turno_id")
    private final int turnoId;

    private final String estado;

    @SerializedName("opcion_horario_alternativo")
    private final String opcionHorarioAlternativo;

    public SolicitudEstado(int turnoId, String estado, String opcionHorarioAlternativo) {
        this.turnoId = turnoId;
        this.estado = estado;
        this.opcionHorarioAlternativo = opcionHorarioAlternativo;
    }
}