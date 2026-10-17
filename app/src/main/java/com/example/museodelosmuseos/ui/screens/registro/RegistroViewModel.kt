package com.example.museodelosmuseos.ui.screens.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: RegistroViewModel
 * ============================================================================
 * Propósito:
 * Gestiona el estado reactivo, la lógica de validación centralizada y la
 * persistencia local en Room para el registro de nuevos usuarios de rol Cliente.
 *
 * Patrones implementados (según el estándar del profesor):
 * - MVVM: Desacopla la lógica de negocio y validación de la interfaz gráfica.
 * - Encapsulación: Backing Property (_uiState privado, uiState público inmutable).
 * - Single Source of Truth: Emite estados reactivos con StateFlow.
 * ============================================================================
 */
class RegistroViewModel(
    private val repository: MuseoRepository
) : ViewModel() {

    // --- ESTADO REACTIVO PRINCIPAL ---
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    // --- EVENTOS DE ENTRADA (Intents) ---

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

    /**
     * Valida todos los campos y persiste en Room si los datos son correctos.
     */
    fun registrar(onExito: () -> Unit) {
        val actual = _uiState.value

        // Barrido completo de validaciones para asegurar coherencia
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

        // Persistir en SQLite / Room usando el repositorio
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

    // --- REGLAS DE NEGOCIO PRIVADAS (Validaciones centralizadas) ---

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
