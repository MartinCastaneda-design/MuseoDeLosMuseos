package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "museos",
    indices = [Index(value = ["codigoQr"], unique = true)]
)
data class Museo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val pais: String,
    val ciudad: String,
    val descripcion: String,
    val clasificacion: String,
    val latitud: Double,
    val longitud: Double,
    val urlVisitaVirtual: String,
    val codigoQr: String,
    val imagenUrl: String = "",
    val proyecto: String = "Red de Museos Virtuales"
)
