package com.example.museodelosmuseos.data.repository

import com.example.museodelosmuseos.data.local.AppDatabase
import com.example.museodelosmuseos.data.local.entity.FavoritoMuseo
import com.example.museodelosmuseos.data.local.entity.HistorialQr
import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.local.entity.SesionActiva
import com.example.museodelosmuseos.data.local.entity.Usuario
import com.example.museodelosmuseos.data.remote.MuseoApiService
import kotlinx.coroutines.flow.Flow

class MuseoRepository(
    private val database: AppDatabase,
    private val apiService: MuseoApiService = MuseoApiService()
) {

    private val usuarioDao = database.usuarioDao()
    private val museoDao = database.museoDao()
    private val favoritoDao = database.favoritoDao()
    private val historialQrDao = database.historialQrDao()
    private val sesionDao = database.sesionDao()

    fun observarSesion(): Flow<SesionActiva?> = sesionDao.observarSesion()

    suspend fun obtenerSesion(): SesionActiva? = sesionDao.obtenerSesion()

    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario> {
        val usuario = usuarioDao.autenticar(correo.trim(), contrasena)
            ?: return Result.failure(Exception("Correo o contraseña incorrectos"))

        sesionDao.guardarSesion(
            SesionActiva(
                usuarioId = usuario.id,
                nombre = usuario.nombre,
                correo = usuario.correo,
                rol = usuario.rol,
                avatar = usuario.avatar
            )
        )
        return Result.success(usuario)
    }

    suspend fun cambiarRolDemo(rolNombre: String): Usuario? {
        val usuario = usuarioDao.obtenerPorRol(rolNombre).firstOrNull() ?: return null
        sesionDao.guardarSesion(
            SesionActiva(
                usuarioId = usuario.id,
                nombre = usuario.nombre,
                correo = usuario.correo,
                rol = usuario.rol,
                avatar = usuario.avatar
            )
        )
        return usuario
    }

    suspend fun registrarUsuario(nombre: String, correo: String, pass: String, avatar: String): Result<Long> {
        val existente = usuarioDao.buscarPorCorreo(correo.trim())
        if (existente != null) {
            return Result.failure(Exception("El correo ya se encuentra registrado"))
        }
        val nuevo = Usuario(
            nombre = nombre.trim(),
            correo = correo.trim(),
            contrasena = pass,
            rol = "CLIENTE",
            avatar = avatar
        )
        val id = usuarioDao.registrar(nuevo)

        sesionDao.guardarSesion(
            SesionActiva(
                usuarioId = id,
                nombre = nuevo.nombre,
                correo = nuevo.correo,
                rol = nuevo.rol,
                avatar = nuevo.avatar
            )
        )
        return Result.success(id)
    }

    suspend fun cerrarSesion() {
        sesionDao.cerrarSesion()
    }

    fun observarMuseos(): Flow<List<Museo>> = museoDao.observarTodos()

    fun observarMuseo(id: Long): Flow<Museo?> = museoDao.observarPorId(id)

    suspend fun obtenerMuseosDirecto(): List<Museo> = museoDao.obtenerTodosDirecto()

    fun observarPaises(): Flow<List<String>> = museoDao.observarPaises()

    fun observarMuseosPorPais(pais: String): Flow<List<Museo>> = museoDao.observarPorPais(pais)

    suspend fun buscarMuseoPorQr(codigoQr: String): Museo? = museoDao.buscarPorCodigoQr(codigoQr)

    suspend fun guardarMuseo(museo: Museo): Long = museoDao.insertar(museo)

    suspend fun sincronizarMuseosRemotos(): Result<Int> {
        return try {
            val museosRemotos = apiService.obtenerMuseosIberoamerica()
            museoDao.insertarSiNoExisten(museosRemotos)
            Result.success(museosRemotos.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun esFavorito(usuarioId: Long, museoId: Long): Flow<Boolean> =
        favoritoDao.esFavorito(usuarioId, museoId)

    suspend fun conmutarFavorito(usuarioId: Long, museoId: Long, marcar: Boolean) {
        if (marcar) {
            favoritoDao.agregarFavorito(FavoritoMuseo(usuarioId = usuarioId, museoId = museoId))
        } else {
            favoritoDao.quitarFavorito(usuarioId, museoId)
        }
    }

    fun observarFavoritos(usuarioId: Long): Flow<List<Museo>> =
        favoritoDao.observarMuseosFavoritos(usuarioId)

    fun observarHistorialQr(usuarioId: Long): Flow<List<HistorialQr>> =
        historialQrDao.observarHistorial(usuarioId)

    suspend fun registrarLecturaQr(usuarioId: Long, codigo: String, titulo: String, descripcion: String, url: String) {
        val registro = HistorialQr(
            usuarioId = usuarioId,
            codigo = codigo,
            titulo = titulo,
            descripcion = descripcion,
            urlDestino = url,
            fechaTimestamp = System.currentTimeMillis()
        )
        historialQrDao.guardarLectura(registro)
    }
}
