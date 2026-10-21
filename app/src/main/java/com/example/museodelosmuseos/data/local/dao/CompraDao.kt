package com.example.museodelosmuseos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.museodelosmuseos.data.local.entity.CarritoItem
import com.example.museodelosmuseos.data.local.entity.SolicitudInformacion
import kotlinx.coroutines.flow.Flow

@Dao
interface CompraDao {
    @Query("SELECT a.*, c.cantidad FROM carrito_items c INNER JOIN articulos_catalogo a ON a.id = c.articuloId WHERE c.usuarioId = :usuarioId ORDER BY a.id")
    fun observarLineas(usuarioId: Long): Flow<List<com.example.museodelosmuseos.data.model.LineaCarrito>>

    @Query("SELECT a.*, c.cantidad FROM carrito_items c INNER JOIN articulos_catalogo a ON a.id = c.articuloId WHERE c.usuarioId = :usuarioId ORDER BY a.id")
    suspend fun obtenerLineas(usuarioId: Long): List<com.example.museodelosmuseos.data.model.LineaCarrito>

    @Query("DELETE FROM carrito_items WHERE usuarioId = :usuarioId AND articuloId = :articuloId")
    suspend fun eliminarItem(usuarioId: Long, articuloId: Long)

    @Query("DELETE FROM carrito_items WHERE usuarioId = :usuarioId")
    suspend fun vaciarCarrito(usuarioId: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarPedido(pedido: com.example.museodelosmuseos.data.local.entity.PedidoLocal)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarLineas(lineas: List<com.example.museodelosmuseos.data.local.entity.PedidoLinea>)

    @Query("SELECT * FROM pedidos_locales WHERE usuarioId = :usuarioId ORDER BY fechaTimestamp DESC")
    fun observarPedidos(usuarioId: Long): Flow<List<com.example.museodelosmuseos.data.local.entity.PedidoLocal>>

    @Query("SELECT l.* FROM pedido_lineas l INNER JOIN pedidos_locales p ON p.id = l.pedidoId WHERE p.usuarioId = :usuarioId AND p.id = :pedidoId ORDER BY l.articuloId")
    suspend fun obtenerLineasPedido(usuarioId: Long, pedidoId: String): List<com.example.museodelosmuseos.data.local.entity.PedidoLinea>
    @Query("SELECT * FROM carrito_items WHERE usuarioId = :usuarioId ORDER BY articuloId")
    fun observarCarrito(usuarioId: Long): Flow<List<CarritoItem>>

    @Query("SELECT * FROM carrito_items WHERE usuarioId = :usuarioId AND articuloId = :articuloId")
    suspend fun obtenerItem(usuarioId: Long, articuloId: Long): CarritoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarItem(item: CarritoItem)

    @Query("SELECT * FROM solicitudes_informacion WHERE usuarioId = :usuarioId ORDER BY fechaTimestamp DESC")
    fun observarSolicitudes(usuarioId: Long): Flow<List<SolicitudInformacion>>

    @Query("SELECT * FROM solicitudes_informacion WHERE usuarioId = :usuarioId AND articuloId = :articuloId")
    suspend fun obtenerSolicitud(usuarioId: Long, articuloId: Long): SolicitudInformacion?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun guardarSolicitud(solicitud: SolicitudInformacion)
}
