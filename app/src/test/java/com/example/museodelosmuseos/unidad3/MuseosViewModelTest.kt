package com.example.museodelosmuseos.unidad3
import com.example.museodelosmuseos.ui.screens.mapa.MapaViewModel
import com.example.museodelosmuseos.ui.screens.dashboard.*
import com.example.museodelosmuseos.ui.screens.detalle.DetalleMuseoViewModel
import com.example.museodelosmuseos.ui.screens.escaner.EscanerQrViewModel
import com.example.museodelosmuseos.ui.screens.resultadoqr.ResultadoQrViewModel
import com.example.museodelosmuseos.ui.navigation.DestinoPantalla
import com.example.museodelosmuseos.data.repository.ClimaRepository
import com.example.museodelosmuseos.data.local.entity.*
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.data.model.clima.InfoClima
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MuseosViewModelTest : BaseViewModelTest() {
    @Test fun mapaCombinaFiltrosYDeseleccionaMuseosFueraDelFiltro() = runTest(dispatcher) {
        val otro = museo.copy(id = 2, pais = "Perú", clasificacion = "Historia"); museos.value = listOf(museo, otro)
        val vm = conservar(MapaViewModel(repo)); runCurrent(); assertEquals(listOf("Todos", "Chile", "Perú"), vm.uiState.value.listaPaises)
        vm.seleccionarMuseo(museo); vm.filtrarPorPais("Perú"); assertNull(vm.uiState.value.museoSeleccionado); assertEquals(listOf(otro), vm.uiState.value.museosFiltrados)
        vm.filtrarPorProyecto("Arte"); assertTrue(vm.uiState.value.museosFiltrados.isEmpty()); vm.filtrarPorPais("Todos"); assertEquals(listOf(museo), vm.uiState.value.museosFiltrados)
        vm.seleccionarMuseo(museo); vm.filtrarPorProyecto("Todos"); assertEquals(museo, vm.uiState.value.museoSeleccionado)
        coEvery { repo.sincronizarMuseosRemotos() } returns Result.failure(Exception("Sin red")); vm.sincronizarConApiFicticia(); runCurrent()
        assertFalse(vm.uiState.value.sincronizando); assertTrue(vm.uiState.value.mensaje!!.contains("locales")); vm.limpiarMensaje(); assertNull(vm.uiState.value.mensaje)
    }
    @Test fun dashboardRespetaPreferenciaDeAvisosYCierraSesion() = runTest(dispatcher) {
        val preferencias = MutableStateFlow<PreferenciasUsuario?>(PreferenciasUsuario(3))
        every { repo.observarPreferencias(3) } returns preferencias
        every { repo.observarAvisos(3) } returns flowOf(listOf(AvisoConLectura(AvisoLocal(1, "Título", "Mensaje", 1, "TODOS"), null)))
        museos.value = (1L..5L).map { museo.copy(id = it) }
        val vm = conservar(DashboardViewModel(repo)); runCurrent(); assertEquals(3, vm.uiState.value.museosDestacados.size); assertEquals(1, vm.uiState.value.avisosNoLeidos); assertTrue(vm.uiState.value.animacionLista)
        preferencias.value = PreferenciasUsuario(3, indicadorAvisos = false); runCurrent(); assertEquals(0, vm.uiState.value.avisosNoLeidos)
        vm.seleccionarTab(TabDashboard.PERFIL); assertEquals(TabDashboard.PERFIL, vm.uiState.value.tabSeleccionada)
        var salio = false; vm.cerrarSesion { salio = true }; runCurrent(); assertTrue(salio); coVerify { repo.cerrarSesion() }
        sesion.value = null; runCurrent(); assertFalse(vm.uiState.value.puedeAccederCatalogo)
    }
    @Test fun dashboardToleraFalloDeContador() = runTest(dispatcher) {
        every { repo.observarAvisos(3) } returns flow { throw IllegalStateException("Room") }
        val vm = conservar(DashboardViewModel(repo)); runCurrent(); assertEquals(0, vm.uiState.value.avisosNoLeidos)
    }
    private fun clima(): ClimaRepository = mockk<ClimaRepository>().also { coEvery { it.consultarClimaCiudad(any(), any()) } returns Result.success(InfoClima(20.0, "Despejado", "sol", 50, 3.0, "Visita")) }
    @Test fun detalleClimaFavoritoYErroresSeReflejanEnEstado() = runTest(dispatcher) {
        val clima = clima(); val vm = conservar(DetalleMuseoViewModel(repo, 1, clima)); runCurrent()
        assertEquals(museo, vm.uiState.value.museo); assertEquals(20.0, vm.uiState.value.clima!!.temperatura, 0.0)
        vm.alternarFavorito(); runCurrent(); coVerify { repo.conmutarFavorito(3, 1, true) }; assertNotNull(vm.uiState.value.mensaje)
        coEvery { repo.conmutarFavorito(any(), any(), any()) } throws IllegalStateException("Sin acceso")
        vm.alternarFavorito(); runCurrent(); assertEquals("Sin acceso", vm.uiState.value.mensaje)
        coEvery { clima.consultarClimaCiudad(any(), any()) } returns Result.failure(Exception("Sin red")); vm.reintentarClima(); runCurrent(); assertEquals("Sin red", vm.uiState.value.errorClima); assertFalse(vm.uiState.value.cargandoClima)
        sesion.value = administrador; runCurrent(); vm.alternarFavorito(); runCurrent(); coVerify(exactly = 2) { repo.conmutarFavorito(any(), any(), any()) }
        sesion.value = null; runCurrent(); vm.alternarFavorito(); assertNull(vm.uiState.value.sesion)
    }
    @Test fun detalleValidaYGuardaDescripcionYRevision() = runTest(dispatcher) {
        val vm = conservar(DetalleMuseoViewModel(repo, 1, clima())); runCurrent(); vm.abrirDialogoEdicion(); assertTrue(vm.uiState.value.mostrarDialogoEdicion)
        vm.actualizarDescripcionEditada(" "); vm.guardarCorreccionFicha(); assertTrue(vm.uiState.value.mensaje!!.contains("vacía"))
        vm.actualizarDescripcionEditada(" Nueva descripción "); vm.actualizarNotasInvestigacion("Notas de campo"); vm.guardarCorreccionFicha(); runCurrent()
        coVerify { repo.guardarMuseo(match { it.descripcion == "Nueva descripción" && it.id == 1L }) }; assertFalse(vm.uiState.value.mostrarDialogoEdicion)
        vm.abrirDialogoEdicion(); vm.cerrarDialogoEdicion(); assertFalse(vm.uiState.value.mostrarDialogoEdicion)
        vm.abrirDialogoRevision(); vm.cerrarDialogoRevision(); assertFalse(vm.uiState.value.mostrarDialogoRevision)
        vm.abrirDialogoRevision(); vm.autorizarPublicacion(); assertTrue(vm.uiState.value.fichaPublicada); vm.limpiarMensaje(); assertNull(vm.uiState.value.mensaje)
    }
    @Test fun escanerDistingueVacioConocidoYDesconocido() = runTest(dispatcher) {
        coEvery { repo.buscarMuseoPorQr("MUSEO_1") } returns museo; coEvery { repo.buscarMuseoPorQr("desconocido") } returns null
        val vm = conservar(EscanerQrViewModel(repo)); runCurrent(); assertEquals(listOf("MUSEO_1"), vm.uiState.value.codigosParaPrueba)
        vm.procesarCodigoQr(" ") { fail() }; assertNotNull(vm.uiState.value.mensajeError); vm.limpiarError(); assertNull(vm.uiState.value.mensajeError)
        var resultado: DestinoPantalla.ResultadoQr? = null; vm.procesarCodigoQr(" MUSEO_1 ") { resultado = it }; runCurrent()
        assertTrue(resultado!!.esValido); assertEquals(1L, resultado!!.museoId); coVerify { repo.registrarLecturaQr(3, "MUSEO_1", museo.nombre, any(), museo.urlVisitaVirtual) }
        vm.procesarCodigoQr("desconocido") { resultado = it }; runCurrent(); assertFalse(resultado!!.esValido); assertFalse(vm.uiState.value.procesando)
    }
    @Test fun resultadoCargaMuseoRegistraLecturaYEsperaAnimacion() = runTest(dispatcher) {
        val resultado = DestinoPantalla.ResultadoQr("MUSEO_1", "Título QR", "Descripción QR", true, 1)
        val vm = conservar(ResultadoQrViewModel(repo, resultado)); runCurrent(); assertEquals(museo.nombre, vm.uiState.value.titulo); assertFalse(vm.uiState.value.animacionLista)
        advanceTimeBy(151); runCurrent(); assertTrue(vm.uiState.value.animacionLista); coVerify { repo.registrarLecturaQr(3, "MUSEO_1", "Título QR", "Descripción QR", any()) }
    }
    @Test fun resultadoInvalidoNoRegistraLectura() = runTest(dispatcher) {
        val vm = conservar(ResultadoQrViewModel(repo, DestinoPantalla.ResultadoQr("x", "No registrado", "Sin museo", false, null))); advanceUntilIdle()
        assertNull(vm.uiState.value.museo); assertFalse(vm.uiState.value.esValido); coVerify(exactly = 0) { repo.registrarLecturaQr(any(), any(), any(), any(), any()) }
    }
}
