package com.example.museodelosmuseos.data.model

import com.example.museodelosmuseos.data.local.entity.SesionActiva
import java.net.URI

object ReglasFavoritos {
    fun validarAcceso(sesion: SesionActiva?, usuarioId: Long): String? = when {
        sesion == null || sesion.usuarioId != usuarioId -> "La sesión cambió. Vuelve a abrir tus favoritos."
        !sesion.rol.equals("CLIENTE", true) -> "Los favoritos están disponibles solo para el rol Cliente."
        else -> null
    }

    fun enlaceValido(enlace: String): Boolean = runCatching {
        val uri = URI(enlace.trim())
        (uri.scheme.equals("https", true) || uri.scheme.equals("http", true)) && !uri.host.isNullOrBlank()
    }.getOrDefault(false)
}
