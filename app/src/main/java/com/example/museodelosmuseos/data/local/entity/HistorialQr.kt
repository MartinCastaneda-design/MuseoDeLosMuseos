package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "historial_qr",
    indices = [
        Index(value = ["usuarioId", "codigo"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HistorialQr(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val usuarioId: Long,
    val codigo: String,
    val titulo: String,
    val descripcion: String,
    val urlDestino: String,
    val tipo: String = "MUSEO",
    val fechaTimestamp: Long = System.currentTimeMillis()
)
