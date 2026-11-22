package com.example.museodelosmuseos.unidad3

import com.example.museodelosmuseos.ui.screens.login.LoginViewModel
import com.example.museodelosmuseos.ui.screens.registro.RegistroViewModel
import com.example.museodelosmuseos.data.model.Rol
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AutenticacionViewModelTest : BaseViewModelTest() {
    @Test fun loginValidaSinConsultarRepositorio() = runTest(dispatcher) {
        val vm = conservar(LoginViewModel(repo)); runCurrent()
        for (correo in listOf("", "invalido")) { vm.cambiarCorreo(correo); vm.cambiarContrasena("123"); vm.iniciarSesion { fail() }; assertNotNull(vm.uiState.value.correo.error) }
        vm.cambiarCorreo("valido@test.cl"); vm.cambiarContrasena(""); vm.iniciarSesion { fail() }
        assertNotNull(vm.uiState.value.contrasena.error)
        coVerify(exactly = 0) { repo.iniciarSesion(any(), any()) }
    }
    @Test fun loginExitosoNormalizaYNotificaSoloAlTerminar() = runTest(dispatcher) {
        coEvery { repo.iniciarSesion("valido@test.cl", "clave123") } returns Result.success(usuario)
        val vm = conservar(LoginViewModel(repo)); runCurrent(); vm.cambiarCorreo(" valido@test.cl "); vm.cambiarContrasena(" clave123 ")
        var entro = false; vm.iniciarSesion { entro = true }
        assertTrue(vm.uiState.value.cargando); assertFalse(entro); runCurrent()
        assertTrue(entro); assertTrue(vm.uiState.value.loginExitoso); assertFalse(vm.uiState.value.cargando)
        coVerify(exactly = 1) { repo.iniciarSesion("valido@test.cl", "clave123") }
        vm.limpiarAlCerrarSesion(); assertFalse(vm.uiState.value.loginExitoso)
    }
    @Test fun loginRechazadoConservaErrorYPermiteCorregir() = runTest(dispatcher) {
        coEvery { repo.iniciarSesion(any(), any()) } returns Result.failure(Exception("Acceso denegado"))
        val vm = conservar(LoginViewModel(repo)); runCurrent(); vm.iniciarSesion { fail() }; runCurrent()
        vm.uiState.value.error shouldBe "Acceso denegado"; assertFalse(vm.uiState.value.cargando)
        vm.cambiarContrasena("otra123"); assertNull(vm.uiState.value.error)
        for (rol in Rol.entries) { vm.seleccionarRolDemo(rol); runCurrent(); vm.uiState.value.rolSeleccionado shouldBe rol }
    }
    @Test fun correoDemoGuardadoNoSobrescribeLoEscritoDuranteLaConsulta() = runTest(dispatcher) {
        coEvery { repo.correoPerfilDemo(any()) } coAnswers { kotlinx.coroutines.delay(10); "editado@test.cl" }
        val vm = conservar(LoginViewModel(repo)); runCurrent(); vm.cambiarCorreo("personal@test.cl"); advanceUntilIdle()
        vm.uiState.value.correo.valor shouldBe "personal@test.cl"
        vm.seleccionarRolDemo(Rol.ADMINISTRADOR); advanceUntilIdle(); vm.uiState.value.correo.valor shouldBe "editado@test.cl"
    }
    @Test fun registroValidaCamposAntesDeGuardar() = runTest(dispatcher) {
        val vm = conservar(RegistroViewModel(repo)); vm.registrar { fail() }
        assertNotNull(vm.uiState.value.nombre.error); assertNotNull(vm.uiState.value.correo.error); assertNotNull(vm.uiState.value.contrasena.error)
        vm.cambiarNombre("ab"); vm.cambiarCorreo("sin-arroba"); vm.cambiarContrasena("123")
        assertFalse(vm.uiState.value.formularioValido); vm.registrar { fail() }
        coVerify(exactly = 0) { repo.registrarUsuario(any(), any(), any(), any()) }
    }
    @Test fun registroGuardaDatosNormalizadosYAvatar() = runTest(dispatcher) {
        coEvery { repo.registrarUsuario(any(), any(), any(), any()) } returns Result.success(9)
        val vm = conservar(RegistroViewModel(repo)); vm.cambiarNombre(" Ana Pérez "); vm.cambiarCorreo(" ana@test.cl "); vm.cambiarContrasena("clave123"); vm.seleccionarAvatar("avatar_2")
        var entro = false; vm.registrar { entro = true }; assertTrue(vm.uiState.value.guardando); runCurrent()
        assertTrue(entro); assertTrue(vm.uiState.value.registroExitoso); assertFalse(vm.uiState.value.guardando)
        coVerify { repo.registrarUsuario("Ana Pérez", "ana@test.cl", "clave123", "avatar_2") }
    }
    @Test fun registroMuestraCorreoDuplicadoYLiberaFormulario() = runTest(dispatcher) {
        coEvery { repo.registrarUsuario(any(), any(), any(), any()) } returns Result.failure(Exception("Correo duplicado"))
        val vm = conservar(RegistroViewModel(repo)); vm.cambiarNombre("Ana"); vm.cambiarCorreo("ana@test.cl"); vm.cambiarContrasena("clave123"); vm.registrar { fail() }; runCurrent()
        vm.uiState.value.errorServidor shouldBe "Correo duplicado"; assertFalse(vm.uiState.value.guardando)
        vm.cambiarCorreo("otra@test.cl"); assertNull(vm.uiState.value.errorServidor)
    }
}
