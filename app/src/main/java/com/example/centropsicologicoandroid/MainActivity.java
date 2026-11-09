package com.example.centropsicologicoandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.centropsicologico.R;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity_Turnos";
    private ProgressBar pbCargando;
    private TextView tvEstadoApi;

    private final List<String> listaFormateada = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lvTurnos = findViewById(R.id.lvTurnos);
        pbCargando = findViewById(R.id.pbCargando);
        tvEstadoApi = findViewById(R.id.tvEstadoApi);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaFormateada);
        lvTurnos.setAdapter(adapter);

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        cargarTurnosDesdeApi();
    }

    private void cargarTurnosDesdeApi() {
        pbCargando.setVisibility(View.VISIBLE);
        tvEstadoApi.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String jwtToken = prefs.getString("token", null);

        if (jwtToken != null) {
            String tokenHeader = "Bearer " + jwtToken;
            ejecutarLlamadaApi(tokenHeader);
        } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // Si hay usuario en Firebase pero falta token JWT, intentar re-autenticar con email
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            Log.w(TAG, "Falta token JWT para usuario: " + email);
            cerrarSesion();
        } else {
            pbCargando.setVisibility(View.GONE);
            tvEstadoApi.setText(R.string.error_usuario_no_autenticado);
            tvEstadoApi.setVisibility(View.VISIBLE);
        }
    }

    private void ejecutarLlamadaApi(String tokenHeader) {
        ApiCliente.getService().obtenerTurnos(tokenHeader).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RespuestaTurnos> call, @NonNull Response<RespuestaTurnos> response) {
                pbCargando.setVisibility(View.GONE);
                Log.d(TAG, "Respuesta HTTP Code: " + response.code());

                if (response.isSuccessful() && response.body() != null && response.body().isExito()) {
                    listaFormateada.clear();
                    List<Turno> turnos = response.body().getTurnos();
                    if (turnos != null && !turnos.isEmpty()) {
                        for (Turno t : turnos) {
                            String info = getString(R.string.info_turno_formato, t.getId(), t.getFechaHoraSolicitada(), t.getEstado().toUpperCase());
                            listaFormateada.add(info);
                        }
                    } else {
                        tvEstadoApi.setText(R.string.no_hay_turnos);
                        tvEstadoApi.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                } else if (response.code() == 401) {
                    Log.w(TAG, "Sesión inválida o expirada (401). Redirigiendo a Login.");
                    cerrarSesion();
                } else {
                    String errorBodyStr = "";
                    try (ResponseBody errorBody = response.errorBody()) {
                        if (errorBody != null) {
                            errorBodyStr = errorBody.string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error leyendo errorBody", e);
                    }
                    Log.e(TAG, "Llamada fallida. Code: " + response.code() + ", errorBody: " + errorBodyStr);
                    tvEstadoApi.setText(getString(R.string.error_cargar_turnos, response.code()));
                    tvEstadoApi.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RespuestaTurnos> call, @NonNull Throwable t) {
                pbCargando.setVisibility(View.GONE);
                Log.e(TAG, "Error de red/conexión", t);
                tvEstadoApi.setText(R.string.error_conexion_servidor);
                tvEstadoApi.setVisibility(View.VISIBLE);
            }
        });
    }

    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        prefs.edit().remove("token").apply();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
