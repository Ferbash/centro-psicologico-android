package com.example.centropsicologicoandroid;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/** Guarda la lista de historias: al girar la pantalla no se vuelve a pedir. */
public class HistoriasViewModel extends AndroidViewModel {

    private final MutableLiveData<Estado<RespuestaHistorias>> estado = new MutableLiveData<>();

    public HistoriasViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<Estado<RespuestaHistorias>> getEstado() { return estado; }

    /** Pide la lista solo la primera vez. */
    public void cargar(String token) {
        if (estado.getValue() == null) recargar(token);
    }

    public void recargar(String token) {
        Estado<RespuestaHistorias> actual = estado.getValue();
        if (actual != null && actual.esCargando()) return;
        Pedido.ejecutar(getApplication(), ApiCliente.getService().obtenerHistorias(token), estado);
    }
}
