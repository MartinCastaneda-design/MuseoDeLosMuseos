package com.example.museodelosmuseos.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.model.Rol
import com.example.museodelosmuseos.data.repository.MuseoRepository
import com.example.museodelosmuseos.ui.screens.registro.CampoFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: MuseoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {

        seleccionarRolDemo(Rol.CLIENTE)
    }

    fun seleccionarRolDemo(rol: Rol) {
        val (correoDemo, passDemo) = when (rol) {
            Rol.ADMINISTRADOR -> Pair("admin@museo.org", "admin123")
            Rol.INVESTIGADOR -> Pair("investigador@museo.org", "inves123")
            Rol.CLIENTE -> Pair("maria@email.com", "cliente123")
        }

        _uiState.update {
            it.copy(
                rolSeleccionado = rol,
                correo = CampoFormulario(correoDemo, null),
                contrasena = CampoFormulario(passDemo, null),
                error = null
            )
        }
    }

    fun cambiarCorreo(valor: String) {
        _uiState.update {
            it.copy(
                correo = CampoFormulario(valor, validarCorreo(valor)),
                error = null
            )
        }
    }

    fun cambiarContrasena(valor: String) {
        _uiState.update {
            it.copy(
                contrasena = CampoFormulario(valor, validarContrasena(valor)),
                error = null
            )
        }
    }

    fun iniciarSesion(onExito: () -> Unit) {
        val actual = _uiState.value

        val correoVal = actual.correo.copy(error = validarCorreo(actual.correo.valor))
        val passVal = actual.contrasena.copy(error = validarContrasena(actual.contrasena.valor))

        val estadoValidado = actual.copy(
            correo = correoVal,
            contrasena = passVal,
            intentoEnviar = true
        )
        _uiState.value = estadoValidado

        if (!estadoValidado.formularioValido) {
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }

        viewModelScope.launch {
            val resultado = repository.iniciarSesion(
                correo = actual.correo.valor.trim(),
                contrasena = actual.contrasena.valor.trim()
            )

            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(cargando = false, loginExitoso = true) }
                    onExito()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = err.message ?: "Credenciales de acceso incorrectas"
                        )
                    }
                }
            )
        }
    }

    private fun validarCorreo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(valor.trim()) ->
            "Ingresa un formato de correo válido"
        else -> null
    }

    private fun validarContrasena(valor: String): String? = when {
        valor.isBlank() -> "La contraseña es obligatoria"
        valor.trim().length < 6 -> "Escribe al menos 6 caracteres"
        else -> null
    }
}
