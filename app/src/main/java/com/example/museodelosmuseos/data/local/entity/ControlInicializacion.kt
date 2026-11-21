package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "control_inicializacion")
data class ControlInicializacion(
    @PrimaryKey
    val clave: String,
    val valor: String = "true",
    val fechaTimestamp: Long = System.currentTimeMillis()
)
