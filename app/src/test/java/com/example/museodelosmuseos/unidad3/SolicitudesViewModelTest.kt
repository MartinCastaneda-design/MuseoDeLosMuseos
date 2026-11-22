package com.example.museodelosmuseos.unidad3
import com.example.museodelosmuseos.ui.screens.solicitudes.SolicitudesViewModel
import com.example.museodelosmuseos.data.repository.MicroservicioRepository
import com.example.museodelosmuseos.data.remote.microservicio.SolicitudCulturalDto
import com.example.museodelosmuseos.data.local.entity.PedidoLocal
import com.example.museodelosmuseos.data.model.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SolicitudesViewModelTest : BaseViewModelTest() {
    private val dto = SolicitudCulturalDto(1, "Consulta", "Descripción", "CURSO", "Museo", "Ana", "ana@test.cl")
    private val pedido = SolicitudHistorial(pedido = PedidoLocal("abc", 3, 10, "NACIONAL", "ESTANDAR", "Santiago", "Calle 123", 1000, 4000, 5000))
    private fun remoto(): MicroservicioRepository = mockk<MicroservicioRepository>(relaxed = true).also {
        every { it.obtenerBaseUrl() } returns "http://localhost:8080/"
        coEvery { it.listarSolicitudes() } returns Result.success(listOf(dto))
    }
    @Test fun cargaLocalesYRemotasYFiltrosConservanMontos() = runTest(dispatcher) {
        every { repo.observarHistorialSolicitudes(3) } returns flowOf(listOf(pedido))
        val vm = conservar(SolicitudesViewModel(repo, 3, pedido.clave, remoto())); runCurrent()
        assertEquals(pedido.clave, vm.uiState.value.expandida); assertEquals(5000L, vm.uiState.value.solicitudes.single().pedido!!.total)
        assertTrue(vm.uiState.value.servidorConectado); vm.filtrar(EstadoSolicitud.ENTREGADO); assertTrue(vm.uiState.value.filtradas.isEmpty()); assertTrue(vm.uiState.value.filtradasRemotas.isEmpty())
        vm.filtrar(null); assertEquals(1, vm.uiState.value.filtradasRemotas.size); vm.expandir(pedido.clave); vm.expandir(pedido.clave); assertNull(vm.uiState.value.expandida)
        vm.alternarModo(false); assertFalse(vm.uiState.value.modoMicroservicio); vm.editar(pedido); assertNull(vm.uiState.value.editando)
    }
    @Test fun administradorValidaCambioYGuardaEstadoLocal() = runTest(dispatcher) {
        sesion.value = administrador; val vm = conservar(SolicitudesViewModel(repo, 3, microservicioRepository = remoto())); runCurrent()
        vm.editar(pedido); vm.guardar(); assertNotNull(vm.uiState.value.error); vm.seleccionar(EstadoSolicitud.PENDIENTE); vm.guardar(); assertNotNull(vm.uiState.value.error)
        vm.seleccionar(EstadoSolicitud.CONFIRMADO); vm.guardar(); runCurrent(); coVerify { repo.cambiarEstadoSolicitud(3, pedido, EstadoSolicitud.CONFIRMADO) }
        assertNull(vm.uiState.value.editando); assertNotNull(vm.uiState.value.confirmacion); vm.limpiarMensaje(); assertNull(vm.uiState.value.confirmacion)
        vm.editar(pedido); vm.cancelar(); assertNull(vm.uiState.value.editando)
        coEvery { repo.cambiarEstadoSolicitud(any(), any(), any()) } throws IllegalStateException("Estado cambió")
        vm.editar(pedido); vm.seleccionar(EstadoSolicitud.ENVIADO); vm.guardar(); runCurrent(); assertEquals("Estado cambió", vm.uiState.value.error); assertFalse(vm.uiState.value.guardando)
    }
    @Test fun cargaRemotaFallidaYReintentoServidor() = runTest(dispatcher) {
        val remoto = remoto(); coEvery { remoto.listarSolicitudes() } returns Result.failure(Exception("Sin conexión"))
        val vm = conservar(SolicitudesViewModel(repo, 3, microservicioRepository = remoto)); runCurrent()
        assertFalse(vm.uiState.value.servidorConectado); assertTrue(vm.uiState.value.errorRemoto!!.contains("Sin conexión"))
        vm.abrirDialogoServidor(); assertTrue(vm.uiState.value.mostrandoDialogoServidor); vm.cerrarDialogoServidor(); assertFalse(vm.uiState.value.mostrandoDialogoServidor)
        coEvery { remoto.listarSolicitudes() } returns Result.success(listOf(dto)); vm.cambiarServidor("http://192.168.1.5:8080/"); runCurrent()
        verify { remoto.cambiarBaseUrl("http://192.168.1.5:8080/") }; assertTrue(vm.uiState.value.servidorConectado)
    }
    @Test fun crearRemotaValidaYUsaIdentidadDeSesion() = runTest(dispatcher) {
        val remoto = remoto(); coEvery { remoto.crearSolicitud(any()) } answers { Result.success(firstArg<SolicitudCulturalDto>().copy(id = 2)) }
        val vm = conservar(SolicitudesViewModel(repo, 3, microservicioRepository = remoto)); runCurrent(); vm.abrirDialogoCrear(); vm.crearSolicitudRemota("", "", "CURSO", "")
        assertNotNull(vm.uiState.value.error); coVerify(exactly = 0) { remoto.crearSolicitud(any()) }
        vm.crearSolicitudRemota(" Título ", " Descripción ", "CURSO", ""); runCurrent()
        coVerify { remoto.crearSolicitud(match { it.titulo == "Título" && it.descripcion == "Descripción" && it.solicitanteCorreo == cliente.correo && it.museoNombre == "Museo Nacional de Bellas Artes" }) }
        assertEquals(2L, vm.uiState.value.solicitudesRemotas.first().id); assertFalse(vm.uiState.value.mostrandoDialogoCrear)
        coEvery { remoto.crearSolicitud(any()) } returns Result.failure(Exception("HTTP 400")); vm.crearSolicitudRemota("Título", "Descripción", "CURSO", "Museo"); runCurrent()
        assertTrue(vm.uiState.value.error!!.contains("HTTP 400")); assertFalse(vm.uiState.value.guardando); vm.cerrarDialogoCrear(); assertFalse(vm.uiState.value.mostrandoDialogoCrear)
    }
    @Test fun editarRemotaActualizaListaYRecogeRespuesta() = runTest(dispatcher) {
        val remoto = remoto(); coEvery { remoto.actualizarSolicitud(any(), any()) } answers { Result.success(secondArg()) }
        val vm = conservar(SolicitudesViewModel(repo, 3, microservicioRepository = remoto)); runCurrent()
        vm.guardarActualizacionRemota(); coVerify(exactly = 0) { remoto.actualizarSolicitud(any(), any()) }
        vm.editarRemota(dto); vm.actualizarEstadoRemotoSeleccionado("ENVIADO"); vm.actualizarRespuestaRemotaTexto(" En camino "); vm.guardarActualizacionRemota(); runCurrent()
        assertEquals("ENVIADO", vm.uiState.value.solicitudesRemotas.single().estado); assertEquals("En camino", vm.uiState.value.solicitudesRemotas.single().respuesta)
        assertNull(vm.uiState.value.editandoRemota); vm.editarRemota(dto); vm.cancelarEdicionRemota(); assertNull(vm.uiState.value.editandoRemota)
        coEvery { remoto.actualizarSolicitud(any(), any()) } returns Result.failure(Exception("HTTP 409")); vm.editarRemota(dto); vm.guardarActualizacionRemota(); runCurrent()
        assertTrue(vm.uiState.value.error!!.contains("HTTP 409")); assertFalse(vm.uiState.value.guardando)
    }
    @Test fun eliminarRemotaSoloQuitaDeListaAlTenerExito() = runTest(dispatcher) {
        val remoto = remoto(); coEvery { remoto.eliminarSolicitud(1) } returns Result.failure(Exception("HTTP 500"))
        val vm = conservar(SolicitudesViewModel(repo, 3, microservicioRepository = remoto)); runCurrent(); vm.eliminarSolicitudRemota(1); runCurrent()
        assertEquals(1, vm.uiState.value.solicitudesRemotas.size); assertNotNull(vm.uiState.value.error)
        coEvery { remoto.eliminarSolicitud(1) } returns Result.success(true); vm.eliminarSolicitudRemota(1); runCurrent()
        assertTrue(vm.uiState.value.solicitudesRemotas.isEmpty()); assertNotNull(vm.uiState.value.confirmacion)
        vm.alternarModo(true); runCurrent(); coVerify(exactly = 2) { remoto.listarSolicitudes() }
    }
    @Test fun historialLocalInaccesibleYFalloDeConsulta() = runTest(dispatcher) {
        sesion.value = null; val vm = conservar(SolicitudesViewModel(repo, 3, microservicioRepository = remoto())); runCurrent(); assertNotNull(vm.uiState.value.error)
        every { repo.observarHistorialSolicitudes(3) } returns flow { throw IllegalStateException("Room") }
        sesion.value = cliente; runCurrent(); assertFalse(vm.uiState.value.cargando); assertTrue(vm.uiState.value.error!!.contains("cargar"))
    }
}
