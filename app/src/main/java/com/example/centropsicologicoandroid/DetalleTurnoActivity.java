package com.example.centropsicologicoandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.centropsicologico.R;

/**
 * Detalle de un turno. Recibe solo el id por extra y pide el turno a la API (GET ?id=X);
 * así se ve siempre el dato actual y se maneja el caso "el turno ya no existe" (404).
 */
public class DetalleTurnoActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "turno_id";
    public static final int RESULT_SESION_EXPIRADA = RESULT_FIRST_USER;

    private DetalleTurnoViewModel vm;
    private String token;
    private int turnoId;
    private boolean esProfesional;

    private Turno turnoActual;       // null hasta que llega de la API
    private boolean accionEnCurso;

    private TextView tvFecha, tvEstado, tvContraparte, tvAlternativa, tvError;
    private ProgressBar pbDetalle;
    private Button btnAceptar, btnReprogramar, btnCancelar, btnEliminar;

    /** Arma el Intent: el dato que viaja como extra es el id del turno. */
    public static Intent nuevoIntent(Context contexto, int turnoId) {
        return new Intent(contexto, DetalleTurnoActivity.class).putExtra(EXTRA_ID, turnoId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_turno);
        BarrasSistema.aplicar(findViewById(R.id.raiz));

        token = Sesion.bearer(this);
        if (token == null) {
            setResult(RESULT_SESION_EXPIRADA);
            finish();
            return;
        }
        turnoId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (turnoId == -1) {
            finish();
            return;
        }
        esProfesional = Sesion.esProfesional(this);

        ((TextView) findViewById(R.id.tvDetalleTitulo)).setText(getString(R.string.detalle_titulo, turnoId));
        tvFecha = findViewById(R.id.tvDetalleFecha);
        tvEstado = findViewById(R.id.tvDetalleEstado);
        tvContraparte = findViewById(R.id.tvDetalleContraparte);
        tvAlternativa = findViewById(R.id.tvDetalleAlternativa);
        tvError = findViewById(R.id.tvDetalleError);
        pbDetalle = findViewById(R.id.pbDetalle);
        btnAceptar = findViewById(R.id.btnAceptar);
        btnReprogramar = findViewById(R.id.btnReprogramar);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnEliminar = findViewById(R.id.btnEliminar);
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());

        btnAceptar.setOnClickListener(v ->
                vm.cambiarEstado(token, turnoId, Turno.ESTADO_ACEPTADO, null));
        btnReprogramar.setOnClickListener(v -> Utilidades.elegirFechaHora(this, fecha ->
                vm.cambiarEstado(token, turnoId, Turno.ESTADO_REPROGRAMADO, fecha)));
        btnCancelar.setOnClickListener(v ->
                vm.cambiarEstado(token, turnoId, Turno.ESTADO_CANCELADO, null));
        btnEliminar.setOnClickListener(v -> confirmarEliminar());

        aplicarBotones(); // arranca sin botones hasta que llegue el turno

        vm = new ViewModelProvider(this).get(DetalleTurnoViewModel.class);
        vm.getTurno().observe(this, this::mostrarCarga);
        vm.getAccion().observe(this, this::mostrarAccion);
        vm.cargar(token, turnoId);
    }

    // ---------------- CARGA DEL TURNO (GET ?id=) ----------------

    private void mostrarCarga(Estado<RespuestaTurno> e) {
        pbDetalle.setVisibility(e.esCargando() || accionEnCurso ? View.VISIBLE : View.GONE);

        switch (e.tipo) {
            case OK:
                if (e.datos.getTurno() != null) {
                    tvError.setVisibility(View.GONE);
                    mostrarTurno(e.datos.getTurno());
                } else {
                    mostrarErrorCarga(getString(R.string.error_respuesta_invalida));
                }
                break;
            case SESION_EXPIRADA:
                setResult(RESULT_SESION_EXPIRADA);
                finish();
                break;
            case CARGANDO:
                tvError.setVisibility(View.GONE);
                break;
            default: // 404 "El turno no existe", 403, sin conexión...
                mostrarErrorCarga(e.mensaje);
                break;
        }
    }

    private void mostrarErrorCarga(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
        turnoActual = null;
        aplicarBotones();
        setResult(RESULT_OK); // al volver, la lista se actualiza (el turno pudo haberse borrado)
    }

    private void mostrarTurno(Turno t) {
        turnoActual = t;
        tvFecha.setText(getString(R.string.detalle_fecha, t.getFechaHoraSolicitada()));
        tvEstado.setText(getString(R.string.detalle_estado, t.getEstado().toUpperCase()));

        String contraparte = esProfesional ? t.getNombrePaciente() : t.getNombreProfesional();
        if (contraparte != null && !contraparte.isEmpty()) {
            tvContraparte.setText(getString(
                    esProfesional ? R.string.turno_paciente_label : R.string.turno_profesional_label, contraparte));
            tvContraparte.setVisibility(View.VISIBLE);
        } else {
            tvContraparte.setVisibility(View.GONE);
        }

        String alternativa = t.getOpcionHorarioAlternativo();
        if (alternativa != null && !alternativa.isEmpty()) {
            tvAlternativa.setText(getString(R.string.turno_alternativo_label, alternativa));
            tvAlternativa.setVisibility(View.VISIBLE);
        } else {
            tvAlternativa.setVisibility(View.GONE);
        }
        aplicarBotones();
    }

    // ---------------- ACCIONES (PUT / DELETE) ----------------

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialogo_eliminar_titulo)
                .setMessage(R.string.dialogo_eliminar_mensaje)
                .setPositiveButton(R.string.btn_eliminar_confirmar, (d, w) -> vm.eliminar(token, turnoId))
                .setNegativeButton(R.string.btn_volver, null)
                .show();
    }

    private void mostrarAccion(Estado<RespuestaApi> e) {
        accionEnCurso = e.esCargando();
        pbDetalle.setVisibility(accionEnCurso ? View.VISIBLE : View.GONE);
        aplicarBotones();

        switch (e.tipo) {
            case OK:
                if (e.consumir()) {
                    String msg = e.datos.getMensaje();
                    Toast.makeText(this, msg != null ? msg : getString(R.string.msg_turno_actualizado),
                            Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            case SESION_EXPIRADA:
                setResult(RESULT_SESION_EXPIRADA);
                finish();
                break;
            case CARGANDO:
                break;
            default: // 403, 404, 422... o sin conexión: se muestra el mensaje y la pantalla sigue
                if (e.consumir()) Toast.makeText(this, e.mensaje, Toast.LENGTH_LONG).show();
                break;
        }
    }

    // ---------------- BOTONES ----------------

    /** Muestra solo los botones que corresponden al rol y al estado, y los deshabilita al enviar. */
    private void aplicarBotones() {
        boolean hayTurno = turnoActual != null;
        boolean cancelado = hayTurno && Turno.ESTADO_CANCELADO.equalsIgnoreCase(turnoActual.getEstado());

        mostrar(btnAceptar, hayTurno && esProfesional && !cancelado);
        mostrar(btnReprogramar, hayTurno && esProfesional && !cancelado);
        mostrar(btnCancelar, hayTurno && !cancelado);
        mostrar(btnEliminar, hayTurno && !esProfesional);

        boolean habilitado = !accionEnCurso;
        btnAceptar.setEnabled(habilitado);
        btnReprogramar.setEnabled(habilitado);
        btnCancelar.setEnabled(habilitado);
        btnEliminar.setEnabled(habilitado);
    }

    private void mostrar(View vista, boolean visible) {
        vista.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
