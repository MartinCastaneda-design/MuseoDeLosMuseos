package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Solicitud académica local, pendiente de una futura sincronización. */
@Entity(
    tableName = "solicitudes_informacion",
    primaryKeys = ["usuarioId", "articuloId"],
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ArticuloCatalogo::class, parentColumns = ["id"], childColumns = ["articuloId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("articuloId")]
)
data class SolicitudInformacion(
    val usuarioId: Long,
    val articuloId: Long,
    val mensaje: String,
    val fechaTimestamp: Long,
    val estado: String = "PENDIENTE_LOCAL"
)
