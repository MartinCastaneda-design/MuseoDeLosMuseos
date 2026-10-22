package com.example.museodelosmuseos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticuloCatalogoDao {

    @Query("SELECT * FROM articulos_catalogo ORDER BY id ASC")
    fun observarTodos(): Flow<List<ArticuloCatalogo>>

    @Query("SELECT * FROM articulos_catalogo WHERE activo = 1 ORDER BY id ASC")
    fun observarActivos(): Flow<List<ArticuloCatalogo>>

    @Query("SELECT * FROM articulos_catalogo WHERE categoria = :categoria AND activo = 1 ORDER BY id ASC")
    fun observarPorCategoria(categoria: String): Flow<List<ArticuloCatalogo>>

    @Query("SELECT * FROM articulos_catalogo WHERE id = :id LIMIT 1")
    fun observarPorId(id: Long): Flow<ArticuloCatalogo?>

    @Query("SELECT * FROM articulos_catalogo WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): ArticuloCatalogo?

    @Query("SELECT COUNT(*) FROM articulos_catalogo")
    suspend fun contarArticulos(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(articulo: ArticuloCatalogo): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExisten(articulos: List<ArticuloCatalogo>): List<Long>

    @Update
    suspend fun actualizar(articulo: ArticuloCatalogo)

    @Query("UPDATE articulos_catalogo SET disponible = :disponible WHERE id = :id")
    suspend fun actualizarDisponibilidad(id: Long, disponible: Boolean)

    @Query("UPDATE articulos_catalogo SET activo = :activo WHERE id = :id")
    suspend fun actualizarEstadoActivo(id: Long, activo: Boolean)

    @Delete
    suspend fun eliminar(articulo: ArticuloCatalogo)
}
