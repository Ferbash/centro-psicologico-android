package com.example.centropsicologicoandroid;

import android.content.Context;
import android.content.SharedPreferences;

/** Único lugar que lee y escribe la sesión (token JWT, rol y perfil). */
final class Sesion {

    private static final String ARCHIVO = "auth";
    private static final String K_TOKEN = "token";
    private static final String K_ROL = "rol";
    private static final String K_PERFIL = "perfil_id";

    static final String ROL_PROFESIONAL = "profesional";
    static final String ROL_PACIENTE = "paciente";

    private Sesion() { }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    static void guardar(Context c, String token, Usuario u) {
        prefs(c).edit()
                .putString(K_TOKEN, token)
                .putString(K_ROL, u != null && u.getRol() != null ? u.getRol() : ROL_PACIENTE)
                .putInt(K_PERFIL, u != null ? u.getPerfilId() : 0)
                .apply();
    }

    /** "Bearer xxx" listo para el header Authorization, o null si no hay sesión. */
    static String bearer(Context c) {
        String jwt = prefs(c).getString(K_TOKEN, null);
        return jwt == null ? null : "Bearer " + jwt;
    }

    static boolean esProfesional(Context c) {
        return ROL_PROFESIONAL.equalsIgnoreCase(prefs(c).getString(K_ROL, ROL_PACIENTE));
    }

    static int perfilId(Context c) {
        return prefs(c).getInt(K_PERFIL, 0);
    }

    static void limpiar(Context c) {
        prefs(c).edit().remove(K_TOKEN).remove(K_ROL).remove(K_PERFIL).apply();
    }
}
