package com.example.museodelosmuseos.data.remote.microservicio

import com.google.gson.annotations.SerializedName

data class SolicitudCulturalDto(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("titulo")
    val titulo: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("tipoSolicitud")
    val tipoSolicitud: String,

    @SerializedName("museoNombre")
    val museoNombre: String,

    @SerializedName("solicitanteNombre")
    val solicitanteNombre: String,

    @SerializedName("solicitanteCorreo")
    val solicitanteCorreo: String,

    @SerializedName("estado")
    val estado: String = "PENDIENTE",

    @SerializedName("respuesta")
    val respuesta: String? = null
)
