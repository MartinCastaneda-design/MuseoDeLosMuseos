package com.example.museodelosmuseos.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.museodelosmuseos.data.local.AppDatabase
import com.example.museodelosmuseos.data.repository.MuseoRepository
import com.example.museodelosmuseos.ui.navigation.DestinoPantalla
import com.example.museodelosmuseos.ui.screens.PantallaBienvenida
import com.example.museodelosmuseos.ui.screens.login.LoginViewModel
import com.example.museodelosmuseos.ui.screens.login.PantallaLogin
import com.example.museodelosmuseos.ui.screens.registro.PantallaRegistro
import com.example.museodelosmuseos.ui.screens.registro.RegistroViewModel

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: MuseoApp (Vista Raíz)
 * ============================================================================
 * Coordina la navegación reactiva entre las pantallas del proyecto
 * y provee el repositorio central de datos a los ViewModels.
 * ============================================================================
 */
@Composable
fun MuseoApp() {
    val context = LocalContext.current

    // Instancia única del repositorio para toda la jerarquía de vistas
    val database = remember { AppDatabase.obtenerInstancia(context) }
    val repository = remember { MuseoRepository(database) }
    val registroViewModel = remember(repository) { RegistroViewModel(repository) }

    // Estado reactivo de navegación (Single Source of Truth para la vista activa)
    var destinoActual by remember { mutableStateOf<DestinoPantalla>(DestinoPantalla.Bienvenida) }

    // Pila simple de retorno hacia atrás
    val volverABienvenida = { destinoActual = DestinoPantalla.Bienvenida }

    BackHandler(enabled = destinoActual != DestinoPantalla.Bienvenida) {
        volverABienvenida()
    }

    // Conmutador reactivo de pantallas (UDF)
    when (val destino = destinoActual) {
        is DestinoPantalla.Bienvenida -> {
            PantallaBienvenida(
                onComenzarClick = {
                    // Lleva al registro o selección de rol
                    destinoActual = DestinoPantalla.Registro
                },
                onIniciarSesionClick = {
                    // Lleva al login
                    destinoActual = DestinoPantalla.Login
                },
                onExplorarMapaClick = {
                    // Acceso directo al mapa interactivo
                    destinoActual = DestinoPantalla.Mapa
                }
            )
        }

        is DestinoPantalla.Registro -> {
            PantallaRegistro(
                viewModel = registroViewModel,
                onVolver = volverABienvenida,
                onRegistroExitoso = {
                    destinoActual = DestinoPantalla.Dashboard
                },
                onIrALogin = {
                    destinoActual = DestinoPantalla.Login
                }
            )
        }

        is DestinoPantalla.Login -> {
            // Se conectará en Pantalla 3
            PantallaBienvenida(
                onComenzarClick = {},
                onIniciarSesionClick = {},
                onExplorarMapaClick = {}
            )
        }

        is DestinoPantalla.Dashboard -> {
            // Se conectará en Pantalla 4
            PantallaBienvenida(
                onComenzarClick = {},
                onIniciarSesionClick = {},
                onExplorarMapaClick = {}
            )
        }

        is DestinoPantalla.Mapa -> {
            // Se conectará en Pantalla 5
            PantallaBienvenida(
                onComenzarClick = {},
                onIniciarSesionClick = {},
                onExplorarMapaClick = {}
            )
        }

        is DestinoPantalla.DetalleMuseo -> {
            // Se conectará en Pantalla 6
        }

        is DestinoPantalla.EscanerQr -> {
            // Se conectará en Pantalla 7
        }

        is DestinoPantalla.ResultadoQr -> {
            // Se conectará en Pantalla 8
        }
    }
}
