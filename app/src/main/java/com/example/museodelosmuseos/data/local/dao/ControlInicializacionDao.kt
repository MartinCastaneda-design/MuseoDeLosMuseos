package com.example.museodelosmuseos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.museodelosmuseos.data.local.entity.ControlInicializacion

@Dao
interface ControlInicializacionDao {

    @Query("SELECT valor FROM control_inicializacion WHERE clave = :clave LIMIT 1")
    suspend fun obtenerValor(clave: String): String?

    @Query("SELECT COUNT(*) FROM control_inicializacion WHERE clave = :clave")
    suspend fun existeClave(clave: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrar(control: ControlInicializacion)
}
