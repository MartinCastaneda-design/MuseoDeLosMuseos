package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pedidos_locales", foreignKeys = [ForeignKey(entity = Usuario::class,
    parentColumns = ["id"], childColumns = ["usuarioId"], onDelete = ForeignKey.RESTRICT)], indices = [Index("usuarioId")])
data class PedidoLocal(
    @PrimaryKey val id: String,
    val usuarioId: Long,
    val fechaTimestamp: Long,
    val destino: String,
    val modalidad: String,
    val ubicacion: String,
    val direccion: String,
    val subtotal: Long,
    val envio: Long,
    val total: Long,
    val estado: String = "PENDIENTE_LOCAL"
)

/** Copia histórica: no depende de cambios ni de la eliminación futura del artículo. */
@Entity(tableName = "pedido_lineas", primaryKeys = ["pedidoId", "articuloId"],
    foreignKeys = [ForeignKey(entity = PedidoLocal::class, parentColumns = ["id"], childColumns = ["pedidoId"], onDelete = ForeignKey.CASCADE)])
data class PedidoLinea(
    val pedidoId: String, val articuloId: Long, val titulo: String, val imagenUrl: String,
    val precioUnitario: Double, val cantidad: Int, val subtotal: Long
)
