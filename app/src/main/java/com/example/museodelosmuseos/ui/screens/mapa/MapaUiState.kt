package com.example.museodelosmuseos.ui.screens.mapa

import com.example.museodelosmuseos.data.local.entity.Museo

data class MapaUiState(

    val todosLosMuseos: List<Museo> = emptyList(),

    val museosFiltrados: List<Museo> = emptyList(),

    val listaPaises: List<String> = emptyList(),

    val listaProyectos: List<String> = emptyList(),

    val paisSeleccionado: String = "Todos",

    val proyectoSeleccionado: String = "Todos",

    val museoSeleccionado: Museo? = null,

    val sincronizando: Boolean = false,

    val mensaje: String? = null
)
