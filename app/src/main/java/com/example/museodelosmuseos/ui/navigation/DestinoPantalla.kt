package com.example.museodelosmuseos.ui.navigation

sealed interface DestinoPantalla {
    data object Bienvenida : DestinoPantalla
    data object Registro : DestinoPantalla
    data object Login : DestinoPantalla
    data object Dashboard : DestinoPantalla
    data object Mapa : DestinoPantalla
    data class DetalleMuseo(val museoId: Long) : DestinoPantalla
    data object EscanerQr : DestinoPantalla
    data class ResultadoQr(
        val codigo: String,
        val titulo: String = "",
        val descripcion: String = "",
        val esValido: Boolean = true,
        val museoId: Long? = null
    ) : DestinoPantalla
}
