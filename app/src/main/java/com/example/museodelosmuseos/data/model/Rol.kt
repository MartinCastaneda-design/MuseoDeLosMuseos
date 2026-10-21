package com.example.museodelosmuseos.data.model

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
