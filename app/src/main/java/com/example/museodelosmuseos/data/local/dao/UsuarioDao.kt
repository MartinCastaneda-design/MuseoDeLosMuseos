package com.example.museodelosmuseos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.museodelosmuseos.data.local.entity.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrar(usuario: Usuario): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExisten(usuarios: List<Usuario>)

    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) AND contrasena = :contrasena LIMIT 1")
    suspend fun autenticar(correo: String, contrasena: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): Usuario?

    @Query("SELECT * FROM usuarios WHERE rol = :rol ORDER BY nombre ASC")
    suspend fun obtenerPorRol(rol: String): List<Usuario>

    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun observarTodos(): Flow<List<Usuario>>
}
