package com.example.centropsicologicoandroid;

import com.google.gson.annotations.SerializedName;

/** Cuerpo de POST historias_clinicas.php. El profesional sale del token, no del cuerpo. */
public class SolicitudNota {
    @SerializedName("paciente_id")
    private final int pacienteId;

    private final String notas;

    public SolicitudNota(int pacienteId, String notas) {
        this.pacienteId = pacienteId;
        this.notas = notas;
    }
}
