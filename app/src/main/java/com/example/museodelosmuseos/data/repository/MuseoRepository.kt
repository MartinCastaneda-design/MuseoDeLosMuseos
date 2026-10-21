package com.example.museodelosmuseos.data.repository

import com.example.museodelosmuseos.data.local.AppDatabase
import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import com.example.museodelosmuseos.data.local.entity.FavoritoMuseo
import com.example.museodelosmuseos.data.local.entity.HistorialQr
import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.local.entity.SesionActiva
import com.example.museodelosmuseos.data.local.entity.Usuario
import com.example.museodelosmuseos.data.remote.MuseoApiService
import kotlinx.coroutines.flow.Flow
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.data.local.entity.PedidoLocal
import com.example.museodelosmuseos.data.local.entity.PedidoLinea
import androidx.room.withTransaction
import com.example.museodelosmuseos.data.local.entity.CarritoItem
import com.example.museodelosmuseos.data.local.entity.SolicitudInformacion
import com.example.museodelosmuseos.data.model.CategoriaArticulo
import com.example.museodelosmuseos.data.model.ReglasSolicitudArticulo

/**
 * Repositorio central que desacopla los ViewModels de la base de datos Room.
 * Proporciona operaciones seguras para autenticación, museos, favoritos e historial QR.
 * Implementa caché local en SQLite respaldada por una API REST ficticia.
 */
class MuseoRepository(
    private val database: AppDatabase,
    private val apiService: MuseoApiService = MuseoApiService()
) {

    private val usuarioDao = database.usuarioDao()
    private val museoDao = database.museoDao()
    private val favoritoDao = database.favoritoDao()
    private val historialQrDao = database.historialQrDao()
    private val sesionDao = database.sesionDao()
    private val articuloCatalogoDao = database.articuloCatalogoDao()
    private val compraDao = database.compraDao()

    fun observarLineasCarrito(usuarioId: Long) = compraDao.observarLineas(usuarioId)
    fun observarPedidos(usuarioId: Long) = compraDao.observarPedidos(usuarioId)
    suspend fun obtenerLineasPedido(usuarioId: Long, pedidoId: String) = compraDao.obtenerLineasPedido(usuarioId, pedidoId)

    suspend fun cambiarCantidadCarrito(usuarioId: Long, articuloId: Long, cantidad: Int) = database.withTransaction {
        ReglasSolicitudArticulo.validarAcceso(sesionDao.obtenerSesion(), usuarioId)?.let { error(it) }
        val anterior = compraDao.obtenerItem(usuarioId, articuloId) ?: error("El producto ya no está en el carrito.")
        val articulo = articuloCatalogoDao.obtenerPorId(articuloId) ?: error("El producto ya no existe.")
        // Una reducción permite corregir un carrito cuyo stock disminuyó; confirmar siempre revalida todo.
        require(cantidad > 0 && (cantidad < anterior.cantidad || cantidad <= articulo.stock)) { "La cantidad supera el stock disponible." }
        if (cantidad > anterior.cantidad) ReglasSolicitudArticulo.validarDisponibilidad(articulo)?.let { error(it) }
        compraDao.guardarItem(anterior.copy(cantidad = cantidad))
    }

    suspend fun eliminarDelCarrito(usuarioId: Long, articuloId: Long) = database.withTransaction {
        ReglasSolicitudArticulo.validarAcceso(sesionDao.obtenerSesion(), usuarioId)?.let { error(it) }
        compraDao.eliminarItem(usuarioId, articuloId)
    }

    suspend fun confirmarCarrito(usuarioId: Long, destino: DestinoEnvio, modalidad: ModalidadEnvio,
        ubicacion: String, direccion: String, revisada: CotizacionCarrito): String = database.withTransaction {
        ReglasSolicitudArticulo.validarAcceso(sesionDao.obtenerSesion(), usuarioId)?.let { error(it) }
        CalculoCarrito.validarUbicacion(ubicacion)?.let { error(it) }
        CalculoCarrito.validarDireccion(direccion)?.let { error(it) }
        val actual = CalculoCarrito.cotizar(compraDao.obtenerLineas(usuarioId), destino, modalidad)
        check(actual == revisada) { "El carrito, stock o precios cambiaron. Revisa el resumen y confirma nuevamente." }
        val id = java.util.UUID.randomUUID().toString()
        compraDao.insertarPedido(PedidoLocal(id, usuarioId, System.currentTimeMillis(), destino.name,
            modalidad.name, ubicacion.trim(), direccion.trim(), actual.subtotal, actual.envio, actual.total))
        compraDao.insertarLineas(actual.lineas.map { PedidoLinea(id, it.articulo.id, it.articulo.titulo,
            it.articulo.imagenUrl, it.articulo.precio, it.cantidad, it.subtotal) })
        compraDao.vaciarCarrito(usuarioId)
        id
    }

    fun observarCarrito(usuarioId: Long): Flow<List<CarritoItem>> = compraDao.observarCarrito(usuarioId)

    fun observarSolicitudesInformacion(usuarioId: Long): Flow<List<SolicitudInformacion>> =
        compraDao.observarSolicitudes(usuarioId)

    /** Revalida sesión, disponibilidad y cantidad acumulada de forma atómica. */
    suspend fun agregarAlCarrito(usuarioId: Long, articuloId: Long, cantidad: Int) = database.withTransaction {
        ReglasSolicitudArticulo.validarAcceso(sesionDao.obtenerSesion(), usuarioId)?.let { error(it) }
        val articulo = articuloCatalogoDao.obtenerPorId(articuloId)
        ReglasSolicitudArticulo.validarDisponibilidad(articulo)?.let { error(it) }
        require(!articulo!!.categoria.equals(CategoriaArticulo.CURSO.name, ignoreCase = true)) {
            "Para los cursos utiliza Solicitar información."
        }
        val anterior = compraDao.obtenerItem(usuarioId, articuloId)?.cantidad ?: 0
        ReglasSolicitudArticulo.validarCantidad(cantidad.toString(), articulo.stock, anterior)?.let { error(it) }
        compraDao.guardarItem(CarritoItem(usuarioId, articuloId, anterior + cantidad))
    }

    suspend fun solicitarInformacion(usuarioId: Long, articuloId: Long, mensaje: String) = database.withTransaction {
        ReglasSolicitudArticulo.validarAcceso(sesionDao.obtenerSesion(), usuarioId)?.let { error(it) }
        val articulo = articuloCatalogoDao.obtenerPorId(articuloId)
        ReglasSolicitudArticulo.validarDisponibilidad(articulo)?.let { error(it) }
        require(articulo!!.categoria.equals(CategoriaArticulo.CURSO.name, ignoreCase = true)) {
            "La solicitud de información corresponde a cursos."
        }
        ReglasSolicitudArticulo.validarConsulta(mensaje)?.let { error(it) }
        check(compraDao.obtenerSolicitud(usuarioId, articuloId) == null) { "Ya tienes una solicitud para este curso." }
        compraDao.guardarSolicitud(SolicitudInformacion(usuarioId, articuloId, mensaje.trim(), System.currentTimeMillis()))
    }

    // --- SESIÓN Y USUARIOS ---
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
        // Autologuear al usuario registrado
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

    // --- MUSEOS Y MAPAS ---
    fun observarMuseos(): Flow<List<Museo>> = museoDao.observarTodos()

    fun observarMuseo(id: Long): Flow<Museo?> = museoDao.observarPorId(id)

    suspend fun obtenerMuseosDirecto(): List<Museo> = museoDao.obtenerTodosDirecto()

    fun observarPaises(): Flow<List<String>> = museoDao.observarPaises()

    fun observarMuseosPorPais(pais: String): Flow<List<Museo>> = museoDao.observarPorPais(pais)

    suspend fun buscarMuseoPorQr(codigoQr: String): Museo? = museoDao.buscarPorCodigoQr(codigoQr)

    suspend fun guardarMuseo(museo: Museo): Long = museoDao.insertar(museo)

    /**
     * Consulta la API REST ficticia y actualiza la caché local en SQLite (Room).
     * Satisface el requerimiento de la pauta:
     * "Consulta datos de una API REST ficticia... puede usar SQLite como caché"
     */
    suspend fun sincronizarMuseosRemotos(): Result<Int> {
        return try {
            val museosRemotos = apiService.obtenerMuseosIberoamerica()
            museoDao.insertarSiNoExisten(museosRemotos)
            Result.success(museosRemotos.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- FAVORITOS (POR USUARIO) ---
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

    // --- HISTORIAL QR (POR USUARIO) ---
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

    // --- CATÁLOGO DE PRODUCTOS Y CURSOS (PANTALLA 9) ---
    fun observarCatalogo(soloActivos: Boolean = true): Flow<List<ArticuloCatalogo>> {
        return if (soloActivos) {
            articuloCatalogoDao.observarActivos()
        } else {
            articuloCatalogoDao.observarTodos()
        }
    }

    fun observarCatalogoPorCategoria(categoria: String): Flow<List<ArticuloCatalogo>> =
        articuloCatalogoDao.observarPorCategoria(categoria)

    fun observarArticuloCatalogo(id: Long): Flow<ArticuloCatalogo?> =
        articuloCatalogoDao.observarPorId(id)

    suspend fun obtenerArticuloCatalogo(id: Long): ArticuloCatalogo? =
        articuloCatalogoDao.obtenerPorId(id)

    suspend fun guardarArticuloCatalogo(articulo: ArticuloCatalogo): Long =
        articuloCatalogoDao.insertar(articulo)

    suspend fun actualizarArticuloCatalogo(articulo: ArticuloCatalogo) {
        articuloCatalogoDao.actualizar(articulo)
    }

    suspend fun cambiarDisponibilidadArticulo(id: Long, disponible: Boolean) {
        articuloCatalogoDao.actualizarDisponibilidad(id, disponible)
    }

    /**
     * Desactiva un artículo del catálogo (baja lógica).
     * Cumple la regla de integridad de negocio: no borra físicamente el registro
     * para preservar compras, pedidos e inscripciones en las pantallas 10 y 12.
     */
    suspend fun desactivarArticuloCatalogo(id: Long) {
        articuloCatalogoDao.actualizarEstadoActivo(id, activo = false)
    }

    /**
     * Reactiva un artículo previamente desactivado.
     */
    suspend fun reactivarArticuloCatalogo(id: Long) {
        articuloCatalogoDao.actualizarEstadoActivo(id, activo = true)
    }

    suspend fun cambiarEstadoActivoArticulo(id: Long, activo: Boolean) {
        articuloCatalogoDao.actualizarEstadoActivo(id, activo)
    }

    /**
     * Eliminación segura: redirige la acción de borrado hacia una desactivación lógica
     * para proteger la integridad referencial de transacciones históricas.
     */
    suspend fun eliminarArticuloCatalogo(articulo: ArticuloCatalogo) {
        desactivarArticuloCatalogo(articulo.id)
    }
}
