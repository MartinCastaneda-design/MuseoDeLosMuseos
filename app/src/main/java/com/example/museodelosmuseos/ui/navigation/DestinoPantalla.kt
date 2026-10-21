package com.example.museodelosmuseos.ui.navigation

/**
 * Destinos de navegación reactiva para las pantallas del proyecto.
 * Permite cambiar de pantalla mediante estado en Compose de forma segura y tipada.
 */
sealed interface DestinoPantalla {
    data object Bienvenida : DestinoPantalla
    data object Registro : DestinoPantalla
    data object Login : DestinoPantalla
    data object Dashboard : DestinoPantalla
    data object Mapa : DestinoPantalla
    data class DetalleMuseo(val museoId: Long, val origenFavoritos: Favoritos? = null) : DestinoPantalla
    data class Favoritos(val origenMuseoId: Long? = null) : DestinoPantalla
    data object EscanerQr : DestinoPantalla
    data class ResultadoQr(
        val codigo: String,
        val titulo: String = "",
        val descripcion: String = "",
        val esValido: Boolean = true,
        val museoId: Long? = null
    ) : DestinoPantalla
    data object Catalogo : DestinoPantalla
    data class DetalleArticulo(val articuloId: Long) : DestinoPantalla
    data class Carrito(val origenArticuloId: Long? = null) : DestinoPantalla
    data class Solicitudes(val origenCarrito: Carrito? = null, val solicitudInicial: String? = null) : DestinoPantalla
    data object Avisos : DestinoPantalla
    data object Perfil : DestinoPantalla
}
