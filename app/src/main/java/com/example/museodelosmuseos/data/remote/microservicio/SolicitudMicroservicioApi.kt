package com.example.museodelosmuseos.data.remote.microservicio

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SolicitudMicroservicioApi {

    @GET("api/solicitudes")
    suspend fun listarSolicitudes(): List<SolicitudCulturalDto>

    @GET("api/solicitudes/{id}")
    suspend fun obtenerSolicitudPorId(@Path("id") id: Long): SolicitudCulturalDto

    @POST("api/solicitudes")
    suspend fun crearSolicitud(@Body solicitud: SolicitudCulturalDto): SolicitudCulturalDto

    @PUT("api/solicitudes/{id}")
    suspend fun actualizarSolicitud(@Path("id") id: Long, @Body solicitud: SolicitudCulturalDto): SolicitudCulturalDto

    @DELETE("api/solicitudes/{id}")
    suspend fun eliminarSolicitud(@Path("id") id: Long): Response<Unit>
}
