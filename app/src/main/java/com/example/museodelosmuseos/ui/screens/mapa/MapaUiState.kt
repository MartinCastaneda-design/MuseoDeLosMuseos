package com.example.museodelosmuseos.ui.screens.mapa

import com.example.museodelosmuseos.data.local.entity.Museo

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: MapaUiState
 * ============================================================================
 * Estado inmutable de la Pantalla 5: Mapa de Museos Virtuales.
 * Contiene el listado de museos, filtros activos (país y proyecto/clasificación),
 * museo seleccionado en el mapa y estado de sincronización con la API REST ficticia.
 * ============================================================================
 */
data class MapaUiState(
    /** Todos los museos en caché local SQLite */
    val todosLosMuseos: List<Museo> = emptyList(),
    /** Museos que coinciden con los filtros aplicados */
    val museosFiltrados: List<Museo> = emptyList(),
    /** Países disponibles para filtrar */
    val listaPaises: List<String> = emptyList(),
    /** Clasificaciones o proyectos disponibles para filtrar */
    val listaProyectos: List<String> = emptyList(),
    /** País actualmente seleccionado ("Todos" por defecto) */
    val paisSeleccionado: String = "Todos",
    /** Proyecto o clasificación actualmente seleccionada ("Todos" por defecto) */
    val proyectoSeleccionado: String = "Todos",
    /** Museo cuyo marcador fue pulsado en el mapa (null si ninguno está seleccionado) */
    val museoSeleccionado: Museo? = null,
    /** Indica si se está consultando la API REST ficticia */
    val sincronizando: Boolean = false,
    /** Mensaje informativo o de error */
    val mensaje: String? = null
)
