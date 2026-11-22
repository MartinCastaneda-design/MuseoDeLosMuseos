package com.example.museodelosmuseos.unidad3
import com.example.museodelosmuseos.ui.screens.favoritos.*
import com.example.museodelosmuseos.ui.screens.avisos.*
import com.example.museodelosmuseos.data.model.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ColeccionesAvisosViewModelTest : BaseViewModelTest() {
    @Test fun favoritosFiltraPideConfirmacionYQuitaSoloAlConfirmar() = runTest(dispatcher) {
        val vm = conservar(FavoritosViewModel(repo, 3)); runCurrent(); assertEquals(listOf(museo), vm.uiState.value.museos)
        vm.buscar("Arte"); assertEquals(listOf(museo), vm.uiState.value.filtrados); vm.buscar("Perú"); assertTrue(vm.uiState.value.filtrados.isEmpty())
        vm.pedirQuitar(museo); vm.cancelar(); assertNull(vm.uiState.value.porQuitar); vm.confirmarQuitar(); coVerify(exactly = 0) { repo.conmutarFavorito(any(), any(), any()) }
        vm.pedirQuitar(museo); vm.confirmarQuitar(); vm.confirmarQuitar(); runCurrent()
        coVerify(exactly = 1) { repo.conmutarFavorito(3, 1, false) }; assertNull(vm.uiState.value.porQuitar); assertNotNull(vm.uiState.value.confirmacion)
        museos.value = emptyList(); runCurrent(); assertTrue(vm.uiState.value.museos.isEmpty())
    }
    @Test fun favoritosValidaEnlacesYConservaErrorDeEliminacion() = runTest(dispatcher) {
        val vm = conservar(FavoritosViewModel(repo, 3)); runCurrent(); vm.abrirRecorrido(museo.copy(urlVisitaVirtual = "archivo")); assertNotNull(vm.uiState.value.error)
        vm.abrirRecorrido(museo); assertEquals(museo.urlVisitaVirtual, vm.uiState.value.enlacePendiente); vm.consumirEnlace(false); assertNull(vm.uiState.value.enlacePendiente)
        vm.abrirRecorrido(museo); vm.consumirEnlace(true); assertNotNull(vm.uiState.value.error)
        coEvery { repo.conmutarFavorito(any(), any(), any()) } throws IllegalStateException("Permiso revocado")
        vm.pedirQuitar(museo); vm.confirmarQuitar(); runCurrent(); assertEquals("Permiso revocado", vm.uiState.value.error); assertFalse(vm.uiState.value.guardando)
        sesion.value = administrador; runCurrent(); assertTrue(vm.uiState.value.museos.isEmpty()); assertNotNull(vm.uiState.value.error)
    }
    @Test fun favoritosErrorDeLecturaTerminaCarga() = runTest(dispatcher) {
        every { repo.observarFavoritos(3) } returns flow { throw IllegalStateException("Room") }
        val vm = conservar(FavoritosViewModel(repo, 3)); runCurrent(); assertFalse(vm.uiState.value.cargando); assertNotNull(vm.uiState.value.error)
    }
    @Test fun avisosSoloAdminPublicaConValidaciones() = runTest(dispatcher) {
        val vm = conservar(AvisosViewModel(repo, 3)); runCurrent(); vm.crear(); assertFalse(vm.uiState.value.mostrarFormulario)
        vm.publicar(); assertNotNull(vm.uiState.value.error); coVerify(exactly = 0) { repo.publicarAviso(any(), any()) }
        sesion.value = administrador; runCurrent(); vm.crear(); assertTrue(vm.uiState.value.mostrarFormulario); vm.publicar(); assertNotNull(vm.uiState.value.formulario.errorTitulo)
        vm.formulario { it.copy(titulo = "Nuevo museo", mensaje = "Ya puedes recorrer la nueva colección") }; vm.publicar(); runCurrent()
        coVerify { repo.publicarAviso(3, match { it.titulo == "Nuevo museo" }) }; assertFalse(vm.uiState.value.mostrarFormulario); assertNotNull(vm.uiState.value.confirmacion)
        vm.crear(); vm.cerrarFormulario(); assertFalse(vm.uiState.value.mostrarFormulario)
    }
    @Test fun avisosLecturaEnlaceYFiltrosAfectanAlUsuarioActivo() = runTest(dispatcher) {
        val aviso = com.example.museodelosmuseos.data.local.entity.AvisoLocal(id = 1, titulo = "Museo abierto", mensaje = "Nueva colección disponible", fechaTimestamp = 1, rolDestino = "TODOS")
        every { repo.observarAvisos(3) } returns flowOf(listOf(AvisoConLectura(aviso, null), AvisoConLectura(aviso.copy(id = 2), 10)))
        coEvery { repo.abrirAviso(3, 1) } returns EnlaceAviso(DestinoAviso.MUSEO, 1)
        val vm = conservar(AvisosViewModel(repo, 3)); runCurrent(); assertEquals(1, vm.uiState.value.noLeidos)
        vm.filtrar(FiltroAvisos.NO_LEIDOS); assertEquals(1L, vm.uiState.value.filtrados.single().aviso.id)
        vm.filtrar(FiltroAvisos.LEIDOS); assertEquals(2L, vm.uiState.value.filtrados.single().aviso.id)
        vm.lectura(1, true); runCurrent(); coVerify { repo.cambiarLecturaAviso(3, 1, true) }
        vm.lectura(1, false); runCurrent(); assertTrue(vm.uiState.value.confirmacion!!.contains("no leído"))
        vm.marcarTodos(); runCurrent(); coVerify { repo.marcarAvisosLeidos(3) }
        vm.abrir(1); runCurrent(); assertEquals(1L, vm.uiState.value.enlacePendiente!!.museoId); vm.consumirEnlace(); assertNull(vm.uiState.value.enlacePendiente)
        coEvery { repo.marcarAvisosLeidos(3) } throws IllegalStateException("Disco lleno")
        vm.marcarTodos(); runCurrent(); assertEquals("Disco lleno", vm.uiState.value.error); assertFalse(vm.uiState.value.guardando)
        sesion.value = null; runCurrent(); assertTrue(vm.uiState.value.avisos.isEmpty()); assertNull(vm.uiState.value.sesion)
    }
    @Test fun avisosErrorDeCargaTieneMensajeRecuperable() = runTest(dispatcher) {
        every { repo.observarAvisos(3) } returns flow { throw IllegalStateException("Room") }
        val vm = conservar(AvisosViewModel(repo, 3)); runCurrent(); assertFalse(vm.uiState.value.cargando); assertNotNull(vm.uiState.value.error)
    }
}
