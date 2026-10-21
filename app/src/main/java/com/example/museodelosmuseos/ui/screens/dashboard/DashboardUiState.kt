package com.example.museodelosmuseos.ui.screens.dashboard

import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.local.entity.SesionActiva

/** Pestañas de la barra de navegación inferior del Dashboard */
enum class TabDashboard {
    INICIO,
    MAPA,
    CATALOGO,
    NOTIFICACIONES,
    PERFIL
}

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: DashboardUiState
 * ============================================================================
 * Estado inmutable de la pantalla de Dashboard (UDF).
 * ============================================================================
 */
data class DashboardUiState(
    val sesion: SesionActiva? = null,
    val museosDestacados: List<Museo> = emptyList(),
    val todosLosMuseos: List<Museo> = emptyList(),
    val tabSeleccionada: TabDashboard = TabDashboard.INICIO,
    val animacionLista: Boolean = false
) {
    val puedeAccederCatalogo: Boolean
        get() = sesion?.rol?.equals("ADMINISTRADOR", ignoreCase = true) == true ||
                sesion?.rol?.equals("CLIENTE", ignoreCase = true) == true
}
