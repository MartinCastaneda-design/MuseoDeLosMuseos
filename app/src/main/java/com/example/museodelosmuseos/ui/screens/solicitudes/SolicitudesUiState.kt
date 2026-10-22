package com.example.museodelosmuseos.ui.screens.solicitudes

import com.example.museodelosmuseos.data.model.*

data class SolicitudesUiState(
    val solicitudes: List<SolicitudHistorial> = emptyList(), val cargando: Boolean = true,
    val esAdministrador: Boolean = false, val filtro: EstadoSolicitud? = null,
    val expandida: String? = null, val editando: SolicitudHistorial? = null,
    val nuevoEstado: EstadoSolicitud? = null, val guardando: Boolean = false,
    val error: String? = null, val confirmacion: String? = null
) {
    val filtradas: List<SolicitudHistorial> get() = solicitudes.filter { filtro == null || EstadoSolicitud.desde(it.estado) == filtro }
}
