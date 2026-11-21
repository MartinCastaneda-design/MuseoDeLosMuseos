package com.example.museodelosmuseos.data.repository

import com.example.museodelosmuseos.data.remote.microservicio.RetrofitMicroservicioInstance
import com.example.museodelosmuseos.data.remote.microservicio.SolicitudCulturalDto
import com.example.museodelosmuseos.data.remote.microservicio.SolicitudMicroservicioApi

class MicroservicioRepository(
    private var api: SolicitudMicroservicioApi = RetrofitMicroservicioInstance.api
) {

    fun cambiarBaseUrl(nuevaUrl: String) {
        RetrofitMicroservicioInstance.configurarBaseUrl(nuevaUrl)
        api = RetrofitMicroservicioInstance.api
    }

    fun obtenerBaseUrl(): String = RetrofitMicroservicioInstance.obtenerBaseUrl()

    suspend fun listarSolicitudes(): Result<List<SolicitudCulturalDto>> {
        return try {
            val lista = api.listarSolicitudes()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerSolicitud(id: Long): Result<SolicitudCulturalDto> {
        return try {
            val solicitud = api.obtenerSolicitudPorId(id)
            Result.success(solicitud)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearSolicitud(solicitud: SolicitudCulturalDto): Result<SolicitudCulturalDto> {
        return try {
            val creada = api.crearSolicitud(solicitud)
            Result.success(creada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarSolicitud(id: Long, solicitud: SolicitudCulturalDto): Result<SolicitudCulturalDto> {
        return try {
            val actualizada = api.actualizarSolicitud(id, solicitud)
            Result.success(actualizada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarSolicitud(id: Long): Result<Boolean> {
        return try {
            val respuesta = api.eliminarSolicitud(id)
            if (respuesta.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(IllegalStateException("HTTP ${respuesta.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
