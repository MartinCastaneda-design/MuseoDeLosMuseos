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

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: LoginViewModel
 * ============================================================================
 * Propósito:
 * Gestiona la autenticación de usuarios, la selección rápida de roles demo
 * académicos (Admin, Investigador, Cliente) y la activación de sesión en Room.
 * ============================================================================
 */
class LoginViewModel(
    private val repository: MuseoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun limpiarAlCerrarSesion() { _uiState.value = LoginUiState() }

    init {
        // Inicializar con la cuenta demo de Cliente por defecto
        seleccionarRolDemo(Rol.CLIENTE)
    }

    /**
     * Rellena automáticamente las credenciales académicas de prueba
     * según el rol seleccionado para facilitar la evaluación rápida (requerimiento de la pauta).
     */
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
        // El perfil permite cambiar el correo: el acceso demo debe utilizar el dato guardado.
        viewModelScope.launch {
            val correoGuardado = repository.correoPerfilDemo(rol.name) ?: return@launch
            _uiState.update { actual ->
                if (actual.rolSeleccionado == rol && actual.correo.valor == correoDemo)
                    actual.copy(correo = CampoFormulario(correoGuardado, null)) else actual
            }
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

    // --- REGLAS DE NEGOCIO PRIVADAS ---

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
