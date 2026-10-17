package com.example.museodelosmuseos.ui.screens.registro

/**
 * ============================================================================
 * MODELO ATÓMICO: CampoFormulario
 * ============================================================================
 * Idéntico al estándar de '102 Fuente Ejemplo Formularios' del profesor.
 * Combina el texto ingresado con el mensaje de fallo correspondiente.
 * ============================================================================
 */
data class CampoFormulario(
    val valor: String = "",
    val error: String? = null
) {
    val esValido: Boolean get() = error == null && valor.isNotBlank()
}

/**
 * Estado reactivo completo de la pantalla de registro (Single Source of Truth).
 */
data class RegistroUiState(
    val nombre: CampoFormulario = CampoFormulario(),
    val correo: CampoFormulario = CampoFormulario(),
    val contrasena: CampoFormulario = CampoFormulario(),
    val avatarSeleccionado: String = "avatar_1",
    val intentoEnviar: Boolean = false,
    val guardando: Boolean = false,
    val registroExitoso: Boolean = false,
    val errorServidor: String? = null
) {
    val formularioValido: Boolean
        get() = nombre.esValido && correo.esValido && contrasena.esValido
}
