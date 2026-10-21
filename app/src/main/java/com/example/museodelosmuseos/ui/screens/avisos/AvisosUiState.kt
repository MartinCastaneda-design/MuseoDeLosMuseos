package com.example.museodelosmuseos.ui.screens.avisos

import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.local.entity.SesionActiva
import com.example.museodelosmuseos.data.model.*

enum class FiltroAvisos(val etiqueta: String) { TODOS("Todos"), NO_LEIDOS("No leídos"), LEIDOS("Leídos") }
data class AvisosUiState(
    val avisos: List<AvisoConLectura> = emptyList(), val museos: List<Museo> = emptyList(),
    val sesion: SesionActiva? = null, val cargando: Boolean = true, val guardando: Boolean = false,
    val filtro: FiltroAvisos = FiltroAvisos.TODOS, val mostrarFormulario: Boolean = false,
    val formulario: FormularioAviso = FormularioAviso(), val error: String? = null,
    val confirmacion: String? = null, val enlacePendiente: EnlaceAviso? = null
) {
    val esAdministrador: Boolean get() = sesion?.rol?.equals("ADMINISTRADOR", true) == true
    val noLeidos: Int get() = avisos.count { !it.leido }
    val filtrados: List<AvisoConLectura> get() = avisos.filter {
        filtro == FiltroAvisos.TODOS || filtro == FiltroAvisos.LEIDOS && it.leido || filtro == FiltroAvisos.NO_LEIDOS && !it.leido
    }
}
