package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para representar a los usuarios registrados en el teléfono.
 * Permite mantener perfiles de prueba (Admin, Investigador) y clientes con sus datos.
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: String, // "ADMINISTRADOR", "INVESTIGADOR", "CLIENTE"
    val avatar: String = "avatar_1"
)
