package com.example.museodelosmuseos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.museodelosmuseos.data.local.entity.FavoritoMuseo
import com.example.museodelosmuseos.data.local.entity.Museo
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregarFavorito(favorito: FavoritoMuseo): Long

    @Query("DELETE FROM favoritos_museo WHERE usuarioId = :usuarioId AND museoId = :museoId")
    suspend fun quitarFavorito(usuarioId: Long, museoId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favoritos_museo WHERE usuarioId = :usuarioId AND museoId = :museoId)")
    fun esFavorito(usuarioId: Long, museoId: Long): Flow<Boolean>

    @Query("""
        SELECT m.* FROM museos m
        INNER JOIN favoritos_museo f ON m.id = f.museoId
        WHERE f.usuarioId = :usuarioId
        ORDER BY m.nombre ASC
    """)
    fun observarMuseosFavoritos(usuarioId: Long): Flow<List<Museo>>
}
