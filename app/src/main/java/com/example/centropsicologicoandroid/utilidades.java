package com.example.centropsicologicoandroid;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.centropsicologico.R;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.function.Consumer;

import okhttp3.ResponseBody;
import retrofit2.Response;

final class Utilidades {

    private static final String TAG = "Utilidades";
    @SuppressLint("ConstantLocale")
    private static final SimpleDateFormat FORMATO_API =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private Utilidades() { }

    /** Devuelve el mensaje que mandó la API en un error (400, 404, 422...) o "Error <código>". */
    public static String mensajeDeError(Context contexto, Response<?> response) {
        try (ResponseBody eb = response.errorBody()) {
            if (eb != null) {
                RespuestaApi ra = new Gson().fromJson(eb.string(), RespuestaApi.class);
                if (ra != null && ra.getMensaje() != null) return ra.getMensaje();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo errorBody", e);
        }
        return contexto.getString(R.string.error_codigo, response.code());
    }

    /** Muestra el selector de fecha y hora (solo futuras) y devuelve "yyyy-MM-dd HH:mm:ss". */
    public static void elegirFechaHora(Context contexto, Consumer<String> alElegir) {
        Calendar ahora = Calendar.getInstance();
        DatePickerDialog dialogo = new DatePickerDialog(contexto, (view, y, m, d) ->
                new TimePickerDialog(contexto, (v, h, min) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(y, m, d, h, min, 0);
                    if (c.getTimeInMillis() <= System.currentTimeMillis()) {
                        Toast.makeText(contexto, R.string.msg_fecha_futura, Toast.LENGTH_LONG).show();
                        return;
                    }
                    alElegir.accept(FORMATO_API.format(c.getTime()));
                }, 9, 0, true).show(),
                ahora.get(Calendar.YEAR), ahora.get(Calendar.MONTH), ahora.get(Calendar.DAY_OF_MONTH));
        dialogo.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialogo.show();
    }
}