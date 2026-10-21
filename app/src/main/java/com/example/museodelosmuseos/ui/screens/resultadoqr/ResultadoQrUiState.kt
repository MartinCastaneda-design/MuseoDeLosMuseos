package com.example.museodelosmuseos.ui.screens.resultadoqr

import com.example.museodelosmuseos.data.local.entity.Museo

data class ResultadoQrUiState(
    val codigo: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val esValido: Boolean = true,
    val museoId: Long? = null,
    val museo: Museo? = null,
    val animacionLista: Boolean = false,
    val cargando: Boolean = false
)
