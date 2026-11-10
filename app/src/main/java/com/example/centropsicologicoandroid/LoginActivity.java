package com.example.centropsicologicoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.centropsicologico.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnIngresar, btnRegistrar;
    private ProgressBar progressBar;
    private TextView tvEstado;
    private LoginViewModel vm;

    // Resultado de RegistroActivity (Activity Result API): vuelve el email registrado
    private final ActivityResultLauncher<Intent> launcherRegistro = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String email = result.getData().getStringExtra(RegistroActivity.EXTRA_EMAIL);
                    if (email != null && etEmail != null) {
                        etEmail.setText(email);
                        etPassword.requestFocus();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Sesión guardada: directo a la lista (Login no queda en la pila)
        if (FirebaseAuth.getInstance().getCurrentUser() != null && Sesion.bearer(this) != null) {
            irAMainActivity();
            return;
        }

        BarrasSistema.aplicar(findViewById(R.id.raiz));
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        progressBar = findViewById(R.id.progressBar);
        tvEstado = findViewById(R.id.tvEstado);

        vm = new ViewModelProvider(this).get(LoginViewModel.class);
        vm.getEstado().observe(this, this::mostrar);

        btnIngresar.setOnClickListener(v -> ingresar());
        btnRegistrar.setOnClickListener(v ->
                launcherRegistro.launch(new Intent(this, RegistroActivity.class)));
    }

    private void ingresar() {
        String email = etEmail.getText().toString().trim().toLowerCase();
        String pass = etPassword.getText().toString();
        if (!camposValidos(email, pass)) return;
        vm.ingresar(email, pass);
    }

    private void mostrar(Estado<Usuario> e) {
        boolean cargando = e.esCargando();
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnIngresar.setEnabled(!cargando);
        btnRegistrar.setEnabled(!cargando);

        switch (e.tipo) {
            case OK:
                irAMainActivity();
                break;
            case CARGANDO:
                tvEstado.setText("");
                break;
            default: // ERROR_API o SIN_CONEXION: el mensaje ya viene listo
                tvEstado.setText(e.mensaje);
                break;
        }
    }

    /** true si los datos están bien; si no, marca el campo con el error. */
    private boolean camposValidos(String email, String pass) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.login_error_correo));
            etEmail.requestFocus();
            return false;
        }
        if (pass.length() < 8) {
            etPassword.setError(getString(R.string.login_error_password));
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void irAMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
