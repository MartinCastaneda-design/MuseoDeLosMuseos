package com.example.museodelosmuseos.ui.screens.detallearticulo

import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import com.example.museodelosmuseos.data.model.CategoriaArticulo
import com.example.museodelosmuseos.data.model.ReglasSolicitudArticulo

data class DetalleArticuloUiState(
    val articulo: ArticuloCatalogo? = null,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val cantidad: String = "1",
    val consulta: String = "",
    val enCarrito: Int = 0,
    val solicitudRegistrada: Boolean = false,
    val cantidadEditada: Boolean = false,
    val consultaEditada: Boolean = false,
    val intentoEnviar: Boolean = false,
    val error: String? = null,
    val confirmacion: String? = null
) {
    val esCurso: Boolean get() = articulo?.categoria.equals(CategoriaArticulo.CURSO.name, ignoreCase = true)
    val errorDisponibilidad: String? get() = ReglasSolicitudArticulo.validarDisponibilidad(articulo)
    val errorCantidad: String? get() = if (cantidadEditada || intentoEnviar)
        ReglasSolicitudArticulo.validarCantidad(cantidad, articulo?.stock ?: 0, enCarrito) else null
    val errorConsulta: String? get() = if (consultaEditada || intentoEnviar)
        ReglasSolicitudArticulo.validarConsulta(consulta) else null
    val subtotal: Double get() = (cantidad.toIntOrNull()?.takeIf { it > 0 } ?: 0) * (articulo?.precio ?: 0.0)
}
