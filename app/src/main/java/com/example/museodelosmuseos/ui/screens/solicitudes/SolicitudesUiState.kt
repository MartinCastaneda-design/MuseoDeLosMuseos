package com.example.museodelosmuseos.ui.screens.solicitudes

import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.data.remote.microservicio.SolicitudCulturalDto

data class SolicitudesUiState(
    val solicitudes: List<SolicitudHistorial> = emptyList(),
    val solicitudesRemotas: List<SolicitudCulturalDto> = emptyList(),
    val modoMicroservicio: Boolean = true,
    val cargando: Boolean = true,
    val cargandoRemoto: Boolean = false,
    val esAdministrador: Boolean = false,
    val filtro: EstadoSolicitud? = null,
    val expandida: String? = null,
    val editando: SolicitudHistorial? = null,
    val editandoRemota: SolicitudCulturalDto? = null,
    val mostrandoDialogoCrear: Boolean = false,
    val mostrandoDialogoServidor: Boolean = false,
    val nuevoEstado: EstadoSolicitud? = null,
    val nuevoEstadoRemoto: String? = null,
    val nuevaRespuestaRemota: String = "",
    val guardando: Boolean = false,
    val error: String? = null,
    val errorRemoto: String? = null,
    val confirmacion: String? = null,
    val servidorConectado: Boolean = false,
    val urlServidor: String = ""
) {
    val filtradas: List<SolicitudHistorial> get() = solicitudes.filter { filtro == null || EstadoSolicitud.desde(it.estado) == filtro }
    val filtradasRemotas: List<SolicitudCulturalDto> get() = solicitudesRemotas.filter { filtro == null || it.estado.equals(filtro.name, ignoreCase = true) }
}
