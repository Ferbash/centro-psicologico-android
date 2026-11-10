package com.example.centropsicologicoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.centropsicologico.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Historias clínicas: pantalla exclusiva del profesional. Ve las notas que escribió para
 * seguir a sus pacientes y puede cargar una nueva (NuevaNotaActivity). Los pacientes no
 * llegan a esta pantalla.
 */
public class HistoriasActivity extends AppCompatActivity {

    /** Ids y nombres de los pacientes del profesional (salen de sus turnos). */
    public static final String EXTRA_PACIENTE_IDS = "paciente_ids";
    public static final String EXTRA_PACIENTE_NOMBRES = "paciente_nombres";
    public static final int RESULT_SESION_EXPIRADA = RESULT_FIRST_USER;

    private HistoriasViewModel vm;
    private String token;

    private ProgressBar pbHistorias;
    private TextView tvEstadoHistorias;
    private Button btnNuevaNota;
    private ArrayAdapter<String> adapter;
    private final List<String> items = new ArrayList<>();

    private final ActivityResultLauncher<Intent> launcherNota = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    vm.recargar(token);
                } else if (result.getResultCode() == NuevaNotaActivity.RESULT_SESION_EXPIRADA) {
                    sesionExpirada();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historias);
        BarrasSistema.aplicar(findViewById(R.id.raiz));

        token = Sesion.bearer(this);
        if (token == null) {
            sesionExpirada();
            return;
        }
        if (!Sesion.esProfesional(this)) { // defensa: un paciente no debería llegar acá
            finish();
            return;
        }

        pbHistorias = findViewById(R.id.pbHistorias);
        tvEstadoHistorias = findViewById(R.id.tvEstadoHistorias);
        btnNuevaNota = findViewById(R.id.btnNuevaNota);
        ListView lv = findViewById(R.id.lvHistorias);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        lv.setAdapter(adapter);

        findViewById(R.id.btnVolverHistorias).setOnClickListener(v -> finish());
        btnNuevaNota.setOnClickListener(v -> {
            Intent intent = new Intent(this, NuevaNotaActivity.class);
            intent.putExtras(getIntent()); // reenvía los pacientes
            launcherNota.launch(intent);
        });

        vm = new ViewModelProvider(this).get(HistoriasViewModel.class);
        vm.getEstado().observe(this, this::mostrar);
        vm.cargar(token);
    }

    private void mostrar(Estado<RespuestaHistorias> e) {
        boolean cargando = e.esCargando();
        pbHistorias.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnNuevaNota.setEnabled(!cargando);

        switch (e.tipo) {
            case OK:
                llenar(e.datos.getHistorias());
                break;
            case SESION_EXPIRADA:
                sesionExpirada();
                break;
            case CARGANDO:
                tvEstadoHistorias.setVisibility(View.GONE);
                break;
            default: // ERROR_API o SIN_CONEXION
                tvEstadoHistorias.setText(e.mensaje);
                tvEstadoHistorias.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void llenar(List<Historia> historias) {
        items.clear();
        if (historias != null) {
            for (Historia h : historias) items.add(formatear(h));
        }
        adapter.notifyDataSetChanged();

        if (items.isEmpty()) {
            tvEstadoHistorias.setText(R.string.historias_vacio);
            tvEstadoHistorias.setVisibility(View.VISIBLE);
        } else {
            tvEstadoHistorias.setVisibility(View.GONE);
        }
    }

    private String formatear(Historia h) {
        String paciente = getString(R.string.turno_paciente_label, h.getNombrePaciente());
        return getString(R.string.historia_item_formato, h.getFechaHora(), paciente, h.getNotas());
    }

    private void sesionExpirada() {
        setResult(RESULT_SESION_EXPIRADA);
        finish();
    }
}
