package com.example.centropsicologicoandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.centropsicologico.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnIngresar, btnRegistrar;
    private ProgressBar progressBar;
    private TextView tvEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Verificar si existe token de sesión activo guardado
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String savedToken = prefs.getString("token", null);

        if (mAuth.getCurrentUser() != null && savedToken != null) {
            irAMainActivity();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        progressBar = findViewById(R.id.progressBar);
        tvEstado = findViewById(R.id.tvEstado);

        btnIngresar.setOnClickListener(v -> loginUsuario());
        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void loginUsuario() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (validarCampos(email, pass)) return;

        mostrarCargando(true);
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        autenticarConBackendPHP(email, pass);
                    } else {
                        mostrarCargando(false);
                        tvEstado.setText(obtenerMensajeError(task.getException()));
                    }
                });
    }

    private void registrarUsuario() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (validarCampos(email, pass)) return;

        mostrarCargando(true);
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        registrarEnBackendPHP(email, pass);
                    } else {
                        mostrarCargando(false);
                        tvEstado.setText(obtenerMensajeError(task.getException()));
                    }
                });
    }

    private void autenticarConBackendPHP(String email, String pass) {
        ApiCliente.getService().login(new SolicitudLogin(email, pass)).enqueue(new Callback<>() {
            @android.annotation.SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<RespuestaLogin> call, @NonNull Response<RespuestaLogin> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isExito()) {
                    mostrarCargando(false);
                    String token = response.body().getToken();
                    guardarToken(token);
                    irAMainActivity();
                } else if (response.code() == 401) {
                    // Si el usuario existe en Firebase pero no en la BD PHP, lo registramos automáticamente en el backend
                    registrarEnBackendPHP(email, pass);
                } else {
                    mostrarCargando(false);
                    tvEstado.setText("Error al conectar con la API PHP: " + response.code());
                }
            }

            @android.annotation.SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<RespuestaLogin> call, @NonNull Throwable t) {
                mostrarCargando(false);
                Log.e(TAG, "Error al autenticar en PHP", t);
                tvEstado.setText("Sin conexión con el servidor en Render.");
            }
        });
    }

    private void registrarEnBackendPHP(String email, String pass) {
        String nombre = email.contains("@") ? email.split("@")[0] : email;
        SolicitudRegistro registro = new SolicitudRegistro(nombre, email, pass, "Tres Arroyos", "paciente", "2983000000");

        ApiCliente.getService().registrar(registro).enqueue(new Callback<>() {
            @android.annotation.SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<RespuestaApi> call, @NonNull Response<RespuestaApi> response) {
                if (response.isSuccessful() || response.code() == 409 || response.code() == 422) {
                    // Tras intentar registrar o si ya existe, procedemos al login PHP
                    autenticarConBackendPHP(email, pass);
                } else {
                    mostrarCargando(false);
                    tvEstado.setText("Error registrando en backend: " + response.code());
                }
            }

            @android.annotation.SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<RespuestaApi> call, @NonNull Throwable t) {
                mostrarCargando(false);
                Log.e(TAG, "Error registrando en backend PHP", t);
                tvEstado.setText("Sin conexión con el servidor en Render.");
            }
        });
    }

    private void guardarToken(String token) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        prefs.edit().putString("token", token).apply();
    }

    private boolean validarCampos(String email, String pass) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un correo válido");
            etEmail.requestFocus();
            return true;
        }
        if (pass.length() < 8) {
            etPassword.setError("La contraseña debe tener al menos 8 caracteres");
            etPassword.requestFocus();
            return true;
        }
        return false;
    }

    private void mostrarCargando(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnIngresar.setEnabled(!cargando);
        btnRegistrar.setEnabled(!cargando);
    }

    private String obtenerMensajeError(Exception e) {
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Credenciales inválidas. Verifique su correo y clave.";
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            return "Ya existe una cuenta registrada con este correo.";
        }
        return "Error: " + (e != null ? e.getMessage() : "Desconocido");
    }

    private void irAMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
