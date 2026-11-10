package com.example.centropsicologicoandroid;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.centropsicologico.R;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Ejecuta un pedido de Retrofit fuera del hilo principal y publica el resultado
 * como Estado. Distingue "no hubo respuesta" de "la API respondió un error".
 * Se usa desde los ViewModel, así el pedido sobrevive a la rotación.
 */
final class Pedido {

    private static final String TAG = "Pedido";

    private Pedido() { }

    static <T> void ejecutar(Context app, Call<T> call, MutableLiveData<Estado<T>> destino) {
        destino.setValue(Estado.cargando());
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> c, @NonNull Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    destino.setValue(Estado.ok(response.body()));
                } else if (response.code() == 401) {
                    destino.setValue(Estado.sesionExpirada());
                } else {
                    destino.setValue(Estado.errorApi(Utilidades.mensajeDeError(app, response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> c, @NonNull Throwable t) {
                if (c.isCanceled()) return;
                Log.e(TAG, "Pedido sin respuesta válida", t);
                if (t instanceof IOException) {
                    destino.setValue(Estado.sinConexion(app.getString(R.string.msg_sin_conexion)));
                } else {
                    destino.setValue(Estado.errorApi(app.getString(R.string.error_respuesta_invalida)));
                }
            }
        });
    }
}
