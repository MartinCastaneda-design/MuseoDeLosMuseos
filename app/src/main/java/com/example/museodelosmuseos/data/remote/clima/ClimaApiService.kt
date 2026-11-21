package com.example.museodelosmuseos.data.remote.clima

import com.example.museodelosmuseos.data.model.clima.RespuestaClimaDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ClimaApiService {

    @GET("v1/forecast")
    suspend fun obtenerClimaActual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m"
    ): RespuestaClimaDto
}
