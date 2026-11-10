package com.example.centropsicologicoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.centropsicologico.R;

/** Alta de paciente o profesional (POST registrar.php). Devuelve el email al login. */
public class RegistroActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "registro_email";

    private EditText etNombre, etEmail, etPassword, etCiudad, etTelefono, etMatricula, etEspecialidad;
    private RadioGroup rgRol;
    private View llProfesional;
    private Button btnCrear;
    private ProgressBar pbRegistro;
    private TextView tvEstado;
    private RegistroViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        BarrasSistema.aplicar(findViewById(R.id.raiz));

        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmailRegistro);
        etPassword = findViewById(R.id.etPasswordRegistro);
        etCiudad = findViewById(R.id.etCiudad);
        etTelefono = findViewById(R.id.etTelefono);
        etMatricula = findViewById(R.id.etMatricula);
        etEspecialidad = findViewById(R.id.etEspecialidad);
        rgRol = findViewById(R.id.rgRol);
        llProfesional = findViewById(R.id.llProfesional);
        btnCrear = findViewById(R.id.btnCrearCuenta);
        pbRegistro = findViewById(R.id.pbRegistro);
        tvEstado = findViewById(R.id.tvEstadoRegistro);

        rgRol.setOnCheckedChangeListener((grupo, id) -> actualizarCamposProfesional());
        btnCrear.setOnClickListener(v -> crearCuenta());
        findViewById(R.id.btnVolverRegistro).setOnClickListener(v -> finish());

        vm = new ViewModelProvider(this).get(RegistroViewModel.class);
        vm.getEstado().observe(this, this::mostrar);
    }

    // La visibilidad no se guarda sola: se recalcula después de restaurar el RadioGroup
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        actualizarCamposProfesional();
    }

    private boolean esProfesional() {
        return rgRol.getCheckedRadioButtonId() == R.id.rbProfesional;
    }

    private void actualizarCamposProfesional() {
        llProfesional.setVisibility(esProfesional() ? View.VISIBLE : View.GONE);
    }

    private void crearCuenta() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase();
        String pass = etPassword.getText().toString();
        String ciudad = etCiudad.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String matricula = etMatricula.getText().toString().trim();
        String especialidad = etEspecialidad.getText().toString().trim();
        boolean profesional = esProfesional();

        if (nombre.isEmpty()) { marcar(etNombre, R.string.registro_error_nombre); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { marcar(etEmail, R.string.login_error_correo); return; }
        if (pass.length() < 8) { marcar(etPassword, R.string.login_error_password); return; }
        if (!telefono.isEmpty() && telefono.replaceAll("\\D", "").length() < 8) { marcar(etTelefono, R.string.registro_error_telefono); return; }
        if (profesional && matricula.isEmpty()) { marcar(etMatricula, R.string.registro_error_matricula); return; }
        if (profesional && especialidad.isEmpty()) { marcar(etEspecialidad, R.string.registro_error_especialidad); return; }

        SolicitudRegistro solicitud = new SolicitudRegistro(nombre, email, pass, ciudad,
                profesional ? Sesion.ROL_PROFESIONAL : Sesion.ROL_PACIENTE, telefono,
                profesional ? matricula : null, profesional ? especialidad : null);
        vm.registrar(solicitud, email);
    }

    private void marcar(EditText campo, int mensaje) {
        campo.setError(getString(mensaje));
        campo.requestFocus();
    }

    // Importante: acá no se leen los EditText (el observer corre antes de restaurar su texto)
    private void mostrar(Estado<RespuestaApi> e) {
        boolean cargando = e.esCargando();
        pbRegistro.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnCrear.setEnabled(!cargando);

        switch (e.tipo) {
            case OK:
                Toast.makeText(this, R.string.msg_cuenta_creada, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK, new Intent().putExtra(EXTRA_EMAIL, vm.getEmailEnviado()));
                finish();
                break;
            case CARGANDO:
                tvEstado.setText("");
                break;
            case SESION_EXPIRADA: // no aplica al registro, pero se cubre el caso
            default:
                tvEstado.setText(e.mensaje);
                break;
        }
    }
}
