package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Mensaje de demostración del dispositivo. No representa una notificación push. */
@Entity(tableName = "avisos_locales", foreignKeys = [ForeignKey(entity = Usuario::class,
    parentColumns = ["id"], childColumns = ["usuarioDestinoId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("usuarioDestinoId"), Index("rolDestino")])
data class AvisoLocal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val mensaje: String,
    val fechaTimestamp: Long,
    val rolDestino: String = "TODOS",
    val usuarioDestinoId: Long? = null,
    val destino: String = "SIN_ENLACE",
    val museoId: Long? = null,
    val solicitudClave: String? = null
)

@Entity(tableName = "avisos_lecturas", primaryKeys = ["avisoId", "usuarioId"], foreignKeys = [
    ForeignKey(entity = AvisoLocal::class, parentColumns = ["id"], childColumns = ["avisoId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"], onDelete = ForeignKey.CASCADE)
], indices = [Index("usuarioId")])
data class AvisoLectura(val avisoId: Long, val usuarioId: Long, val fechaLectura: Long)
