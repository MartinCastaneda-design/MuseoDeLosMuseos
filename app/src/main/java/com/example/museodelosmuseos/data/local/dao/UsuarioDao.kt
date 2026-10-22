package com.example.museodelosmuseos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.museodelosmuseos.data.local.entity.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT u.* FROM usuarios u INNER JOIN sesion_activa s ON s.usuarioId = u.id WHERE s.id = 1 AND u.id = :usuarioId")
    fun observarPerfilActivo(usuarioId: Long): Flow<Usuario?>

    @Query("UPDATE usuarios SET nombre = :nombre, correo = :correo, avatar = :avatar WHERE id = :usuarioId")
    suspend fun actualizarPerfil(usuarioId: Long, nombre: String, correo: String, avatar: String): Int

    @Query("SELECT p.* FROM preferencias_usuario p INNER JOIN sesion_activa s ON s.usuarioId = p.usuarioId WHERE s.id = 1 AND p.usuarioId = :usuarioId")
    fun observarPreferencias(usuarioId: Long): Flow<com.example.museodelosmuseos.data.local.entity.PreferenciasUsuario?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPreferencias(preferencias: com.example.museodelosmuseos.data.local.entity.PreferenciasUsuario)

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
