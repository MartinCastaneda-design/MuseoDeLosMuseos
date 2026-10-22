package com.example.museodelosmuseos.data.model

import com.example.museodelosmuseos.data.local.entity.SesionActiva

object ReglasPerfil {
    fun acceso(sesion: SesionActiva?, usuarioId: Long): String? = when {
        sesion == null || sesion.usuarioId != usuarioId -> "La sesión cambió. Vuelve a ingresar a tu perfil."
        sesion.rol.uppercase() !in setOf("CLIENTE", "ADMINISTRADOR", "INVESTIGADOR") -> "Rol no autorizado."
        else -> null
    }
    fun nombre(valor: String): String? = when {
        valor.trim().length !in 3..80 -> "Escribe un nombre de 3 a 80 caracteres."
        valor.any { it.isISOControl() } -> "El nombre no puede incluir saltos de línea ni caracteres de control."
        else -> null
    }
    fun correo(valor: String): String? = if (valor.trim().length <= 120 &&
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$").matches(valor.trim())) null
        else "Escribe un correo válido (por ejemplo, nombre@correo.cl)."
    fun avatar(valor: String, usuarioId: Long): String? = if (
        valor in setOf("avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_admin", "avatar_investigador", "avatar_cliente") ||
        Regex("avatar-local/$usuarioId/[a-f0-9-]+\\.img").matches(valor)
    ) null else "Selecciona un avatar válido para tu usuario."
}
