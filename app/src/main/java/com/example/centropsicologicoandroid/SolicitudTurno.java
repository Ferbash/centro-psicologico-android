package com.example.centropsicologicoandroid;

import com.google.gson.annotations.SerializedName;

/** Cuerpo de POST gestionar_turnos.php. El paciente sale del token, no del cuerpo. */
public class SolicitudTurno {
    @SerializedName("profesional_id")
    private final int profesionalId;

    @SerializedName("fecha_hora_solicitada")
    private final String fechaHoraSolicitada;

    public SolicitudTurno(int profesionalId, String fechaHoraSolicitada) {
        this.profesionalId = profesionalId;
        this.fechaHoraSolicitada = fechaHoraSolicitada;
    }
}
