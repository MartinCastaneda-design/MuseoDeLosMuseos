package com.example.museodelosmuseos.ui.screens.resultadoqr

import com.example.museodelosmuseos.data.local.entity.Museo

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: ResultadoQrUiState
 * ============================================================================
 * Estado inmutable de la Pantalla 8: Resultado QR.
 * Contiene el código escaneado, la validez del resultado, la entidad Museo
 * encontrada (si aplica), la descripción e imagen, y el disparador de animación.
 * ============================================================================
 */
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
