package com.example.museodelosmuseos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.museodelosmuseos.data.model.PedidoConLineas
import com.example.museodelosmuseos.data.model.ConsultaConArticulo
import com.example.museodelosmuseos.data.local.entity.PedidoLocal
import com.example.museodelosmuseos.data.local.entity.CarritoItem
import com.example.museodelosmuseos.data.local.entity.SolicitudInformacion
import kotlinx.coroutines.flow.Flow

@Dao
interface CompraDao {
    // La sesión forma parte de la consulta: también invalida el Flow al cambiar de perfil.
    @Transaction
    @Query("SELECT p.* FROM pedidos_locales p INNER JOIN sesion_activa s ON s.usuarioId = :actorId WHERE UPPER(s.rol) = 'ADMINISTRADOR' OR (UPPER(s.rol) = 'CLIENTE' AND p.usuarioId = s.usuarioId) ORDER BY p.fechaTimestamp DESC, p.id")
    fun observarHistorialPedidos(actorId: Long): Flow<List<PedidoConLineas>>

    @Transaction
    @Query("SELECT p.* FROM solicitudes_informacion p INNER JOIN sesion_activa s ON s.usuarioId = :actorId WHERE UPPER(s.rol) = 'ADMINISTRADOR' OR (UPPER(s.rol) = 'CLIENTE' AND p.usuarioId = s.usuarioId) ORDER BY p.fechaTimestamp DESC")
    fun observarHistorialConsultas(actorId: Long): Flow<List<ConsultaConArticulo>>

    @Query("SELECT * FROM pedidos_locales WHERE id = :id")
    suspend fun obtenerPedido(id: String): PedidoLocal?

    @Query("UPDATE pedidos_locales SET estado = :nuevo WHERE id = :id AND estado = :anterior")
    suspend fun actualizarEstadoPedido(id: String, anterior: String, nuevo: String): Int

    @Query("UPDATE solicitudes_informacion SET estado = :nuevo WHERE usuarioId = :usuarioId AND articuloId = :articuloId AND estado = :anterior")
    suspend fun actualizarEstadoConsulta(usuarioId: Long, articuloId: Long, anterior: String, nuevo: String): Int
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
