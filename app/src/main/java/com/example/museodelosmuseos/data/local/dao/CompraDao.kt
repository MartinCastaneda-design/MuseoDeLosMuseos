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
