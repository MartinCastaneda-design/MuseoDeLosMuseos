package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para representar los Museos Virtuales de Iberoamérica.
 * Incluye coordenadas para OSMDroid y URL para la visita virtual.
 */
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
    val clasificacion: String, // Ej: "Historia", "Arte Colonial", "Bellas Artes"
    val latitud: Double,       // Coordenada para OSMDroid
    val longitud: Double,      // Coordenada para OSMDroid
    val urlVisitaVirtual: String,
    val codigoQr: String,      // Código que reconoce el escáner QR
    val proyecto: String = "Red de Museos Virtuales"
)
