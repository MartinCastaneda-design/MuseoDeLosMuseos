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
import kotlinx.coroutines.flow.combine
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.data.local.entity.PedidoLocal
import com.example.museodelosmuseos.data.local.entity.PedidoLinea
import androidx.room.withTransaction
import com.example.museodelosmuseos.data.local.entity.CarritoItem
import com.example.museodelosmuseos.data.local.entity.SolicitudInformacion
import com.example.museodelosmuseos.data.local.entity.AvisoLocal
import com.example.museodelosmuseos.data.local.entity.AvisoLectura
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
    private val avisoDao = database.avisoDao()

    fun observarAvisos(actorId: Long) = avisoDao.observar(actorId)

    suspend fun publicarAviso(actorId: Long, formulario: FormularioAviso): Long = database.withTransaction {
        ReglasAvisos.validarFormulario(sesionDao.obtenerSesion(), actorId, formulario)?.let { error(it) }
        if (formulario.destino == DestinoAviso.MUSEO)
            check(museoDao.obtenerPorId(formulario.museoId!!) != null) { "El museo seleccionado ya no existe." }
        avisoDao.insertar(AvisoLocal(titulo = formulario.titulo.trim(), mensaje = formulario.mensaje.trim(),
            fechaTimestamp = System.currentTimeMillis(), rolDestino = formulario.audiencia.name,
            destino = formulario.destino.name, museoId = if (formulario.destino == DestinoAviso.MUSEO) formulario.museoId else null))
    }

    suspend fun cambiarLecturaAviso(actorId: Long, avisoId: Long, leido: Boolean) = database.withTransaction {
        ReglasAvisos.acceso(sesionDao.obtenerSesion(), actorId)?.let { error(it) }
        check(avisoDao.obtenerVisible(actorId, avisoId) != null) { "Este aviso no está disponible para tu perfil." }
        if (leido) avisoDao.guardarLecturas(listOf(AvisoLectura(avisoId, actorId, System.currentTimeMillis())))
        else avisoDao.marcarNoLeido(actorId, avisoId)
    }

    suspend fun marcarAvisosLeidos(actorId: Long) = database.withTransaction {
        ReglasAvisos.acceso(sesionDao.obtenerSesion(), actorId)?.let { error(it) }
        avisoDao.guardarLecturas(avisoDao.obtenerVisibles(actorId).filter { !it.leido }
            .map { AvisoLectura(it.aviso.id, actorId, System.currentTimeMillis()) })
    }

    suspend fun abrirAviso(actorId: Long, avisoId: Long): EnlaceAviso = database.withTransaction {
        val sesion = sesionDao.obtenerSesion()
        ReglasAvisos.acceso(sesion, actorId)?.let { error(it) }
        val aviso = avisoDao.obtenerVisible(actorId, avisoId)?.aviso ?: error("El aviso ya no está disponible.")
        val destino = DestinoAviso.entries.firstOrNull { it.name == aviso.destino } ?: error("El enlace del aviso no es válido.")
        check(ReglasAvisos.puedeAbrir(sesion!!.rol, destino)) { "Tu rol no tiene acceso al destino de este aviso." }
        if (destino == DestinoAviso.MUSEO) check(aviso.museoId != null && museoDao.obtenerPorId(aviso.museoId) != null) { "El museo ya no está disponible." }
        if (destino == DestinoAviso.SOLICITUDES && aviso.solicitudClave != null)
            check(obtenerSolicitudVisible(sesion, aviso.solicitudClave)) { "La solicitud ya no está disponible para tu perfil." }
        avisoDao.guardarLecturas(listOf(AvisoLectura(avisoId, actorId, System.currentTimeMillis())))
        EnlaceAviso(destino, aviso.museoId, aviso.solicitudClave)
    }

    private suspend fun obtenerSolicitudVisible(sesion: SesionActiva, clave: String): Boolean {
        if (clave.startsWith("pedido-")) {
            val pedido = compraDao.obtenerPedido(clave.removePrefix("pedido-")) ?: return false
            return PermisosSolicitudes.puedeConsultar(sesion, pedido.usuarioId)
        }
        val partes = clave.split('-')
        if (partes.size != 3 || partes[0] != "curso") return false
        val usuarioId = partes[1].toLongOrNull() ?: return false
        val articuloId = partes[2].toLongOrNull() ?: return false
        return PermisosSolicitudes.puedeConsultar(sesion, usuarioId) && compraDao.obtenerSolicitud(usuarioId, articuloId) != null
    }

    private suspend fun avisoSolicitud(usuarioId: Long, clave: String, titulo: String, mensaje: String) {
        avisoDao.insertar(AvisoLocal(titulo = titulo, mensaje = mensaje, fechaTimestamp = System.currentTimeMillis(),
            rolDestino = "CLIENTE", usuarioDestinoId = usuarioId, destino = DestinoAviso.SOLICITUDES.name, solicitudClave = clave))
    }

    fun observarHistorialSolicitudes(actorId: Long): Flow<List<SolicitudHistorial>> = combine(
        compraDao.observarHistorialPedidos(actorId), compraDao.observarHistorialConsultas(actorId)
    ) { pedidos, consultas ->
        (pedidos.map { SolicitudHistorial(pedido = it.pedido, lineas = it.lineas.sortedBy { l -> l.articuloId }) } +
            consultas.map { SolicitudHistorial(consulta = it.consulta, tituloCurso = it.articulo?.titulo) })
            .sortedByDescending { it.fecha }
    }

    suspend fun cambiarEstadoSolicitud(actorId: Long, solicitud: SolicitudHistorial, nuevo: EstadoSolicitud) = database.withTransaction {
        PermisosSolicitudes.validarCambio(sesionDao.obtenerSesion(), actorId, solicitud.estado, nuevo)?.let { error(it) }
        val actual = solicitud.pedido?.let { compraDao.obtenerPedido(it.id)?.estado }
            ?: solicitud.consulta?.let { compraDao.obtenerSolicitud(it.usuarioId, it.articuloId)?.estado }
            ?: error("La solicitud ya no existe.")
        check(actual == solicitud.estado) { "El estado cambió. Revisa la solicitud actualizada antes de guardar." }
        val filas = solicitud.pedido?.let { compraDao.actualizarEstadoPedido(it.id, actual, nuevo.valor) }
            ?: solicitud.consulta!!.let { compraDao.actualizarEstadoConsulta(it.usuarioId, it.articuloId, actual, nuevo.valor) }
        check(filas == 1) { "No fue posible actualizar la solicitud." }
        avisoSolicitud(solicitud.usuarioId, solicitud.clave, "Estado de solicitud actualizado",
            "${solicitud.titulo}: ${nuevo.etiqueta}. Cambio de demostración guardado solo en este dispositivo.")
    }

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
        avisoSolicitud(usuarioId, "pedido-$id", "Solicitud local registrada", "Pedido ${id.take(8)} registrado como Pendiente local. Total: $${actual.total} CLP. Sin pago real.")
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
        avisoSolicitud(usuarioId, "curso-$usuarioId-$articuloId", "Consulta de curso registrada", "Tu consulta sobre ${articulo.titulo} se guardó en este dispositivo. Aún no se envía a la organización.")
    }

    // --- SESIÓN Y USUARIOS ---
    fun observarPerfilActivo(usuarioId: Long) = usuarioDao.observarPerfilActivo(usuarioId)
    suspend fun correoPerfilDemo(rol: String): String? = usuarioDao.obtenerPorRol(rol).minByOrNull { it.id }?.correo
    fun observarPreferencias(usuarioId: Long) = usuarioDao.observarPreferencias(usuarioId)

    suspend fun guardarPerfil(usuarioId: Long, nombre: String, correo: String, avatar: String,
        preferencias: com.example.museodelosmuseos.data.local.entity.PreferenciasUsuario) = database.withTransaction {
        val sesion = sesionDao.obtenerSesion()
        ReglasPerfil.acceso(sesion, usuarioId)?.let { error(it) }
        ReglasPerfil.nombre(nombre)?.let { error(it) }
        ReglasPerfil.correo(correo)?.let { error(it) }
        ReglasPerfil.avatar(avatar, usuarioId)?.let { error(it) }
        require(preferencias.usuarioId == usuarioId) { "Las preferencias pertenecen a otro usuario." }
        val existente = usuarioDao.buscarPorCorreo(correo.trim())
        check(existente == null || existente.id == usuarioId) { "El correo ya está registrado por otro usuario." }
        check(usuarioDao.actualizarPerfil(usuarioId, nombre.trim(), correo.trim().lowercase(java.util.Locale.ROOT), avatar) == 1) { "El usuario ya no existe." }
        usuarioDao.guardarPreferencias(preferencias)
        sesionDao.guardarSesion(sesion!!.copy(nombre = nombre.trim(), correo = correo.trim().lowercase(java.util.Locale.ROOT), avatar = avatar))
    }

    suspend fun cerrarSesionDePerfil(usuarioId: Long) = database.withTransaction {
        ReglasPerfil.acceso(sesionDao.obtenerSesion(), usuarioId)?.let { error(it) }
        sesionDao.cerrarSesion()
    }

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

    suspend fun conmutarFavorito(usuarioId: Long, museoId: Long, marcar: Boolean): Unit = database.withTransaction {
        ReglasFavoritos.validarAcceso(sesionDao.obtenerSesion(), usuarioId)?.let { error(it) }
        if (marcar) {
            check(museoDao.obtenerPorId(museoId) != null) { "El museo ya no está disponible." }
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
