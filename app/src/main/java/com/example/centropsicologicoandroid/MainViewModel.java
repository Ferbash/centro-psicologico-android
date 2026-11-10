package com.example.centropsicologicoandroid;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/** Estado de la lista de turnos y del alta de turnos: sobrevive a la rotación. */
public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<Estado<RespuestaTurnos>> turnos = new MutableLiveData<>();
    private final MutableLiveData<Estado<RespuestaProfesionales>> profesionales = new MutableLiveData<>();
    private final MutableLiveData<Estado<RespuestaApi>> solicitud = new MutableLiveData<>();

    public MainViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<Estado<RespuestaTurnos>> getTurnos() { return turnos; }
    public LiveData<Estado<RespuestaProfesionales>> getProfesionales() { return profesionales; }
    public LiveData<Estado<RespuestaApi>> getSolicitud() { return solicitud; }

    /** Pide la lista solo la primera vez (al girar la pantalla no se repite). */
    public void cargarTurnos(String token) {
        if (turnos.getValue() == null) recargarTurnos(token);
    }

    public void recargarTurnos(String token) {
        if (enCurso(turnos.getValue())) return;
        Pedido.ejecutar(getApplication(), ApiCliente.getService().obtenerTurnos(token), turnos);
    }

    public void pedirProfesionales(String token) {
        if (enCurso(profesionales.getValue())) return;
        Pedido.ejecutar(getApplication(), ApiCliente.getService().obtenerProfesionales(token), profesionales);
    }

    public void solicitar(String token, int profesionalId, String fechaHora) {
        if (enCurso(solicitud.getValue())) return;
        Pedido.ejecutar(getApplication(),
                ApiCliente.getService().solicitarTurno(token, new SolicitudTurno(profesionalId, fechaHora)),
                solicitud);
    }

    private boolean enCurso(Estado<?> e) {
        return e != null && e.esCargando();
    }
}
