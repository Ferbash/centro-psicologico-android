package com.example.centropsicologicoandroid;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/** Carga un turno por su id (GET ?id=) y ejecuta las acciones sobre él (PUT / DELETE). */
public class DetalleTurnoViewModel extends AndroidViewModel {

    private final MutableLiveData<Estado<RespuestaTurno>> turno = new MutableLiveData<>();
    private final MutableLiveData<Estado<RespuestaApi>> accion = new MutableLiveData<>();

    public DetalleTurnoViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<Estado<RespuestaTurno>> getTurno() { return turno; }
    public LiveData<Estado<RespuestaApi>> getAccion() { return accion; }

    public void cargar(String token, int id) {
        if (turno.getValue() != null) return; // ya se pidió: al girar no se repite
        Pedido.ejecutar(getApplication(), ApiCliente.getService().obtenerTurno(token, id), turno);
    }

    public void cambiarEstado(String token, int id, String estado, String horarioAlternativo) {
        if (accionEnCurso()) return;
        Pedido.ejecutar(getApplication(),
                ApiCliente.getService().cambiarEstado(token, new SolicitudEstado(id, estado, horarioAlternativo)),
                accion);
    }

    public void eliminar(String token, int id) {
        if (accionEnCurso()) return;
        Pedido.ejecutar(getApplication(), ApiCliente.getService().eliminarTurno(token, id), accion);
    }

    private boolean accionEnCurso() {
        Estado<RespuestaApi> e = accion.getValue();
        return e != null && e.esCargando();
    }
}
