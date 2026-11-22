package com.example.museodelosmuseos.unidad3
import android.net.Uri
import com.example.museodelosmuseos.data.local.ArchivosAvatar
import com.example.museodelosmuseos.ui.screens.perfil.PerfilViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelTest : BaseViewModelTest() {
    private fun crear(): Pair<PerfilViewModel, ArchivosAvatar> { val archivos = mockk<ArchivosAvatar>(relaxed = true); return conservar(PerfilViewModel(repo, 3, archivos)) to archivos }
    @Test fun perfilCargaDatosValidaYGuardaPreferencias() = runTest(dispatcher) {
        val (vm, _) = crear(); runCurrent(); assertEquals(usuario, vm.uiState.value.usuario); assertFalse(vm.uiState.value.cargando)
        vm.nombre("a"); vm.correo("invalido"); vm.guardar(); assertNotNull(vm.uiState.value.nombre.error); assertNotNull(vm.uiState.value.correo.error)
        coVerify(exactly = 0) { repo.guardarPerfil(any(), any(), any(), any(), any()) }
        vm.nombre("Ana Pérez"); vm.correo("ana@test.cl"); vm.indicador(false); vm.textoGrande(true); vm.avatarPredeterminado(); vm.guardar(); runCurrent()
        coVerify { repo.guardarPerfil(3, "Ana Pérez", "ana@test.cl", "avatar_1", match { it.usuarioId == 3L && !it.indicadorAvisos && it.textoGrande }) }
        assertFalse(vm.uiState.value.modificado); assertNotNull(vm.uiState.value.confirmacion)
    }
    @Test fun borradorNoSeSobrescribeYNoPasaAOtroUsuario() = runTest(dispatcher) {
        val perfil = MutableStateFlow<com.example.museodelosmuseos.data.local.entity.Usuario?>(usuario)
        every { repo.observarPerfilActivo(3) } returns perfil
        val (vm, _) = crear(); runCurrent(); vm.nombre("Borrador personal"); perfil.value = usuario.copy(nombre = "Desde Room"); runCurrent()
        assertEquals("Borrador personal", vm.uiState.value.nombre.valor)
        sesion.value = cliente.copy(usuarioId = 8); runCurrent(); assertNull(vm.uiState.value.usuario); assertEquals("", vm.uiState.value.nombre.valor)
        vm.nombre("No autorizado"); assertEquals("", vm.uiState.value.nombre.valor)
    }
    @Test fun perfilMuestraErrorDePersistenciaYNoPierdeEdicion() = runTest(dispatcher) {
        coEvery { repo.guardarPerfil(any(), any(), any(), any(), any()) } throws IllegalStateException("Correo duplicado")
        val (vm, _) = crear(); runCurrent(); vm.nombre("Otro nombre"); vm.guardar(); runCurrent()
        assertEquals("Correo duplicado", vm.uiState.value.error); assertTrue(vm.uiState.value.modificado); assertFalse(vm.uiState.value.guardando)
    }
    @Test fun avatarCanceladoConservaAnteriorYFinaliza() = runTest(dispatcher) {
        val (vm, archivos) = crear(); runCurrent(); var termino = false
        vm.recibirImagen(null, 3) { termino = true }; assertTrue(termino); assertEquals("avatar_1", vm.uiState.value.avatar)
        coVerify(exactly = 0) { archivos.importar(any(), any()) }
        vm.recibirImagen(mockk<Uri>(), 8); coVerify(exactly = 0) { archivos.importar(any(), any()) }
    }
    @Test fun avatarImportadoSeDescartaOSoloSeConservaAlGuardar() = runTest(dispatcher) {
        val (vm, archivos) = crear(); runCurrent(); val uri = mockk<Uri>()
        coEvery { archivos.importar(uri, 3) } returns "avatar-borrador"
        vm.recibirImagen(uri, 3); assertTrue(vm.uiState.value.guardando); runCurrent()
        assertEquals("avatar-borrador", vm.uiState.value.avatar); vm.descartarBorrador(); runCurrent(); coVerify { archivos.borrarBorrador("avatar-borrador", 3) }
        vm.recibirImagen(uri, 3); runCurrent(); clearMocks(archivos, answers = false); vm.guardar(); runCurrent()
        coVerify(exactly = 0) { archivos.borrarBorrador(any(), any()) }; assertNotNull(vm.uiState.value.confirmacion)
    }
    @Test fun avatarErrorYLuegoCierreLimpianEstado() = runTest(dispatcher) {
        val (vm, archivos) = crear(); runCurrent()
        coEvery { archivos.importar(any(), any()) } throws IllegalStateException("Imagen inválida")
        vm.recibirImagen(mockk<Uri>(), 3); runCurrent(); assertEquals("Imagen inválida", vm.uiState.value.error); assertFalse(vm.uiState.value.guardando)
        vm.cerrarSesion(); runCurrent(); coVerify { repo.cerrarSesionDePerfil(3) }; assertTrue(vm.uiState.value.salida); assertNull(vm.uiState.value.usuario)
    }
    @Test fun errorDeCargaYDeCierreSeMuestran() = runTest(dispatcher) {
        every { repo.observarPerfilActivo(3) } returns flow { throw IllegalStateException("Room") }
        val (vm, _) = crear(); runCurrent(); assertFalse(vm.uiState.value.cargando); assertNotNull(vm.uiState.value.error)
        coEvery { repo.cerrarSesionDePerfil(3) } throws IllegalStateException("Sesión cambiada")
        vm.cerrarSesion(); runCurrent(); assertEquals("Sesión cambiada", vm.uiState.value.error); assertFalse(vm.uiState.value.guardando)
    }
}
