package com.example.museodelosmuseos.ui.screens.login

import com.example.museodelosmuseos.data.model.Rol
import com.example.museodelosmuseos.ui.screens.registro.CampoFormulario

/**
 * Estado inmutable de la pantalla de selección e inicio de sesión.
 */
data class LoginUiState(
    val correo: CampoFormulario = CampoFormulario(),
    val contrasena: CampoFormulario = CampoFormulario(),
    val rolSeleccionado: Rol = Rol.CLIENTE,
    val cargando: Boolean = false,
    val error: String? = null,
    val intentoEnviar: Boolean = false,
    val loginExitoso: Boolean = false
) {
    val formularioValido: Boolean
        get() = correo.esValido && contrasena.esValido
}
