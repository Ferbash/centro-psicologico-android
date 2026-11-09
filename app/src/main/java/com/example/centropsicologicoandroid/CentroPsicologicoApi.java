package com.example.centropsicologicoandroid;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface CentroPsicologicoApi {

    @POST("login.php")
    Call<RespuestaLogin> login(@Body SolicitudLogin solicitud);

    @POST("registrar.php")
    Call<RespuestaApi> registrar(@Body SolicitudRegistro solicitud);

    @GET("gestionar_turnos.php")
    Call<RespuestaTurnos> obtenerTurnos(@Header("Authorization") String token);
}
