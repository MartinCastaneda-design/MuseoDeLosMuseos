package com.example.museodelosmuseos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.museodelosmuseos.data.local.entity.SesionActiva
import kotlinx.coroutines.flow.Flow

@Dao
interface SesionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarSesion(sesion: SesionActiva)

    @Query("SELECT * FROM sesion_activa WHERE id = 1 LIMIT 1")
    fun observarSesion(): Flow<SesionActiva?>

    @Query("SELECT * FROM sesion_activa WHERE id = 1 LIMIT 1")
    suspend fun obtenerSesion(): SesionActiva?

    @Query("DELETE FROM sesion_activa")
    suspend fun cerrarSesion()
}
