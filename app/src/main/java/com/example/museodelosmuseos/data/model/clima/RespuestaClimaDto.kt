package com.example.museodelosmuseos.data.model.clima

import com.google.gson.annotations.SerializedName

data class RespuestaClimaDto(
    @SerializedName("latitude")
    val latitud: Double,

    @SerializedName("longitude")
    val longitud: Double,

    @SerializedName("current")
    val actual: ClimaActualDto? = null
)

data class ClimaActualDto(
    @SerializedName("time")
    val hora: String = "",

    @SerializedName("temperature_2m")
    val temperatura: Double = 0.0,

    @SerializedName("relative_humidity_2m")
    val humedad: Int = 0,

    @SerializedName("weather_code")
    val codigoClima: Int = 0,

    @SerializedName("wind_speed_10m")
    val velocidadViento: Double = 0.0
)
