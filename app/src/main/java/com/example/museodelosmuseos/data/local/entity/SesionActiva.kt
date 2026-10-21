package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
