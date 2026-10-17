package com.example.museodelosmuseos.ui.screens.detalle

import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.local.entity.SesionActiva

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: DetalleMuseoUiState
 * ============================================================================
 * Estado inmutable de la Pantalla 6: Detalle del Museo Virtual.
 * Contiene el museo consultado, el estado de favorito del usuario,
 * la sesión activa (para mostrar las acciones correspondientes a su rol),
 * y el estado de los formularios de edición/revisión.
 * ============================================================================
 */
data class DetalleMuseoUiState(
    /** Museo que se está visualizando */
    val museo: Museo? = null,
    /** Sesión activa del usuario */
    val sesion: SesionActiva? = null,
    /** Indica si el museo actual está marcado como favorito por este usuario */
    val esFavorito: Boolean = false,
    /** Controla si está abierto el diálogo de edición de ficha para el Investigador */
    val mostrarDialogoEdicion: Boolean = false,
    /** Campo editable: descripción del museo para corrección del Investigador */
    val descripcionEditada: String = "",
    /** Campo editable: notas de investigación museológica */
    val notasInvestigacion: String = "",
    /** Controla si está abierto el diálogo de revisión para el Administrador */
    val mostrarDialogoRevision: Boolean = false,
    /** Indica si la ficha fue aprobada y publicada por el Administrador */
    val fichaPublicada: Boolean = true,
    /** Mensaje de confirmación o alerta (Snackbar) */
    val mensaje: String? = null,
    /** Estado de carga */
    val cargando: Boolean = true
)
