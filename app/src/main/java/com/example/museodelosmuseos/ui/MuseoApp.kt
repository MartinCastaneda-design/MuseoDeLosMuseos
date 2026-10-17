package com.example.museodelosmuseos.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.data.local.AppDatabase
import com.example.museodelosmuseos.data.repository.MuseoRepository
import com.example.museodelosmuseos.ui.navigation.DestinoPantalla
import com.example.museodelosmuseos.ui.screens.PantallaBienvenida
import com.example.museodelosmuseos.ui.screens.login.LoginViewModel
import com.example.museodelosmuseos.ui.screens.login.PantallaLogin
import com.example.museodelosmuseos.ui.screens.registro.PantallaRegistro
import com.example.museodelosmuseos.ui.screens.registro.RegistroViewModel
import com.example.museodelosmuseos.ui.theme.AzulMuseoProfundo
import com.example.museodelosmuseos.ui.theme.DoradoBrillante
import com.example.museodelosmuseos.ui.theme.FondoOscuro
import com.example.museodelosmuseos.ui.theme.TextoPrimario

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
    val loginViewModel = remember(repository) { LoginViewModel(repository) }

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
            PantallaLogin(
                viewModel = loginViewModel,
                onVolver = volverABienvenida,
                onLoginExitoso = {
                    destinoActual = DestinoPantalla.Dashboard
                },
                onIrARegistro = {
                    destinoActual = DestinoPantalla.Registro
                }
            )
        }

        is DestinoPantalla.Dashboard -> {
            // STUB TEMPORAL — será reemplazado por PantallaDashboard en Pantalla 4
            val sesion by repository.observarSesion().collectAsStateWithLifecycle(null)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FondoOscuro),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "✅ Sesión iniciada",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = DoradoBrillante,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = sesion?.let { "Bienvenido/a, ${it.nombre}\nRol: ${it.rol}" }
                            ?: "Cargando sesión...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextoPrimario,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "(Pantalla 4 — Dashboard en construcción)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoPrimario.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
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
