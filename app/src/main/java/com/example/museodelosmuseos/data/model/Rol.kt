package com.example.museodelosmuseos.data.model

/**
 * Roles disponibles en la aplicación según el caso de estudio.
 * - ADMINISTRADOR: Gestión del sistema, contenidos y notificaciones.
 * - INVESTIGADOR: Fichas museológicas, vinculación con QR y cámara.
 * - CLIENTE: Exploración, mapa, favoritos, escaneo y compras.
 */
enum class Rol(val etiqueta: String) {
    ADMINISTRADOR("Administrador"),
    INVESTIGADOR("Investigador"),
    CLIENTE("Cliente");

    companion object {
        fun desdeTexto(texto: String): Rol {
            return entries.firstOrNull { it.name.equals(texto, ignoreCase = true) } ?: CLIENTE
        }
    }
}
