package com.example.museodelosmuseos.ui.screens.carrito

import com.example.museodelosmuseos.data.model.*

data class RevisionPedido(val cotizacion: CotizacionCarrito, val destino: DestinoEnvio,
    val modalidad: ModalidadEnvio, val ubicacion: String, val direccion: String)

data class CarritoUiState(
    val lineas: List<LineaCarrito> = emptyList(), val cargando: Boolean = true,
    val guardando: Boolean = false, val destino: DestinoEnvio? = null,
    val modalidad: ModalidadEnvio? = null, val ubicacion: String = "", val direccion: String = "",
    val intentoEnviar: Boolean = false, val error: String? = null,
    val confirmacion: String? = null, val revision: RevisionPedido? = null
) {
    val subtotal: Long get() = runCatching { lineas.fold(0L) { total, linea -> Math.addExact(total, linea.subtotal) } }.getOrDefault(0)
    val cotizacion: CotizacionCarrito? get() = if (destino == null || modalidad == null) null
        else runCatching { CalculoCarrito.cotizar(lineas, destino!!, modalidad!!) }.getOrNull()
    val errorUbicacion: String? get() = if (intentoEnviar) CalculoCarrito.validarUbicacion(ubicacion) else null
    val errorDireccion: String? get() = if (intentoEnviar) CalculoCarrito.validarDireccion(direccion) else null
}
