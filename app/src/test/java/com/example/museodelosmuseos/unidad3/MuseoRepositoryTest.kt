package com.example.museodelosmuseos.unidad3

import androidx.room.withTransaction
import com.example.museodelosmuseos.data.local.AppDatabase
import com.example.museodelosmuseos.data.local.dao.*
import com.example.museodelosmuseos.data.local.entity.*
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.data.remote.MuseoApiService
import com.example.museodelosmuseos.data.repository.MuseoRepository
import io.mockk.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test

class MuseoRepositoryTest {
    private val db = mockk<AppDatabase>()
    private val usuarios = mockk<UsuarioDao>(relaxed = true)
    private val museos = mockk<MuseoDao>(relaxed = true)
    private val favoritos = mockk<FavoritoDao>(relaxed = true)
    private val historial = mockk<HistorialQrDao>(relaxed = true)
    private val sesiones = mockk<SesionDao>(relaxed = true)
    private val catalogo = mockk<ArticuloCatalogoDao>(relaxed = true)
    private val compras = mockk<CompraDao>(relaxed = true)
    private val avisos = mockk<AvisoDao>(relaxed = true)
    private val api = mockk<MuseoApiService>()
    private lateinit var repo: MuseoRepository
    private val pedido = PedidoLocal("p1", 3, 10, "NACIONAL", "ESTANDAR", "Santiago", "Calle 123", 2000, 4500, 6500)
    @Before fun preparar() {
        every { db.usuarioDao() } returns usuarios; every { db.museoDao() } returns museos; every { db.favoritoDao() } returns favoritos
        every { db.historialQrDao() } returns historial; every { db.sesionDao() } returns sesiones; every { db.articuloCatalogoDao() } returns catalogo
        every { db.compraDao() } returns compras; every { db.avisoDao() } returns avisos
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { db.withTransaction(any<suspend () -> Any?>()) } coAnswers { secondArg<suspend () -> Any?>().invoke() }
        coEvery { sesiones.obtenerSesion() } returns cliente
        coEvery { catalogo.obtenerPorId(2) } returns articulo
        coEvery { museos.obtenerPorId(1) } returns museo
        repo = MuseoRepository(db, api)
    }
    @After fun terminar() { unmockkAll() }
    @Test fun autenticaYGuardaIdentidadSoloConCredencialesValidas() = runTest {
        coEvery { usuarios.autenticar("maria@test.cl", "clave123") } returns usuario
        assertEquals(usuario, repo.iniciarSesion(" maria@test.cl ", "clave123").getOrThrow())
        coVerify { sesiones.guardarSesion(match { it.usuarioId == 3L && it.correo == usuario.correo && it.rol == "CLIENTE" }) }
        clearMocks(sesiones, answers = false)
        coEvery { usuarios.autenticar(any(), any()) } returns null
        assertTrue(repo.iniciarSesion("x@test.cl", "incorrecta").isFailure); coVerify(exactly = 0) { sesiones.guardarSesion(any()) }
    }
    @Test fun registroEvitaDuplicadosYGuardaClienteSinCambiarRol() = runTest {
        coEvery { usuarios.buscarPorCorreo("ana@test.cl") } returns usuario
        assertTrue(repo.registrarUsuario("Ana", "ana@test.cl", "clave123", "avatar_1").isFailure)
        coVerify(exactly = 0) { usuarios.registrar(any()) }
        coEvery { usuarios.buscarPorCorreo("ana@test.cl") } returns null; coEvery { usuarios.registrar(any()) } returns 8
        assertEquals(8L, repo.registrarUsuario(" Ana ", " ana@test.cl ", "clave123", "avatar_2").getOrThrow())
        coVerify { usuarios.registrar(match { it.nombre == "Ana" && it.correo == "ana@test.cl" && it.rol == "CLIENTE" }) }
        coVerify { sesiones.guardarSesion(match { it.usuarioId == 8L && it.avatar == "avatar_2" }) }
    }
    @Test fun carritoAcumulaCantidadesYRechazaStockExcedido() = runTest {
        coEvery { compras.obtenerItem(3, 2) } returns CarritoItem(3, 2, 2)
        repo.agregarAlCarrito(3, 2, 3); coVerify { compras.guardarItem(CarritoItem(3, 2, 5)) }
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.agregarAlCarrito(3, 2, 4) } }
        coVerify(exactly = 1) { compras.guardarItem(any()) }
        coEvery { sesiones.obtenerSesion() } returns administrador
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.agregarAlCarrito(3, 2, 1) } }
    }

    @Test fun carritoReduceStockDesactualizadoPeroNoPermiteIncrementarlo() = runTest {
        coEvery { compras.obtenerItem(3, 2) } returns CarritoItem(3, 2, 7)
        repo.cambiarCantidadCarrito(3, 2, 6); coVerify { compras.guardarItem(CarritoItem(3, 2, 6)) }
        assertThrows(IllegalArgumentException::class.java) { kotlinx.coroutines.runBlocking { repo.cambiarCantidadCarrito(3, 2, 8) } }
        assertThrows(IllegalArgumentException::class.java) { kotlinx.coroutines.runBlocking { repo.cambiarCantidadCarrito(3, 2, 0) } }
        coEvery { compras.obtenerItem(3, 2) } returns null
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.cambiarCantidadCarrito(3, 2, 1) } }
        repo.eliminarDelCarrito(3, 2); coVerify { compras.eliminarItem(3, 2) }
    }
    @Test fun carritoIncrementoRevalidaDisponibilidadYProductoExistente() = runTest {
        coEvery { compras.obtenerItem(3, 2) } returns CarritoItem(3, 2, 1)
        coEvery { catalogo.obtenerPorId(2) } returns articulo.copy(disponible = false)
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.cambiarCantidadCarrito(3, 2, 2) } }
        coEvery { catalogo.obtenerPorId(2) } returns null
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.cambiarCantidadCarrito(3, 2, 2) } }
        coVerify(exactly = 0) { compras.guardarItem(any()) }
    }
    @Test fun confirmaPedidoConMontosHistoricosYVaciaSoloSuCarrito() = runTest {
        val lineas = listOf(LineaCarrito(articulo, 2)); coEvery { compras.obtenerLineas(3) } returns lineas
        val cotizacion = CalculoCarrito.cotizar(lineas, DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR)
        val id = repo.confirmarCarrito(3, DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR, " Santiago ", " Calle Museo 123 ", cotizacion)
        assertTrue(id.isNotBlank())
        coVerify { compras.insertarPedido(match { it.id == id && it.usuarioId == 3L && it.total == 6500L && it.estado == "PENDIENTE_LOCAL" && it.direccion == "Calle Museo 123" }) }
        coVerify { compras.insertarLineas(match { it.single().precioUnitario == 1000.0 && it.single().cantidad == 2 && it.single().subtotal == 2000L }) }
        coVerify(exactly = 1) { compras.vaciarCarrito(3) }
        coVerify { avisos.insertar(match { it.usuarioDestinoId == 3L && it.solicitudClave == "pedido-$id" }) }
    }
    @Test fun confirmacionRechazaCotizacionObsoletaSinEscribirPedido() = runTest {
        val lineas = listOf(LineaCarrito(articulo, 1)); coEvery { compras.obtenerLineas(3) } returns lineas
        val vieja = CalculoCarrito.cotizar(listOf(LineaCarrito(articulo, 2)), DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR)
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.confirmarCarrito(3, DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR, "Santiago", "Calle 123", vieja) } }
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.confirmarCarrito(3, DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR, "", "Calle 123", vieja) } }
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.confirmarCarrito(3, DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR, "Santiago", "", vieja) } }
        coVerify(exactly = 0) { compras.insertarPedido(any()) }; coVerify(exactly = 0) { compras.vaciarCarrito(any()) }
    }
    @Test fun cursoSeConsultaUnaVezYNoSeAgregaComoProducto() = runTest {
        coEvery { catalogo.obtenerPorId(2) } returns articulo.copy(categoria = "CURSO")
        coEvery { compras.obtenerSolicitud(3, 2) } returns null
        repo.solicitarInformacion(3, 2, " Deseo conocer horarios y requisitos ")
        coVerify { compras.guardarSolicitud(match { it.usuarioId == 3L && it.articuloId == 2L && it.mensaje == "Deseo conocer horarios y requisitos" }) }
        coVerify { avisos.insertar(match { it.solicitudClave == "curso-3-2" && it.usuarioDestinoId == 3L }) }
        assertThrows(IllegalArgumentException::class.java) { kotlinx.coroutines.runBlocking { repo.agregarAlCarrito(3, 2, 1) } }
        coEvery { compras.obtenerSolicitud(3, 2) } returns SolicitudInformacion(3, 2, "Anterior consulta", 1)
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.solicitarInformacion(3, 2, "Consulta duplicada sobre horarios") } }
    }
    @Test fun solicitudDeCursoRechazaArticuloIncorrectoYConsultaCorta() = runTest {
        assertThrows(IllegalArgumentException::class.java) { kotlinx.coroutines.runBlocking { repo.solicitarInformacion(3, 2, "Mensaje suficientemente largo") } }
        coEvery { catalogo.obtenerPorId(2) } returns articulo.copy(categoria = "CURSO")
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.solicitarInformacion(3, 2, "corta") } }
        coVerify(exactly = 0) { compras.guardarSolicitud(any()) }
    }
    @Test fun perfilNormalizaCorreoYActualizaSesionYPreferencias() = runTest {
        coEvery { usuarios.buscarPorCorreo(any()) } returns null; coEvery { usuarios.actualizarPerfil(any(), any(), any(), any()) } returns 1
        val preferencias = PreferenciasUsuario(3, false, true)
        repo.guardarPerfil(3, " Ana Pérez ", " ANA@TEST.CL ", "avatar_2", preferencias)
        coVerify { usuarios.actualizarPerfil(3, "Ana Pérez", "ana@test.cl", "avatar_2") }
        coVerify { usuarios.guardarPreferencias(preferencias) }; coVerify { sesiones.guardarSesion(match { it.usuarioId == 3L && it.nombre == "Ana Pérez" && it.correo == "ana@test.cl" && it.rol == "CLIENTE" }) }
        repo.cerrarSesionDePerfil(3); coVerify { sesiones.cerrarSesion() }
    }
    @Test fun perfilRechazaCorreoAjenoPreferenciasAjenasYUsuarioEliminado() = runTest {
        coEvery { usuarios.buscarPorCorreo(any()) } returns usuario.copy(id = 8)
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.guardarPerfil(3, "Ana", "ana@test.cl", "avatar_1", PreferenciasUsuario(3)) } }
        assertThrows(IllegalArgumentException::class.java) { kotlinx.coroutines.runBlocking { repo.guardarPerfil(3, "Ana", "ana@test.cl", "avatar_1", PreferenciasUsuario(8)) } }
        coEvery { usuarios.buscarPorCorreo(any()) } returns null; coEvery { usuarios.actualizarPerfil(any(), any(), any(), any()) } returns 0
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.guardarPerfil(3, "Ana", "ana@test.cl", "avatar_1", PreferenciasUsuario(3)) } }
        coVerify(exactly = 0) { usuarios.guardarPreferencias(any()) }
    }
    @Test fun favoritosVerificaSesionYMuseoAntesDeEscribir() = runTest {
        repo.conmutarFavorito(3, 1, true); coVerify { favoritos.agregarFavorito(FavoritoMuseo(usuarioId = 3, museoId = 1)) }
        repo.conmutarFavorito(3, 1, false); coVerify { favoritos.quitarFavorito(3, 1) }
        coEvery { museos.obtenerPorId(1) } returns null
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.conmutarFavorito(3, 1, true) } }
        coEvery { sesiones.obtenerSesion() } returns cliente.copy(usuarioId = 8)
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.conmutarFavorito(3, 1, false) } }
    }
    @Test fun actualizaEstadoConControlDeConcurrenciaYAvisoPrivado() = runTest {
        coEvery { sesiones.obtenerSesion() } returns administrador
        coEvery { compras.obtenerPedido("p1") } returns pedido; coEvery { compras.actualizarEstadoPedido(any(), any(), any()) } returns 1
        repo.cambiarEstadoSolicitud(3, SolicitudHistorial(pedido = pedido), EstadoSolicitud.ENVIADO)
        coVerify { compras.actualizarEstadoPedido("p1", "PENDIENTE_LOCAL", "ENVIADO") }; coVerify { avisos.insertar(match { it.usuarioDestinoId == 3L && it.solicitudClave == "pedido-p1" }) }
        coEvery { compras.obtenerPedido("p1") } returns pedido.copy(estado = "ENTREGADO")
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.cambiarEstadoSolicitud(3, SolicitudHistorial(pedido = pedido), EstadoSolicitud.ENVIADO) } }
        coEvery { compras.obtenerPedido("p1") } returns null
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.cambiarEstadoSolicitud(3, SolicitudHistorial(pedido = pedido), EstadoSolicitud.ENVIADO) } }
    }
    @Test fun actualizaConsultaYDetectaFalloDeEscritura() = runTest {
        coEvery { sesiones.obtenerSesion() } returns administrador
        val consulta = SolicitudInformacion(3, 2, "Horarios del curso", 1); coEvery { compras.obtenerSolicitud(3, 2) } returns consulta
        coEvery { compras.actualizarEstadoConsulta(any(), any(), any(), any()) } returns 1
        repo.cambiarEstadoSolicitud(3, SolicitudHistorial(consulta = consulta), EstadoSolicitud.CONFIRMADO)
        coVerify { compras.actualizarEstadoConsulta(3, 2, "PENDIENTE_LOCAL", "CONFIRMADO") }
        coEvery { compras.actualizarEstadoConsulta(any(), any(), any(), any()) } returns 0
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.cambiarEstadoSolicitud(3, SolicitudHistorial(consulta = consulta), EstadoSolicitud.CONFIRMADO) } }
    }
    @Test fun historialCombinaPedidosYConsultasPorFechaSinRecalcularPrecios() = runTest {
        val consulta = SolicitudInformacion(3, 2, "Horarios del curso", 20)
        every { compras.observarHistorialPedidos(3) } returns flowOf(listOf(PedidoConLineas(pedido, listOf(PedidoLinea("p1", 9, "Antiguo", "", 500.0, 4, 2000)))))
        every { compras.observarHistorialConsultas(3) } returns flowOf(listOf(ConsultaConArticulo(consulta, articulo)))
        val lista = repo.observarHistorialSolicitudes(3).first()
        assertEquals("curso-3-2", lista.first().clave); assertEquals(20L, lista.first().fecha); assertEquals("Consulta: Libro de arte", lista.first().titulo)
        assertEquals(6500L, lista.last().pedido!!.total); assertEquals(500.0, lista.last().lineas.single().precioUnitario, 0.0)
    }
    @Test fun publicaAvisoSoloAdminYValidaMuseoExistente() = runTest {
        val formulario = FormularioAviso("Nuevo museo", "Recorrido virtual disponible", destino = DestinoAviso.MUSEO, museoId = 1)
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.publicarAviso(3, formulario) } }
        coEvery { sesiones.obtenerSesion() } returns administrador; coEvery { avisos.insertar(any()) } returns 7
        assertEquals(7L, repo.publicarAviso(3, formulario)); coVerify { avisos.insertar(match { it.titulo == "Nuevo museo" && it.museoId == 1L }) }
        coEvery { museos.obtenerPorId(1) } returns null
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.publicarAviso(3, formulario) } }
    }
    @Test fun avisosSoloMarcanLecturasPropiasYNoRepitenLosLeidos() = runTest {
        val aviso = AvisoLocal(1, "Título", "Mensaje", 1, "TODOS")
        coEvery { avisos.obtenerVisible(3, 1) } returns AvisoConLectura(aviso, null)
        repo.cambiarLecturaAviso(3, 1, true); coVerify { avisos.guardarLecturas(match { it.single().usuarioId == 3L && it.single().avisoId == 1L }) }
        repo.cambiarLecturaAviso(3, 1, false); coVerify { avisos.marcarNoLeido(3, 1) }
        coEvery { avisos.obtenerVisibles(3) } returns listOf(AvisoConLectura(aviso, null), AvisoConLectura(aviso.copy(id = 2), 1))
        repo.marcarAvisosLeidos(3); coVerify(exactly = 2) { avisos.guardarLecturas(match { it.size == 1 && it.single().avisoId == 1L }) }
        coEvery { avisos.obtenerVisible(3, 1) } returns null
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.cambiarLecturaAviso(3, 1, true) } }
    }
    @Test fun abreAvisoDeMuseoValidaDestinoYGuardaLectura() = runTest {
        val aviso = AvisoLocal(1, "Título", "Mensaje", 1, "TODOS", destino = "MUSEO", museoId = 1)
        coEvery { avisos.obtenerVisible(3, 1) } returns AvisoConLectura(aviso, null)
        assertEquals(EnlaceAviso(DestinoAviso.MUSEO, 1), repo.abrirAviso(3, 1))
        coEvery { avisos.obtenerVisible(3, 1) } returns AvisoConLectura(aviso.copy(destino = "INVALIDO"), null)
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.abrirAviso(3, 1) } }
        coEvery { avisos.obtenerVisible(3, 1) } returns null
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.abrirAviso(3, 1) } }
    }
    @Test fun avisosDeSolicitudesNoAbrenPedidosAjenosNiClavesInvalidas() = runTest {
        val aviso = AvisoLocal(1, "Título", "Mensaje", 1, "CLIENTE", destino = "SOLICITUDES", solicitudClave = "pedido-p1")
        coEvery { avisos.obtenerVisible(3, 1) } returns AvisoConLectura(aviso, null); coEvery { compras.obtenerPedido("p1") } returns pedido
        assertEquals("pedido-p1", repo.abrirAviso(3, 1).solicitudClave)
        coEvery { compras.obtenerPedido("p1") } returns pedido.copy(usuarioId = 8)
        assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.abrirAviso(3, 1) } }
        for (clave in listOf("otra", "curso-3", "curso-x-2", "curso-3-x", "curso-8-2")) {
            coEvery { avisos.obtenerVisible(3, 1) } returns AvisoConLectura(aviso.copy(solicitudClave = clave), null)
            assertThrows(IllegalStateException::class.java) { kotlinx.coroutines.runBlocking { repo.abrirAviso(3, 1) } }
        }
        coEvery { avisos.obtenerVisible(3, 1) } returns AvisoConLectura(aviso.copy(solicitudClave = "curso-3-2"), null)
        coEvery { compras.obtenerSolicitud(3, 2) } returns SolicitudInformacion(3, 2, "Consulta previa", 1)
        assertEquals("curso-3-2", repo.abrirAviso(3, 1).solicitudClave)
    }
    @Test fun operacionesDeCatalogoDeleganIdsYDesactivanEnLugarDeBorrar() = runTest {
        repo.guardarArticuloCatalogo(articulo); repo.actualizarArticuloCatalogo(articulo); repo.cambiarDisponibilidadArticulo(2, false)
        repo.desactivarArticuloCatalogo(2); repo.reactivarArticuloCatalogo(2); repo.cambiarEstadoActivoArticulo(2, true); repo.eliminarArticuloCatalogo(articulo)
        coVerify { catalogo.insertar(articulo) }; coVerify { catalogo.actualizar(articulo) }; coVerify { catalogo.actualizarDisponibilidad(2, false) }
        coVerify(exactly = 2) { catalogo.actualizarEstadoActivo(2, false) }; coVerify(exactly = 2) { catalogo.actualizarEstadoActivo(2, true) }
        assertEquals(articulo, repo.obtenerArticuloCatalogo(2))
    }
    @Test fun sesionDemoUsaPerfilGuardadoYSinPerfilNoCreaSesion() = runTest {
        coEvery { usuarios.obtenerPorRol("CLIENTE") } returns listOf(usuario.copy(id = 9), usuario.copy(correo = "editado@test.cl"))
        assertEquals("editado@test.cl", repo.correoPerfilDemo("CLIENTE"))
        assertEquals(9L, repo.cambiarRolDemo("CLIENTE")!!.id)
        coEvery { usuarios.obtenerPorRol("INVESTIGADOR") } returns emptyList(); assertNull(repo.cambiarRolDemo("INVESTIGADOR")); assertNull(repo.correoPerfilDemo("INVESTIGADOR"))
        assertEquals(cliente, repo.obtenerSesion()); repo.cerrarSesion(); coVerify { sesiones.cerrarSesion() }
    }
    @Test fun sincronizacionConservaFalloYLecturaQrGuardaPropietario() = runTest {
        coEvery { api.obtenerMuseosIberoamerica() } returns listOf(museo)
        assertEquals(1, repo.sincronizarMuseosRemotos().getOrThrow()); coVerify { museos.insertarSiNoExisten(listOf(museo)) }
        val error = java.io.IOException("Sin red"); coEvery { api.obtenerMuseosIberoamerica() } throws error
        assertSame(error, repo.sincronizarMuseosRemotos().exceptionOrNull())
        repo.registrarLecturaQr(3, "QR", "Título", "Descripción", "https://museo.cl")
        coVerify { historial.guardarLectura(match { it.usuarioId == 3L && it.codigo == "QR" && it.fechaTimestamp > 0 }) }
        repo.guardarMuseo(museo); coVerify { museos.insertar(museo) }
        coEvery { museos.buscarPorCodigoQr("QR") } returns museo; assertEquals(museo, repo.buscarMuseoPorQr("QR"))
        coEvery { museos.obtenerTodosDirecto() } returns listOf(museo); assertEquals(listOf(museo), repo.obtenerMuseosDirecto())
    }
    @Test fun consultasReactivasConservanFiltrosYPropietarios() = runTest {
        every { catalogo.observarActivos() } returns flowOf(listOf(articulo)); every { catalogo.observarTodos() } returns flowOf(listOf(articulo.copy(activo = false)))
        assertTrue(repo.observarCatalogo().first().single().activo); assertFalse(repo.observarCatalogo(false).first().single().activo)
        every { catalogo.observarPorCategoria("PUBLICACION") } returns flowOf(listOf(articulo)); assertEquals(articulo, repo.observarCatalogoPorCategoria("PUBLICACION").first().single())
        every { catalogo.observarPorId(2) } returns flowOf(articulo); assertEquals(articulo, repo.observarArticuloCatalogo(2).first())
        every { sesiones.observarSesion() } returns flowOf(cliente); assertEquals(cliente, repo.observarSesion().first())
        every { museos.observarTodos() } returns flowOf(listOf(museo)); assertEquals(listOf(museo), repo.observarMuseos().first())
        every { museos.observarPorId(1) } returns flowOf(museo); assertEquals(museo, repo.observarMuseo(1).first())
        every { museos.observarPaises() } returns flowOf(listOf("Chile")); assertEquals(listOf("Chile"), repo.observarPaises().first())
        every { museos.observarPorPais("Chile") } returns flowOf(listOf(museo)); assertEquals(listOf(museo), repo.observarMuseosPorPais("Chile").first())
        every { favoritos.esFavorito(3, 1) } returns flowOf(true); assertTrue(repo.esFavorito(3, 1).first())
        every { favoritos.observarMuseosFavoritos(3) } returns flowOf(listOf(museo)); assertEquals(listOf(museo), repo.observarFavoritos(3).first())
        every { historial.observarHistorial(3) } returns flowOf(emptyList()); assertTrue(repo.observarHistorialQr(3).first().isEmpty())
        every { compras.observarCarrito(3) } returns flowOf(listOf(CarritoItem(3, 2, 1))); assertEquals(3L, repo.observarCarrito(3).first().single().usuarioId)
        every { compras.observarSolicitudes(3) } returns flowOf(emptyList()); assertTrue(repo.observarSolicitudesInformacion(3).first().isEmpty())
        every { compras.observarLineas(3) } returns flowOf(listOf(LineaCarrito(articulo, 2))); assertEquals(2, repo.observarLineasCarrito(3).first().single().cantidad)
        every { compras.observarPedidos(3) } returns flowOf(listOf(pedido)); assertEquals(pedido, repo.observarPedidos(3).first().single())
        coEvery { compras.obtenerLineasPedido(3, "p1") } returns emptyList(); assertTrue(repo.obtenerLineasPedido(3, "p1").isEmpty())
        every { usuarios.observarPerfilActivo(3) } returns flowOf(usuario); assertEquals(usuario, repo.observarPerfilActivo(3).first())
        every { usuarios.observarPreferencias(3) } returns flowOf(PreferenciasUsuario(3)); assertEquals(3L, repo.observarPreferencias(3).first()!!.usuarioId)
        every { avisos.observar(3) } returns flowOf(emptyList()); assertTrue(repo.observarAvisos(3).first().isEmpty())
    }
}
