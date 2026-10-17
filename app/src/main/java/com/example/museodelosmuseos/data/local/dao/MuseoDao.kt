package com.example.museodelosmuseos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.museodelosmuseos.data.local.entity.Museo
import kotlinx.coroutines.flow.Flow

@Dao
interface MuseoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExisten(museos: List<Museo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(museo: Museo): Long

    @Update
    suspend fun actualizar(museo: Museo)

    @Query("SELECT * FROM museos ORDER BY nombre ASC")
    fun observarTodos(): Flow<List<Museo>>

    @Query("SELECT * FROM museos ORDER BY nombre ASC")
    suspend fun obtenerTodosDirecto(): List<Museo>

    @Query("SELECT * FROM museos WHERE id = :id LIMIT 1")
    fun observarPorId(id: Long): Flow<Museo?>

    @Query("SELECT * FROM museos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): Museo?

    @Query("SELECT * FROM museos WHERE UPPER(codigoQr) = UPPER(:codigoQr) LIMIT 1")
    suspend fun buscarPorCodigoQr(codigoQr: String): Museo?

    @Query("SELECT * FROM museos WHERE pais = :pais ORDER BY nombre ASC")
    fun observarPorPais(pais: String): Flow<List<Museo>>

    @Query("SELECT DISTINCT pais FROM museos ORDER BY pais ASC")
    fun observarPaises(): Flow<List<String>>
}
