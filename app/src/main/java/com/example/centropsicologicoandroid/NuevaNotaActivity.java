package com.example.centropsicologicoandroid;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.centropsicologico.R;

import java.util.ArrayList;

/** Pantalla del profesional para cargar una nota clínica (POST historias_clinicas.php). */
public class NuevaNotaActivity extends AppCompatActivity {

    public static final int RESULT_SESION_EXPIRADA = RESULT_FIRST_USER;

    private NuevaNotaViewModel vm;
    private String token;
    private ArrayList<Integer> pacienteIds;
    private boolean hayPacientes;

    private Spinner spPaciente;
    private EditText etNota;
    private TextView tvEstadoNota;
    private ProgressBar pbNota;
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_nota);
        BarrasSistema.aplicar(findViewById(R.id.raiz));

        token = Sesion.bearer(this);
        if (token == null) {
            setResult(RESULT_SESION_EXPIRADA);
            finish();
            return;
        }

        spPaciente = findViewById(R.id.spPaciente);
        etNota = findViewById(R.id.etNota);
        tvEstadoNota = findViewById(R.id.tvEstadoNota);
        pbNota = findViewById(R.id.pbNota);
        btnGuardar = findViewById(R.id.btnGuardarNota);
        findViewById(R.id.btnVolverNota).setOnClickListener(v -> finish());

        pacienteIds = getIntent().getIntegerArrayListExtra(HistoriasActivity.EXTRA_PACIENTE_IDS);
        ArrayList<String> nombres = getIntent().getStringArrayListExtra(HistoriasActivity.EXTRA_PACIENTE_NOMBRES);
        hayPacientes = pacienteIds != null && nombres != null && !pacienteIds.isEmpty()
                && pacienteIds.size() == nombres.size();

        if (hayPacientes) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombres);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spPaciente.setAdapter(adapter);
        } else {
            tvEstadoNota.setText(R.string.nota_sin_pacientes);
            tvEstadoNota.setVisibility(View.VISIBLE);
            btnGuardar.setEnabled(false);
        }
        btnGuardar.setOnClickListener(v -> guardar());

        vm = new ViewModelProvider(this).get(NuevaNotaViewModel.class);
        vm.getEstado().observe(this, this::mostrar);
    }

    private void guardar() {
        String notas = etNota.getText().toString().trim();
        if (notas.isEmpty()) {
            etNota.setError(getString(R.string.nota_error_vacia));
            etNota.requestFocus();
            return;
        }
        vm.guardar(token, pacienteIds.get(spPaciente.getSelectedItemPosition()), notas);
    }

    private void mostrar(Estado<RespuestaApi> e) {
        boolean cargando = e.esCargando();
        pbNota.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!cargando && hayPacientes);

        switch (e.tipo) {
            case OK:
                Toast.makeText(this, R.string.msg_nota_guardada, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
                break;
            case SESION_EXPIRADA:
                setResult(RESULT_SESION_EXPIRADA);
                finish();
                break;
            case CARGANDO:
                tvEstadoNota.setVisibility(View.GONE);
                break;
            default: // ERROR_API (ej. 403 "No tienes turnos con este paciente") o SIN_CONEXION
                tvEstadoNota.setText(e.mensaje);
                tvEstadoNota.setVisibility(View.VISIBLE);
                break;
        }
    }
}
