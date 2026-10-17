package com.example.museodelosmuseos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.museodelosmuseos.data.local.entity.HistorialQr
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialQrDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarLectura(registro: HistorialQr): Long

    @Query("SELECT * FROM historial_qr WHERE usuarioId = :usuarioId ORDER BY fechaTimestamp DESC")
    fun observarHistorial(usuarioId: Long): Flow<List<HistorialQr>>

    @Query("SELECT * FROM historial_qr WHERE usuarioId = :usuarioId AND codigo = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(usuarioId: Long, codigo: String): HistorialQr?

    @Query("DELETE FROM historial_qr WHERE id = :id")
    suspend fun eliminar(id: Long)

    @Query("DELETE FROM historial_qr WHERE usuarioId = :usuarioId")
    suspend fun limpiarHistorial(usuarioId: Long)
}
