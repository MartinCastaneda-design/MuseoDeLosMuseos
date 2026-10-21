package com.example.museodelosmuseos.ui.screens.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistroViewModel(
    private val repository: MuseoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun cambiarNombre(valor: String) {
        _uiState.update {
            it.copy(
                nombre = CampoFormulario(valor, validarNombre(valor)),
                errorServidor = null
            )
        }
    }

    fun cambiarCorreo(valor: String) {
        _uiState.update {
            it.copy(
                correo = CampoFormulario(valor, validarCorreo(valor)),
                errorServidor = null
            )
        }
    }

    fun cambiarContrasena(valor: String) {
        _uiState.update {
            it.copy(
                contrasena = CampoFormulario(valor, validarContrasena(valor)),
                errorServidor = null
            )
        }
    }

    fun seleccionarAvatar(avatar: String) {
        _uiState.update { it.copy(avatarSeleccionado = avatar) }
    }

    fun registrar(onExito: () -> Unit) {
        val actual = _uiState.value

        val nombreVal = actual.nombre.copy(error = validarNombre(actual.nombre.valor))
        val correoVal = actual.correo.copy(error = validarCorreo(actual.correo.valor))
        val passVal = actual.contrasena.copy(error = validarContrasena(actual.contrasena.valor))

        val estadoValidado = actual.copy(
            nombre = nombreVal,
            correo = correoVal,
            contrasena = passVal,
            intentoEnviar = true
        )
        _uiState.value = estadoValidado

        if (!estadoValidado.formularioValido) {
            return
        }

        _uiState.update { it.copy(guardando = true, errorServidor = null) }
        viewModelScope.launch {
            val resultado = repository.registrarUsuario(
                nombre = actual.nombre.valor.trim(),
                correo = actual.correo.valor.trim(),
                pass = actual.contrasena.valor,
                avatar = actual.avatarSeleccionado
            )

            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(guardando = false, registroExitoso = true) }
                    onExito()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            errorServidor = error.message ?: "No se pudo completar el registro"
                        )
                    }
                }
            )
        }
    }

    private fun validarNombre(valor: String): String? = when {
        valor.isBlank() -> "El nombre completo es obligatorio"
        valor.trim().length < 3 -> "Escribe al menos 3 caracteres"
        else -> null
    }

    private fun validarCorreo(valor: String): String? = when {
        valor.isBlank() -> "El correo electrónico es obligatorio"
        !Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(valor.trim()) ->
            "Ingresa un correo electrónico válido (ej: nombre@correo.com)"
        else -> null
    }

    private fun validarContrasena(valor: String): String? = when {
        valor.isBlank() -> "La contraseña es obligatoria"
        valor.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }
}
