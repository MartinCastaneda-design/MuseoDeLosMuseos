package com.example.museodelosmuseos.data.local.dao

import androidx.room.*
import com.example.museodelosmuseos.data.local.entity.AvisoLocal
import com.example.museodelosmuseos.data.local.entity.AvisoLectura
import com.example.museodelosmuseos.data.model.AvisoConLectura
import kotlinx.coroutines.flow.Flow

private const val AVISOS_VISIBLES = "SELECT a.*, l.fechaLectura FROM avisos_locales a INNER JOIN sesion_activa s ON s.usuarioId = :actorId LEFT JOIN avisos_lecturas l ON l.avisoId = a.id AND l.usuarioId = s.usuarioId WHERE UPPER(s.rol) IN ('CLIENTE','INVESTIGADOR','ADMINISTRADOR') AND (a.usuarioDestinoId = s.usuarioId OR (a.usuarioDestinoId IS NULL AND (a.rolDestino = 'TODOS' OR a.rolDestino = UPPER(s.rol))))"

@Dao
interface AvisoDao {
    @Query(AVISOS_VISIBLES + " ORDER BY a.fechaTimestamp DESC, a.id DESC")
    fun observar(actorId: Long): Flow<List<AvisoConLectura>>
    @Query(AVISOS_VISIBLES + " ORDER BY a.fechaTimestamp DESC, a.id DESC")
    suspend fun obtenerVisibles(actorId: Long): List<AvisoConLectura>
    @Query(AVISOS_VISIBLES + " AND a.id = :avisoId")
    suspend fun obtenerVisible(actorId: Long, avisoId: Long): AvisoConLectura?
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(aviso: AvisoLocal): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun guardarLecturas(lecturas: List<AvisoLectura>)
    @Query("DELETE FROM avisos_lecturas WHERE usuarioId = :actorId AND avisoId = :avisoId")
    suspend fun marcarNoLeido(actorId: Long, avisoId: Long)
}
