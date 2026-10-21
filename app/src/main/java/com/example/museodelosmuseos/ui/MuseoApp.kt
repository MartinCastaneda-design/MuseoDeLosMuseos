package com.example.museodelosmuseos.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.museodelosmuseos.data.local.AppDatabase
import com.example.museodelosmuseos.data.repository.MuseoRepository
import com.example.museodelosmuseos.ui.navigation.DestinoPantalla
import com.example.museodelosmuseos.ui.screens.PantallaBienvenida
import com.example.museodelosmuseos.ui.screens.dashboard.DashboardViewModel
import com.example.museodelosmuseos.ui.screens.dashboard.PantallaDashboard
import com.example.museodelosmuseos.ui.screens.detalle.DetalleMuseoViewModel
import com.example.museodelosmuseos.ui.screens.detalle.PantallaDetalleMuseo
import com.example.museodelosmuseos.ui.screens.escaner.EscanerQrViewModel
import com.example.museodelosmuseos.ui.screens.escaner.PantallaEscanerQr
import com.example.museodelosmuseos.ui.screens.login.LoginViewModel
import com.example.museodelosmuseos.ui.screens.login.PantallaLogin
import com.example.museodelosmuseos.ui.screens.mapa.MapaViewModel
import com.example.museodelosmuseos.ui.screens.mapa.PantallaMapa
import com.example.museodelosmuseos.ui.screens.registro.PantallaRegistro
import com.example.museodelosmuseos.ui.screens.registro.RegistroViewModel
import com.example.museodelosmuseos.ui.screens.resultadoqr.PantallaResultadoQr
import com.example.museodelosmuseos.ui.screens.resultadoqr.ResultadoQrViewModel

@Composable
fun MuseoApp() {
    val context = LocalContext.current

    val database = remember { AppDatabase.obtenerInstancia(context) }
    val repository = remember { MuseoRepository(database) }
    val registroViewModel = remember(repository) { RegistroViewModel(repository) }
    val loginViewModel = remember(repository) { LoginViewModel(repository) }
    val dashboardViewModel = remember(repository) { DashboardViewModel(repository) }
    val mapaViewModel = remember(repository) { MapaViewModel(repository) }
    val escanerViewModel = remember(repository) { EscanerQrViewModel(repository) }

    var destinoActual by remember { mutableStateOf<DestinoPantalla>(DestinoPantalla.Bienvenida) }

    val volverABienvenida = { destinoActual = DestinoPantalla.Bienvenida }

    BackHandler(enabled = destinoActual != DestinoPantalla.Bienvenida) {
        when (destinoActual) {
            is DestinoPantalla.ResultadoQr -> destinoActual = DestinoPantalla.EscanerQr
            is DestinoPantalla.Mapa,
            is DestinoPantalla.DetalleMuseo,
            is DestinoPantalla.EscanerQr -> destinoActual = DestinoPantalla.Dashboard
            else -> volverABienvenida()
        }
    }

    when (val destino = destinoActual) {
        is DestinoPantalla.Bienvenida -> {
            PantallaBienvenida(
                onComenzarClick = {

                    destinoActual = DestinoPantalla.Registro
                },
                onIniciarSesionClick = {

                    destinoActual = DestinoPantalla.Login
                },
                onExplorarMapaClick = {

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
            PantallaDashboard(
                viewModel = dashboardViewModel,
                onNavegarAMapa = {
                    destinoActual = DestinoPantalla.Mapa
                },
                onNavegarADetalle = { museoId ->
                    destinoActual = DestinoPantalla.DetalleMuseo(museoId)
                },
                onNavegarAEscanerQr = {
                    destinoActual = DestinoPantalla.EscanerQr
                },
                onCerrarSesion = volverABienvenida
            )
        }

        is DestinoPantalla.Mapa -> {
            PantallaMapa(
                viewModel = mapaViewModel,
                onVolver = {
                    destinoActual = DestinoPantalla.Dashboard
                },
                onNavegarADetalle = { museoId ->
                    destinoActual = DestinoPantalla.DetalleMuseo(museoId)
                }
            )
        }

        is DestinoPantalla.DetalleMuseo -> {
            val detalleViewModel = remember(destino.museoId) {
                DetalleMuseoViewModel(repository, destino.museoId)
            }
            PantallaDetalleMuseo(
                viewModel = detalleViewModel,
                onVolver = {
                    destinoActual = DestinoPantalla.Dashboard
                }
            )
        }

        is DestinoPantalla.EscanerQr -> {
            PantallaEscanerQr(
                viewModel = escanerViewModel,
                onVolver = {
                    destinoActual = DestinoPantalla.Dashboard
                },
                onNavegarAResultado = { resultado ->
                    destinoActual = resultado
                }
            )
        }

        is DestinoPantalla.ResultadoQr -> {
            val resultadoViewModel = remember(destino) {
                ResultadoQrViewModel(repository, destino)
            }
            PantallaResultadoQr(
                viewModel = resultadoViewModel,
                onVolverAEscanear = {
                    destinoActual = DestinoPantalla.EscanerQr
                },
                onNavegarAFicha = { museoId ->
                    destinoActual = DestinoPantalla.DetalleMuseo(museoId)
                }
            )
        }
    }
}
