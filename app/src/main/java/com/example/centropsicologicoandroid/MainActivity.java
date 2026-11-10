package com.example.centropsicologicoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.centropsicologico.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lista de turnos del usuario. El paciente solicita turnos; el profesional accede a sus historias. */
public class MainActivity extends AppCompatActivity {

    private MainViewModel vm;
    private String token;
    private boolean esProfesional;

    private ListView lvTurnos;
    private ProgressBar pbCargando;
    private TextView tvEstadoApi;
    private Button btnSolicitarTurno, btnHistorias;
    private ArrayAdapter<String> adapter;

    private final List<String> listaFormateada = new ArrayList<>();
    private final List<Turno> turnosActuales = new ArrayList<>();

    private boolean cargandoTurnos, cargandoProfesionales, enviandoSolicitud;

    // Resultado de DetalleTurnoActivity (Activity Result API)
    private final ActivityResultLauncher<Intent> launcherDetalle = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    vm.recargarTurnos(token);
                } else if (result.getResultCode() == DetalleTurnoActivity.RESULT_SESION_EXPIRADA) {
                    cerrarSesion();
                }
            });

    private final ActivityResultLauncher<Intent> launcherHistorias = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == HistoriasActivity.RESULT_SESION_EXPIRADA) cerrarSesion();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BarrasSistema.aplicar(findViewById(R.id.raiz));

        // Sin token o sin perfil (sesión vieja): volver a iniciar sesión
        token = Sesion.bearer(this);
        if (token == null || Sesion.perfilId(this) == 0) {
            cerrarSesion();
            return;
        }
        esProfesional = Sesion.esProfesional(this);

        lvTurnos = findViewById(R.id.lvTurnos);
        pbCargando = findViewById(R.id.pbCargando);
        tvEstadoApi = findViewById(R.id.tvEstadoApi);
        btnSolicitarTurno = findViewById(R.id.btnSolicitarTurno);
        btnHistorias = findViewById(R.id.btnHistorias);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaFormateada);
        lvTurnos.setAdapter(adapter);
        // Se abre el detalle por id: la pantalla pide el turno a la API
        lvTurnos.setOnItemClickListener((parent, view, position, id) ->
                launcherDetalle.launch(DetalleTurnoActivity.nuevoIntent(this, turnosActuales.get(position).getId())));

        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> cerrarSesion());

        if (esProfesional) {
            btnSolicitarTurno.setVisibility(View.GONE);
            btnHistorias.setOnClickListener(v -> abrirHistorias());
        } else {
            // Las historias clínicas las ve solo el profesional que las escribe
            btnHistorias.setVisibility(View.GONE);
            btnSolicitarTurno.setOnClickListener(v -> vm.pedirProfesionales(token));
        }

        vm = new ViewModelProvider(this).get(MainViewModel.class);
        vm.getTurnos().observe(this, this::mostrarTurnos);
        vm.getProfesionales().observe(this, this::alRecibirProfesionales);
        vm.getSolicitud().observe(this, this::alSolicitarTurno);
        vm.cargarTurnos(token);
    }

    // ---------------- LISTAR TURNOS ----------------

    private void mostrarTurnos(Estado<RespuestaTurnos> e) {
        cargandoTurnos = e.esCargando();
        actualizarOcupado();

        switch (e.tipo) {
            case OK:
                llenarLista(e.datos.getTurnos());
                break;
            case SESION_EXPIRADA:
                cerrarSesion();
                break;
            case CARGANDO:
                tvEstadoApi.setVisibility(View.GONE);
                break;
            default: // ERROR_API o SIN_CONEXION: se muestra el mensaje de la API o "sin conexión"
                tvEstadoApi.setText(e.mensaje);
                tvEstadoApi.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void llenarLista(List<Turno> turnos) {
        listaFormateada.clear();
        turnosActuales.clear();
        if (turnos != null) {
            for (Turno t : turnos) {
                turnosActuales.add(t);
                listaFormateada.add(formatearTurno(t));
            }
        }
        adapter.notifyDataSetChanged();

        if (turnosActuales.isEmpty()) {
            tvEstadoApi.setText(R.string.no_hay_turnos);
            tvEstadoApi.setVisibility(View.VISIBLE);
        } else {
            tvEstadoApi.setVisibility(View.GONE);
        }
    }

    private String formatearTurno(Turno t) {
        StringBuilder sb = new StringBuilder(getString(R.string.info_turno_formato,
                t.getId(), t.getFechaHoraSolicitada(), t.getEstado().toUpperCase()));

        String contraparte = esProfesional ? t.getNombrePaciente() : t.getNombreProfesional();
        if (contraparte != null && !contraparte.isEmpty()) {
            sb.append("\n").append(getString(
                    esProfesional ? R.string.turno_paciente_label : R.string.turno_profesional_label, contraparte));
        }
        String alt = t.getOpcionHorarioAlternativo();
        if (alt != null && !alt.isEmpty()) {
            sb.append("\n").append(getString(R.string.turno_alternativo_label, alt));
        }
        return sb.toString();
    }

    // ---------------- SOLICITAR TURNO (PACIENTE) ----------------

    private void alRecibirProfesionales(Estado<RespuestaProfesionales> e) {
        cargandoProfesionales = e.esCargando();
        actualizarOcupado();

        switch (e.tipo) {
            case OK:
                if (!e.consumir()) break; // el diálogo se muestra una sola vez
                List<Profesional> lista = e.datos.getProfesionales();
                if (lista == null || lista.isEmpty()) {
                    Toast.makeText(this, R.string.msg_sin_profesionales, Toast.LENGTH_LONG).show();
                } else {
                    mostrarProfesionales(lista);
                }
                break;
            case SESION_EXPIRADA:
                cerrarSesion();
                break;
            case CARGANDO:
                break;
            default:
                if (e.consumir()) Toast.makeText(this, e.mensaje, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void mostrarProfesionales(List<Profesional> lista) {
        String[] nombres = new String[lista.size()];
        for (int i = 0; i < lista.size(); i++) {
            nombres[i] = lista.get(i).toString();
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialogo_elegir_profesional)
                .setItems(nombres, (d, i) -> {
                    Profesional p = lista.get(i);
                    Utilidades.elegirFechaHora(this, fecha -> vm.solicitar(token, p.getId(), fecha));
                })
                .show();
    }

    private void alSolicitarTurno(Estado<RespuestaApi> e) {
        enviandoSolicitud = e.esCargando();
        actualizarOcupado();

        switch (e.tipo) {
            case OK:
                if (e.consumir()) {
                    Toast.makeText(this, R.string.msg_turno_solicitado, Toast.LENGTH_LONG).show();
                    vm.recargarTurnos(token);
                }
                break;
            case SESION_EXPIRADA:
                cerrarSesion();
                break;
            case CARGANDO:
                break;
            default: // 400, 404, 409, 422...: mensaje de la API; o sin conexión
                if (e.consumir()) Toast.makeText(this, e.mensaje, Toast.LENGTH_LONG).show();
                break;
        }
    }

    // ---------------- HISTORIAS (PROFESIONAL) ----------------

    private void abrirHistorias() {
        Intent intent = new Intent(this, HistoriasActivity.class);
        // Sus pacientes salen de sus turnos (el backend solo deja escribir sobre ellos)
        Map<Integer, String> pacientes = new LinkedHashMap<>();
        for (Turno t : turnosActuales) pacientes.put(t.getPacienteId(), t.getNombrePaciente());
        intent.putIntegerArrayListExtra(HistoriasActivity.EXTRA_PACIENTE_IDS, new ArrayList<>(pacientes.keySet()));
        intent.putStringArrayListExtra(HistoriasActivity.EXTRA_PACIENTE_NOMBRES, new ArrayList<>(pacientes.values()));
        launcherHistorias.launch(intent);
    }

    // ---------------- ESTADO DE LA PANTALLA ----------------

    /** Muestra "cargando" y deshabilita botones y lista para no enviar dos veces. */
    private void actualizarOcupado() {
        boolean ocupado = cargandoTurnos || cargandoProfesionales || enviandoSolicitud;
        pbCargando.setVisibility(ocupado ? View.VISIBLE : View.GONE);
        btnSolicitarTurno.setEnabled(!ocupado);
        btnHistorias.setEnabled(!ocupado);
        lvTurnos.setEnabled(!ocupado);
    }

    private void cerrarSesion() {
        Sesion.limpiar(this);
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
