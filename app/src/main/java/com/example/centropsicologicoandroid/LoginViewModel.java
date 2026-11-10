package com.example.centropsicologicoandroid;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.centropsicologico.R;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Ingreso en dos pasos: 1) login contra la API PHP (fuente de verdad: también entran
 * los usuarios creados en la web) y 2) Firebase Authentication. Si la cuenta no existe
 * en Firebase se crea con el mismo correo y contraseña.
 */
public class LoginViewModel extends AndroidViewModel {

    private static final String TAG = "LoginViewModel";

    private final MutableLiveData<Estado<Usuario>> estado = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<Estado<Usuario>> getEstado() { return estado; }

    public void ingresar(String email, String pass) {
        Estado<Usuario> actual = estado.getValue();
        if (actual != null && actual.esCargando()) return;
        estado.setValue(Estado.cargando());

        ApiCliente.getService().login(new SolicitudLogin(email, pass)).enqueue(new Callback<RespuestaLogin>() {
            @Override
            public void onResponse(@NonNull Call<RespuestaLogin> call, @NonNull Response<RespuestaLogin> response) {
                RespuestaLogin r = response.body();
                if (response.isSuccessful() && r != null && r.isExito()) {
                    asegurarCuentaFirebase(email, pass, r);
                } else if (response.isSuccessful() && r != null && r.getMensaje() != null) {
                    estado.setValue(Estado.errorApi(r.getMensaje()));
                } else {
                    estado.setValue(Estado.errorApi(Utilidades.mensajeDeError(getApplication(), response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RespuestaLogin> call, @NonNull Throwable t) {
                Log.e(TAG, "Login PHP sin respuesta", t);
                if (t instanceof IOException) {
                    estado.setValue(Estado.sinConexion(getApplication().getString(R.string.login_sin_conexion)));
                } else {
                    estado.setValue(Estado.errorApi(getApplication().getString(R.string.error_respuesta_invalida)));
                }
            }
        });
    }

    private void asegurarCuentaFirebase(String email, String pass, RespuestaLogin r) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                terminar(r);
                return;
            }
            Exception ex = task.getException();
            if (ex instanceof FirebaseAuthInvalidUserException
                    || ex instanceof FirebaseAuthInvalidCredentialsException) {
                // Usuario creado en la web: todavía no tiene cuenta en Firebase
                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(crear -> {
                    if (crear.isSuccessful()) {
                        terminar(r);
                    } else {
                        estado.setValue(errorFirebase(crear.getException()));
                    }
                });
            } else {
                estado.setValue(errorFirebase(ex));
            }
        });
    }

    private void terminar(RespuestaLogin r) {
        Sesion.guardar(getApplication(), r.getToken(), r.getUsuario());
        estado.setValue(Estado.ok(r.getUsuario()));
    }

    private Estado<Usuario> errorFirebase(Exception e) {
        Application app = getApplication();
        Log.e(TAG, "Error de Firebase", e);
        if (e instanceof FirebaseNetworkException) {
            return Estado.sinConexion(app.getString(R.string.msg_sin_conexion));
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return Estado.errorApi(app.getString(R.string.login_firebase_clave_distinta));
        }
        return Estado.errorApi(app.getString(R.string.login_error_generico,
                e != null && e.getMessage() != null ? e.getMessage() : app.getString(R.string.login_desconocido)));
    }
}
