package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Una línea por producto y usuario. Agregar al carrito no reserva ni descuenta stock. */
@Entity(
    tableName = "carrito_items",
    primaryKeys = ["usuarioId", "articuloId"],
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ArticuloCatalogo::class, parentColumns = ["id"], childColumns = ["articuloId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("articuloId")]
)
data class CarritoItem(val usuarioId: Long, val articuloId: Long, val cantidad: Int)
