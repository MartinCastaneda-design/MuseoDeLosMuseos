package com.example.museodelosmuseos.data.repository

import com.example.museodelosmuseos.data.model.clima.InfoClima
import com.example.museodelosmuseos.data.model.clima.InterpretadorClima
import com.example.museodelosmuseos.data.remote.clima.ClimaApiService
import com.example.museodelosmuseos.data.remote.clima.RetrofitClimaInstance

class ClimaRepository(
    private val api: ClimaApiService = RetrofitClimaInstance.api
) {

    suspend fun consultarClimaCiudad(latitud: Double, longitud: Double): Result<InfoClima> {
        return try {
            val respuesta = api.obtenerClimaActual(latitud = latitud, longitud = longitud)
            val actual = respuesta.actual ?: throw IllegalStateException("Respuesta sin datos climáticos")

            val (condicion, icono) = InterpretadorClima.interpretar(actual.codigoClima)
            val recomendacion = InterpretadorClima.sugerirVisita(actual.codigoClima, actual.temperatura)

            val infoClima = InfoClima(
                temperatura = actual.temperatura,
                condicion = condicion,
                icono = icono,
                humedad = actual.humedad,
                vientoKmH = actual.velocidadViento,
                recomendacionVisita = recomendacion
            )

            Result.success(infoClima)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
