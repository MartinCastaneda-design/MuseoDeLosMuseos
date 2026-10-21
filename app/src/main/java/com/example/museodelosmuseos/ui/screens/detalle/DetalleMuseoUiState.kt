package com.example.museodelosmuseos.ui.screens.detalle

import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.local.entity.SesionActiva

data class DetalleMuseoUiState(

    val museo: Museo? = null,

    val sesion: SesionActiva? = null,

    val esFavorito: Boolean = false,

    val mostrarDialogoEdicion: Boolean = false,

    val descripcionEditada: String = "",

    val notasInvestigacion: String = "",

    val mostrarDialogoRevision: Boolean = false,

    val fichaPublicada: Boolean = true,

    val mensaje: String? = null,

    val cargando: Boolean = true
)
