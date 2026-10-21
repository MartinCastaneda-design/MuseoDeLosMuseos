package com.example.museodelosmuseos.ui.screens.favoritos

import com.example.museodelosmuseos.data.local.entity.Museo

data class FavoritosUiState(
    val museos: List<Museo> = emptyList(), val cargando: Boolean = true,
    val busqueda: String = "", val porQuitar: Museo? = null, val guardando: Boolean = false,
    val error: String? = null, val confirmacion: String? = null, val enlacePendiente: String? = null
) {
    val filtrados: List<Museo> get() = museos.filter {
        val texto = busqueda.trim()
        texto.isEmpty() || it.nombre.contains(texto, true) || it.pais.contains(texto, true) || it.clasificacion.contains(texto, true)
    }
}
