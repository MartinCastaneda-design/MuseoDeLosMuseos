package com.example.museodelosmuseos.ui.screens.escaner

import com.example.museodelosmuseos.data.local.entity.HistorialQr
import com.example.museodelosmuseos.data.local.entity.SesionActiva

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: EscanerQrUiState
 * ============================================================================
 * Estado inmutable de la Pantalla 7: Escáner QR.
 * Contiene la sesión activa (Investigador o Cliente), el estado de lectura,
 * el historial de códigos escaneados guardados en SQLite (Room)
 * y la lista de códigos administrados para pruebas de Investigador.
 * ============================================================================
 */
data class EscanerQrUiState(
    /** Sesión activa para identificar al usuario */
    val sesion: SesionActiva? = null,
    /** Indica si se está validando un código en segundo plano */
    val procesando: Boolean = false,
    /** Mensaje de retroalimentación o error */
    val mensajeError: String? = null,
    /** Historial reactivo de lecturas del usuario activo desde Room */
    val historialLecturas: List<HistorialQr> = emptyList(),
    /** Códigos de museos disponibles para que el Investigador los pruebe directamente */
    val codigosParaPrueba: List<String> = emptyList()
)
