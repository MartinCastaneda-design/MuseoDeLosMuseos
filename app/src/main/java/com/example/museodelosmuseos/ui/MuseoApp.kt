package com.example.museodelosmuseos.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.museodelosmuseos.ui.screens.detallearticulo.DetalleArticuloViewModel
import com.example.museodelosmuseos.ui.screens.detallearticulo.PantallaDetalleArticulo
import com.example.museodelosmuseos.ui.screens.carrito.CarritoViewModel
import com.example.museodelosmuseos.ui.screens.carrito.PantallaCarrito
import com.example.museodelosmuseos.ui.screens.solicitudes.SolicitudesViewModel
import com.example.museodelosmuseos.ui.screens.solicitudes.PantallaSolicitudes
import com.example.museodelosmuseos.ui.screens.favoritos.FavoritosViewModel
import com.example.museodelosmuseos.ui.screens.favoritos.PantallaFavoritos
import com.example.museodelosmuseos.ui.screens.avisos.AvisosViewModel
import com.example.museodelosmuseos.ui.screens.avisos.PantallaAvisos
import com.example.museodelosmuseos.ui.screens.perfil.PerfilViewModel
import com.example.museodelosmuseos.ui.screens.perfil.PantallaPerfil
import com.example.museodelosmuseos.data.local.ArchivosAvatar
import com.example.museodelosmuseos.data.model.DestinoAviso
import com.example.museodelosmuseos.data.model.ReglasAvisos
import com.example.museodelosmuseos.ui.screens.catalogo.CatalogoViewModel
import com.example.museodelosmuseos.ui.screens.catalogo.PantallaCatalogo
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
    val dashboardViewModel = remember(repository) { DashboardViewModel(repository) }
    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val catalogoViewModel = remember(repository) { CatalogoViewModel(repository) }
    val mapaViewModel = remember(repository) { MapaViewModel(repository) }
    val escanerViewModel = remember(repository) { EscanerQrViewModel(repository) }

    // Estado reactivo de navegación (Single Source of Truth para la vista activa)
    var destinoActual by remember { mutableStateOf<DestinoPantalla>(DestinoPantalla.Bienvenida) }
    var origenDesdeAvisos by remember { mutableStateOf<DestinoPantalla?>(null) }
    var entradaPerfil by remember { mutableStateOf(0) }
    fun volverDesdeAvisos(): Boolean {
        if (destinoActual != origenDesdeAvisos || origenDesdeAvisos == null) return false
        origenDesdeAvisos = null
        destinoActual = DestinoPantalla.Avisos
        return true
    }
    LaunchedEffect(destinoActual) {
        if (origenDesdeAvisos != null && destinoActual != origenDesdeAvisos) origenDesdeAvisos = null
    }

    // Pila simple de retorno hacia atrás
    val volverABienvenida = { destinoActual = DestinoPantalla.Bienvenida }

    val esCliente = dashboardState.sesion?.rol?.equals("CLIENTE", ignoreCase = true) == true
    LaunchedEffect(destinoActual, dashboardState.puedeAccederCatalogo, esCliente) {
        if (destinoActual is DestinoPantalla.Favoritos && !esCliente) destinoActual = DestinoPantalla.Dashboard
        if (destinoActual is DestinoPantalla.Solicitudes && !dashboardState.puedeAccederCatalogo) {
            destinoActual = DestinoPantalla.Dashboard
        }
        if (destinoActual == DestinoPantalla.Catalogo && !dashboardState.puedeAccederCatalogo) {
            destinoActual = DestinoPantalla.Dashboard
        }
        if ((destinoActual is DestinoPantalla.DetalleArticulo || destinoActual is DestinoPantalla.Carrito) && !esCliente) {
            destinoActual = if (dashboardState.puedeAccederCatalogo) DestinoPantalla.Catalogo else DestinoPantalla.Dashboard
        }
    }

    LaunchedEffect(destinoActual, dashboardState.sesion) {
        if (destinoActual == DestinoPantalla.Perfil && dashboardState.sesion == null) {
            origenDesdeAvisos = null
            loginViewModel.limpiarAlCerrarSesion()
            destinoActual = DestinoPantalla.Login
        }
        if (destinoActual == DestinoPantalla.Avisos) {
            val sesion = dashboardState.sesion
            if (sesion == null || ReglasAvisos.acceso(sesion, sesion.usuarioId) != null) destinoActual = DestinoPantalla.Dashboard
        }
    }

    BackHandler(enabled = destinoActual != DestinoPantalla.Bienvenida) {
        if (!volverDesdeAvisos()) {
        when (val actual = destinoActual) {
            is DestinoPantalla.Perfil -> destinoActual = DestinoPantalla.Dashboard
            is DestinoPantalla.Avisos -> destinoActual = DestinoPantalla.Dashboard
            is DestinoPantalla.Favoritos -> destinoActual = actual.origenMuseoId?.let { DestinoPantalla.DetalleMuseo(it) } ?: DestinoPantalla.Dashboard
            is DestinoPantalla.DetalleMuseo -> destinoActual = if (esCliente) actual.origenFavoritos ?: DestinoPantalla.Dashboard else DestinoPantalla.Dashboard
            is DestinoPantalla.Solicitudes -> destinoActual = if (esCliente) actual.origenCarrito ?: DestinoPantalla.Dashboard else DestinoPantalla.Dashboard
            is DestinoPantalla.Carrito -> destinoActual = actual.origenArticuloId?.let { DestinoPantalla.DetalleArticulo(it) } ?: DestinoPantalla.Catalogo
            is DestinoPantalla.DetalleArticulo -> destinoActual = DestinoPantalla.Catalogo
            is DestinoPantalla.ResultadoQr -> destinoActual = DestinoPantalla.EscanerQr
            is DestinoPantalla.Mapa,
            is DestinoPantalla.Catalogo,
            is DestinoPantalla.EscanerQr -> destinoActual = DestinoPantalla.Dashboard
            else -> volverABienvenida()
        }
        }
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
                onCerrarSesion = volverABienvenida,
                onNavegarAPerfil = { entradaPerfil++; destinoActual = DestinoPantalla.Perfil },
                onNavegarAAvisos = { destinoActual = DestinoPantalla.Avisos },
                onNavegarAFavoritos = {
                    if (dashboardViewModel.uiState.value.sesion?.rol?.equals("CLIENTE", true) == true) destinoActual = DestinoPantalla.Favoritos()
                },
                onNavegarASolicitudes = {
                    if (dashboardViewModel.uiState.value.puedeAccederCatalogo) destinoActual = DestinoPantalla.Solicitudes()
                },
                onNavegarACatalogo = {
                    if (dashboardViewModel.uiState.value.puedeAccederCatalogo) {
                        destinoActual = DestinoPantalla.Catalogo
                    }
                }
            )
        }

        is DestinoPantalla.Catalogo -> {
            if (dashboardState.puedeAccederCatalogo) {
                PantallaCatalogo(
                    viewModel = catalogoViewModel,
                    onVolver = { if (!volverDesdeAvisos()) destinoActual = DestinoPantalla.Dashboard },
                    onNavegarACarrito = {
                        if (esCliente) destinoActual = DestinoPantalla.Carrito()
                    },
                    onNavegarADetalleArticulo = { articuloId ->
                        if (dashboardViewModel.uiState.value.sesion?.rol?.equals("CLIENTE", ignoreCase = true) == true) {
                            destinoActual = DestinoPantalla.DetalleArticulo(articuloId)
                        }
                    }
                )
            }
        }

        is DestinoPantalla.DetalleArticulo -> {
            val usuarioId = dashboardState.sesion?.usuarioId
            if (esCliente && usuarioId != null) {
                val detalleArticuloViewModel: DetalleArticuloViewModel = viewModel(
                    key = "detalle-articulo-${destino.articuloId}-$usuarioId",
                    factory = viewModelFactory {
                        initializer { DetalleArticuloViewModel(repository, destino.articuloId, usuarioId) }
                    }
                )
                PantallaDetalleArticulo(
                    viewModel = detalleArticuloViewModel,
                    onVolver = { destinoActual = DestinoPantalla.Catalogo },
                    onNavegarACarrito = { destinoActual = DestinoPantalla.Carrito(destino.articuloId) }
                )
            }
        }

        is DestinoPantalla.Carrito -> {
            val usuarioId = dashboardState.sesion?.usuarioId
            if (esCliente && usuarioId != null) {
                val carritoViewModel: CarritoViewModel = viewModel(key = "carrito-$usuarioId",
                    factory = viewModelFactory { initializer { CarritoViewModel(repository, usuarioId) } })
                PantallaCarrito(carritoViewModel, onNavegarASolicitudes = { destinoActual = DestinoPantalla.Solicitudes(destino) }, onVolver = {
                    destinoActual = destino.origenArticuloId?.let { DestinoPantalla.DetalleArticulo(it) } ?: DestinoPantalla.Catalogo
                })
            }
        }

        is DestinoPantalla.Solicitudes -> {
            val actorId = dashboardState.sesion?.usuarioId
            if (dashboardState.puedeAccederCatalogo && actorId != null) {
                val solicitudesViewModel: SolicitudesViewModel = viewModel(key = "solicitudes-$actorId-${destino.solicitudInicial}",
                    factory = viewModelFactory { initializer { SolicitudesViewModel(repository, actorId, destino.solicitudInicial) } })
                PantallaSolicitudes(solicitudesViewModel, onVolver = {
                    if (!volverDesdeAvisos()) destinoActual = if (esCliente) destino.origenCarrito ?: DestinoPantalla.Dashboard else DestinoPantalla.Dashboard
                })
            }
        }

        is DestinoPantalla.Favoritos -> {
            val usuarioId = dashboardState.sesion?.usuarioId
            if (esCliente && usuarioId != null) {
                val favoritosViewModel: FavoritosViewModel = viewModel(key = "favoritos-$usuarioId",
                    factory = viewModelFactory { initializer { FavoritosViewModel(repository, usuarioId) } })
                PantallaFavoritos(favoritosViewModel,
                    onVolver = { destinoActual = destino.origenMuseoId?.let { DestinoPantalla.DetalleMuseo(it) } ?: DestinoPantalla.Dashboard },
                    onAbrirMuseo = { destinoActual = DestinoPantalla.DetalleMuseo(it, destino) },
                    onExplorar = { destinoActual = DestinoPantalla.Mapa })
            }
        }

        is DestinoPantalla.Perfil -> {
            val sesion = dashboardState.sesion
            if (sesion != null && com.example.museodelosmuseos.data.model.ReglasPerfil.acceso(sesion, sesion.usuarioId) == null) {
                val archivos = remember { ArchivosAvatar(context) }
                val perfilViewModel: PerfilViewModel = viewModel(key = "perfil-${sesion.usuarioId}-$entradaPerfil",
                    factory = viewModelFactory { initializer { PerfilViewModel(repository, sesion.usuarioId, archivos) } })
                PantallaPerfil(perfilViewModel, archivos, onVolver = { destinoActual = DestinoPantalla.Dashboard },
                    onSalir = { loginViewModel.limpiarAlCerrarSesion(); origenDesdeAvisos = null; destinoActual = DestinoPantalla.Login })
            }
        }

        is DestinoPantalla.Avisos -> {
            val sesion = dashboardState.sesion
            if (sesion != null && ReglasAvisos.acceso(sesion, sesion.usuarioId) == null) {
                val avisosViewModel: AvisosViewModel = viewModel(key = "avisos-${sesion.usuarioId}",
                    factory = viewModelFactory { initializer { AvisosViewModel(repository, sesion.usuarioId) } })
                PantallaAvisos(avisosViewModel, onVolver = { destinoActual = DestinoPantalla.Dashboard }, onAbrirContenido = { enlace ->
                    val actual = dashboardViewModel.uiState.value.sesion
                    if (actual?.usuarioId == sesion.usuarioId && ReglasAvisos.puedeAbrir(actual.rol, enlace.destino)) {
                        val destinoEnlace = when (enlace.destino) {
                            DestinoAviso.MUSEO -> enlace.museoId?.let { DestinoPantalla.DetalleMuseo(it) }
                            DestinoAviso.CATALOGO -> DestinoPantalla.Catalogo
                            DestinoAviso.SOLICITUDES -> DestinoPantalla.Solicitudes(solicitudInicial = enlace.solicitudClave)
                            DestinoAviso.SIN_ENLACE -> null
                        }
                        if (destinoEnlace != null) { origenDesdeAvisos = destinoEnlace; destinoActual = destinoEnlace }
                    }
                })
            }
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
                onAbrirFavoritos = {
                    if (esCliente) destinoActual = destino.origenFavoritos ?: DestinoPantalla.Favoritos(destino.museoId)
                },
                onVolver = {
                    if (!volverDesdeAvisos()) destinoActual = if (esCliente) destino.origenFavoritos ?: DestinoPantalla.Dashboard else DestinoPantalla.Dashboard
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
