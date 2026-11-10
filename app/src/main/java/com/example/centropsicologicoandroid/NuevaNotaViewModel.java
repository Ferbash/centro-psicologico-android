package com.example.centropsicologicoandroid;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/** Envía la nota una sola vez, aunque se gire la pantalla o se toque dos veces el botón. */
public class NuevaNotaViewModel extends AndroidViewModel {

    private final MutableLiveData<Estado<RespuestaApi>> estado = new MutableLiveData<>();

    public NuevaNotaViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<Estado<RespuestaApi>> getEstado() { return estado; }

    public void guardar(String token, int pacienteId, String notas) {
        Estado<RespuestaApi> actual = estado.getValue();
        if (actual != null && actual.esCargando()) return;
        Pedido.ejecutar(getApplication(),
                ApiCliente.getService().guardarNota(token, new SolicitudNota(pacienteId, notas)),
                estado);
    }
}
