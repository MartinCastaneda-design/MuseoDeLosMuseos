package com.example.museodelosmuseos.ui.screens.perfil

import com.example.museodelosmuseos.data.local.entity.Usuario
import com.example.museodelosmuseos.ui.screens.registro.CampoFormulario

data class PerfilUiState(
    val usuario: Usuario? = null,
    val nombre: CampoFormulario = CampoFormulario(),
    val correo: CampoFormulario = CampoFormulario(),
    val avatar: String = "avatar_1",
    val indicadorAvisos: Boolean = true,
    val textoGrande: Boolean = false,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val modificado: Boolean = false,
    val error: String? = null,
    val confirmacion: String? = null,
    val salida: Boolean = false
)
