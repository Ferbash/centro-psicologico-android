package com.example.centropsicologicoandroid;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiCliente {
    private static final String URL_BASE = "https://centro-psicologico-ldrf.onrender.com/api/";
    private static CentroPsicologicoApi apiService;

    public static CentroPsicologicoApi getService() {
        if (apiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL_BASE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = retrofit.create(CentroPsicologicoApi.class);
        }
        return apiService;
    }
}