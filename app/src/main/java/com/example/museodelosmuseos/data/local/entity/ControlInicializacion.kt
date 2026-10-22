package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room de control interno para registrar marcas y estados de inicialización
 * persistentes en la base de datos (por ejemplo, para garantizar que la precarga de
 * datos iniciales ocurra una única vez por instalación, sin sobrescribir cambios del usuario).
 *
 * @param clave Identificador único del evento o configuración (ej. "CATALOGO_PRECARGADO_V1").
 * @param valor Estado o valor asociado a la clave.
 * @param fechaTimestamp Marca de tiempo en milisegundos cuando se registró el evento.
 */
@Entity(tableName = "control_inicializacion")
data class ControlInicializacion(
    @PrimaryKey
    val clave: String,
    val valor: String = "true",
    val fechaTimestamp: Long = System.currentTimeMillis()
)
