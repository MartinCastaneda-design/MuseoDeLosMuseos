package com.example.museodelosmuseos.ui.screens.dashboard

import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.local.entity.SesionActiva

enum class TabDashboard {
    INICIO,
    MAPA,
    CATALOGO,
    NOTIFICACIONES,
    PERFIL
}

data class DashboardUiState(
    val sesion: SesionActiva? = null,
    val museosDestacados: List<Museo> = emptyList(),
    val todosLosMuseos: List<Museo> = emptyList(),
    val tabSeleccionada: TabDashboard = TabDashboard.INICIO,
    val animacionLista: Boolean = false,
    val avisosNoLeidos: Int = 0
) {
    val puedeAccederCatalogo: Boolean
        get() = sesion?.rol?.equals("ADMINISTRADOR", ignoreCase = true) == true ||
                sesion?.rol?.equals("CLIENTE", ignoreCase = true) == true
}
