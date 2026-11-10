package com.example.centropsicologicoandroid;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class RegistroViewModel extends AndroidViewModel {

    private final MutableLiveData<Estado<RespuestaApi>> estado = new MutableLiveData<>();
    private String emailEnviado;

    public RegistroViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<Estado<RespuestaApi>> getEstado() { return estado; }

    /** Email con el que se registró (la pantalla lo devuelve al login). */
    public String getEmailEnviado() { return emailEnviado; }

    public void registrar(SolicitudRegistro solicitud, String email) {
        Estado<RespuestaApi> actual = estado.getValue();
        if (actual != null && actual.esCargando()) return;
        emailEnviado = email;
        Pedido.ejecutar(getApplication(), ApiCliente.getService().registrar(solicitud), estado);
    }
}
