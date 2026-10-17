package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla de una sola fila que conserva la sesión del usuario activo en el teléfono.
 * Permite cambiar de perfil sin mezclar favoritos ni historial.
 */
@Entity(tableName = "sesion_activa")
data class SesionActiva(
    @PrimaryKey
    val id: Int = 1,
    val usuarioId: Long,
    val nombre: String,
    val correo: String,
    val rol: String,
    val avatar: String
)
