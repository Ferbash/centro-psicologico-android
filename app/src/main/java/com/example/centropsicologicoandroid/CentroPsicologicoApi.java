package com.example.centropsicologicoandroid;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/** Toda la API REST del TP N.º 1 descripta en un solo lugar. */
public interface CentroPsicologicoApi {

    // ----- Autenticación -----
    @POST("login.php")
    Call<RespuestaLogin> login(@Body SolicitudLogin solicitud);

    @POST("registrar.php")
    Call<RespuestaApi> registrar(@Body SolicitudRegistro solicitud);

    // ----- Profesionales -----
    @GET("obtener_profesionales.php")
    Call<RespuestaProfesionales> obtenerProfesionales(@Header("Authorization") String token);

    // ----- Turnos (el backend filtra por el usuario del token) -----
    @GET("gestionar_turnos.php")
    Call<RespuestaTurnos> obtenerTurnos(@Header("Authorization") String token);

    /** Detalle por id: 404 si no existe o no es del usuario. */
    @GET("gestionar_turnos.php")
    Call<RespuestaTurno> obtenerTurno(@Header("Authorization") String token, @Query("id") int id);

    @POST("gestionar_turnos.php")
    Call<RespuestaApi> solicitarTurno(@Header("Authorization") String token, @Body SolicitudTurno solicitud);

    @PUT("gestionar_turnos.php")
    Call<RespuestaApi> cambiarEstado(@Header("Authorization") String token, @Body SolicitudEstado solicitud);

    @DELETE("gestionar_turnos.php")
    Call<RespuestaApi> eliminarTurno(@Header("Authorization") String token, @Query("id") int id);

    // ----- Historias clínicas (solo las usa el profesional) -----
    @GET("historias_clinicas.php")
    Call<RespuestaHistorias> obtenerHistorias(@Header("Authorization") String token);

    @POST("historias_clinicas.php")
    Call<RespuestaApi> guardarNota(@Header("Authorization") String token, @Body SolicitudNota solicitud);
}
