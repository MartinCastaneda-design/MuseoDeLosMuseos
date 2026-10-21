package com.example.museodelosmuseos.ui.screens.escaner

import com.example.museodelosmuseos.data.local.entity.HistorialQr
import com.example.museodelosmuseos.data.local.entity.SesionActiva

data class EscanerQrUiState(

    val sesion: SesionActiva? = null,

    val procesando: Boolean = false,

    val mensajeError: String? = null,

    val historialLecturas: List<HistorialQr> = emptyList(),

    val codigosParaPrueba: List<String> = emptyList()
)
