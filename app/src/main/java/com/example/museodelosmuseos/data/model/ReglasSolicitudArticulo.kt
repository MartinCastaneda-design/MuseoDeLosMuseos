package com.example.museodelosmuseos.data.model

import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import com.example.museodelosmuseos.data.local.entity.SesionActiva

/** Reglas compartidas por el formulario y la transacción local (sin dependencias de UI). */
object ReglasSolicitudArticulo {
    fun validarAcceso(sesion: SesionActiva?, usuarioEsperado: Long): String? = when {
        sesion == null || sesion.usuarioId != usuarioEsperado -> "La sesión cambió. Vuelve al catálogo."
        !sesion.rol.equals("CLIENTE", ignoreCase = true) -> "Solo los clientes pueden realizar solicitudes."
        else -> null
    }

    fun validarDisponibilidad(articulo: ArticuloCatalogo?): String? = when {
        articulo == null -> "El artículo ya no existe."
        !articulo.activo || !articulo.disponible -> "El artículo no está disponible."
        articulo.stock <= 0 -> "No quedan unidades o cupos disponibles."
        !articulo.precio.isFinite() || articulo.precio <= 0 -> "El precio del artículo no es válido."
        else -> null
    }

    fun validarCantidad(texto: String, stock: Int, enCarrito: Int): String? {
        val cantidad = texto.toIntOrNull()
        return when {
            cantidad == null || cantidad <= 0 -> "Ingresa una cantidad entera mayor a cero."
            cantidad.toLong() + enCarrito.toLong() > stock.toLong() ->
                "Puedes agregar hasta ${(stock.toLong() - enCarrito).coerceAtLeast(0)} unidades; ya tienes $enCarrito en el carrito."
            else -> null
        }
    }

    fun validarConsulta(texto: String): String? = when {
        texto.trim().length < 10 -> "Escribe una consulta de al menos 10 caracteres."
        texto.trim().length > 500 -> "La consulta admite hasta 500 caracteres."
        else -> null
    }
}
