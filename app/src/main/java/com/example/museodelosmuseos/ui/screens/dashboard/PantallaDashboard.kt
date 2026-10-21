package com.example.museodelosmuseos.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.local.entity.SesionActiva
import com.example.museodelosmuseos.ui.theme.AzulCard
import com.example.museodelosmuseos.ui.theme.AzulMuseoMedio
import com.example.museodelosmuseos.ui.theme.AzulMuseoProfundo
import com.example.museodelosmuseos.ui.theme.DoradoBrillante
import com.example.museodelosmuseos.ui.theme.DoradoPatrimonial
import com.example.museodelosmuseos.ui.theme.FondoOscuro
import com.example.museodelosmuseos.ui.theme.TextoApagado
import com.example.museodelosmuseos.ui.theme.TextoPrimario
import com.example.museodelosmuseos.ui.theme.TextoSecundario
import com.example.museodelosmuseos.ui.theme.VerdeExito

/**
 * ============================================================================
 * PANTALLA 4: DASHBOARD / PANTALLA PRINCIPAL
 * ============================================================================
 * Cumple con los requerimientos de la pauta:
 * - Roles: Administrador, Investigador y Cliente.
 * - Resumen adaptado al rol activo.
 * - Para Cliente: museos destacados, cursos, productos y accesos rápidos.
 * - Para Administrador: solicitudes y novedades de gestión.
 * - Para Investigador: registros museológicos y herramientas.
 * - Incorpora tarjetas, pequeñas animaciones y una barra de navegación inferior
 *   hacia: Inicio, Mapa, Catálogo, Notificaciones y Perfil.
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDashboard(
    viewModel: DashboardViewModel,
    onNavegarAMapa: () -> Unit,
    onNavegarADetalle: (Long) -> Unit,
    onNavegarAEscanerQr: () -> Unit,
    onCerrarSesion: () -> Unit,
    onNavegarACatalogo: () -> Unit,
    onNavegarASolicitudes: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Museo de los Museos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DoradoPatrimonial
                        )
                        Text(
                            text = estado.sesion?.nombre ?: "Usuario",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSecundario,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AzulMuseoMedio)
                            .border(1.dp, DoradoPatrimonial, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (estado.sesion?.rol?.uppercase()) {
                                "ADMINISTRADOR" -> Icons.Filled.AdminPanelSettings
                                "INVESTIGADOR" -> Icons.Filled.School
                                else -> Icons.Filled.Person
                            },
                            contentDescription = "Avatar de Usuario",
                            tint = DoradoBrillante,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    if (estado.puedeAccederCatalogo) IconButton(onClick = onNavegarASolicitudes) {
                        Icon(Icons.Filled.History, "Historial de solicitudes", tint = DoradoPatrimonial)
                    }
                    IconButton(onClick = { viewModel.cerrarSesion(onCerrarSesion) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = DoradoPatrimonial
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulMuseoProfundo
                )
            )
        },
        bottomBar = {
            BarraNavegacionInferior(
                tabSeleccionada = estado.tabSeleccionada,
                puedeAccederCatalogo = estado.puedeAccederCatalogo,
                onTabSeleccionada = { tab ->
                    if (tab == TabDashboard.MAPA) {
                        onNavegarAMapa()
                    } else if (tab == TabDashboard.CATALOGO) {
                        if (estado.puedeAccederCatalogo) onNavegarACatalogo()
                    } else {
                        viewModel.seleccionarTab(tab)
                    }
                }
            )
        }
    ) { paddingValores ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
                .background(
                    Brush.verticalGradient(
                        listOf(AzulMuseoProfundo, FondoOscuro)
                    )
                )
        ) {
            AnimatedVisibility(
                visible = estado.animacionLista,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
            ) {
                when (estado.tabSeleccionada) {
                    TabDashboard.INICIO -> {
                        ContenidoInicioSegunRol(
                            sesion = estado.sesion,
                            museosDestacados = estado.museosDestacados,
                            onNavegarAMapa = onNavegarAMapa,
                            onNavegarADetalle = onNavegarADetalle,
                            onNavegarAEscanerQr = onNavegarAEscanerQr,
                            onVerCatalogo = {
                                if (estado.puedeAccederCatalogo) onNavegarACatalogo()
                            }
                        )
                    }
                    TabDashboard.MAPA -> {
                        // Navegación interceptada, pero en caso de seleccionarse:
                        ContenidoPlaceholder(
                            titulo = "Mapa Interactivo",
                            descripcion = "Explora los museos geolocalizados de Iberoamérica.",
                            botonTexto = "Abrir Mapa Completo",
                            onAccion = onNavegarAMapa
                        )
                    }
                    TabDashboard.CATALOGO -> {
                        ContenidoPlaceholder(
                            titulo = "Productos y cursos",
                            descripcion = "Consulta el catálogo cultural.",
                            botonTexto = "Abrir catálogo",
                            onAccion = {
                                if (estado.puedeAccederCatalogo) onNavegarACatalogo()
                            }
                        )
                    }
                    TabDashboard.NOTIFICACIONES -> {
                        ContenidoNotificaciones(sesion = estado.sesion)
                    }
                    TabDashboard.PERFIL -> {
                        ContenidoPerfil(
                            sesion = estado.sesion,
                            onCerrarSesion = { viewModel.cerrarSesion(onCerrarSesion) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Barra de navegación inferior con las 5 secciones requeridas:
 * Inicio, Mapa, Catálogo, Notificaciones y Perfil.
 */
@Composable
private fun BarraNavegacionInferior(
    tabSeleccionada: TabDashboard,
    puedeAccederCatalogo: Boolean,
    onTabSeleccionada: (TabDashboard) -> Unit
) {
    NavigationBar(
        containerColor = AzulMuseoProfundo,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(TabDashboard.INICIO, "Inicio", Icons.Filled.Home),
            Triple(TabDashboard.MAPA, "Mapa", Icons.Filled.Map),
            Triple(TabDashboard.CATALOGO, "Catálogo", Icons.Filled.Explore),
            Triple(TabDashboard.NOTIFICACIONES, "Avisos", Icons.Filled.Notifications),
            Triple(TabDashboard.PERFIL, "Perfil", Icons.Filled.Person)
        )

        items.filter { it.first != TabDashboard.CATALOGO || puedeAccederCatalogo }
            .forEach { (tab, etiqueta, icono) ->
            NavigationBarItem(
                selected = tabSeleccionada == tab,
                onClick = { onTabSeleccionada(tab) },
                icon = {
                    if (tab == TabDashboard.NOTIFICACIONES) {
                        BadgedBox(badge = { Badge { Text("2") } }) {
                            Icon(icono, contentDescription = etiqueta)
                        }
                    } else {
                        Icon(icono, contentDescription = etiqueta)
                    }
                },
                label = { Text(etiqueta, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FondoOscuro,
                    selectedTextColor = DoradoBrillante,
                    indicatorColor = DoradoPatrimonial,
                    unselectedIconColor = TextoSecundario,
                    unselectedTextColor = TextoSecundario
                )
            )
        }
    }
}

/**
 * Resumen adaptado al rol del usuario en la pestaña INICIO.
 */
@Composable
private fun ContenidoInicioSegunRol(
    sesion: SesionActiva?,
    museosDestacados: List<Museo>,
    onNavegarAMapa: () -> Unit,
    onNavegarADetalle: (Long) -> Unit,
    onNavegarAEscanerQr: () -> Unit,
    onVerCatalogo: () -> Unit
) {
    val rol = sesion?.rol?.uppercase() ?: "CLIENTE"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Banner de bienvenida con rol
        item {
            BannerBienvenidaRol(sesion = sesion)
        }

        when (rol) {
            "ADMINISTRADOR" -> {
                // ---- ROL ADMINISTRADOR ----
                item {
                    SeccionAdminMetricas()
                }
                item {
                    SeccionAdminSolicitudes()
                }
                item {
                    SeccionAdminNovedades()
                }
            }

            "INVESTIGADOR" -> {
                // ---- ROL INVESTIGADOR ----
                item {
                    SeccionInvestigadorAccesos(
                        onNavegarAEscanerQr = onNavegarAEscanerQr,
                        onNavegarAMapa = onNavegarAMapa
                    )
                }
                item {
                    SeccionInvestigadorFichas()
                }
                item {
                    SeccionInvestigadorHerramientas()
                }
            }

            else -> {
                // ---- ROL CLIENTE ----
                item {
                    SeccionAccesosRapidos(
                        onNavegarAMapa = onNavegarAMapa,
                        onNavegarAEscanerQr = onNavegarAEscanerQr,
                        onVerCatalogo = onVerCatalogo
                    )
                }

                item {
                    Text(
                        text = "Museos Destacados",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DoradoPatrimonial
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(museosDestacados) { museo ->
                            TarjetaMuseoDestacado(
                                museo = museo,
                                onClick = { onNavegarADetalle(museo.id) }
                            )
                        }
                    }
                }

                item {
                    SeccionCursosVirtuales()
                }

                item {
                    SeccionProductosTienda()
                }
            }
        }
    }
}

/**
 * Banner superior con saludo y rol activo.
 */
@Composable
private fun BannerBienvenidaRol(sesion: SesionActiva?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(DoradoBrillante, AzulMuseoMedio))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(DoradoPatrimonial),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = FondoOscuro,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "¡Hola, ${sesion?.nombre ?: "Visitante"}!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextoPrimario
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Surface(
                        color = DoradoPatrimonial.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ROL: ${sesion?.rol ?: "CLIENTE"}",
                            color = DoradoBrillante,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// COMPONENTES: CLIENTE (Destacados, Cursos, Productos, Accesos rápidos)
// ============================================================================

@Composable
private fun SeccionAccesosRapidos(
    onNavegarAMapa: () -> Unit,
    onNavegarAEscanerQr: () -> Unit,
    onVerCatalogo: () -> Unit
) {
    Column {
        Text(
            text = "Accesos Rápidos",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DoradoPatrimonial
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TarjetaAccesoBoton(
                titulo = "Mapa Virtual",
                icono = Icons.Filled.Map,
                modifier = Modifier.weight(1f),
                onClick = onNavegarAMapa
            )
            TarjetaAccesoBoton(
                titulo = "Escanear QR",
                icono = Icons.Filled.QrCodeScanner,
                modifier = Modifier.weight(1f),
                onClick = onNavegarAEscanerQr
            )
            TarjetaAccesoBoton(
                titulo = "Catálogo",
                icono = Icons.Filled.Explore,
                modifier = Modifier.weight(1f),
                onClick = onVerCatalogo
            )
        }
    }
}

@Composable
private fun TarjetaAccesoBoton(
    titulo: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AzulMuseoMedio),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = DoradoBrillante, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextoPrimario,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun TarjetaMuseoDestacado(museo: Museo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AzulMuseoMedio),
                contentAlignment = Alignment.Center
            ) {
                if (museo.imagenUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(museo.imagenUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = museo.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = DoradoPatrimonial,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = museo.nombre,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextoPrimario,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${museo.ciudad}, ${museo.pais}",
                style = MaterialTheme.typography.labelSmall,
                color = DoradoBrillante
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = museo.clasificacion,
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SeccionCursosVirtuales() {
    Column {
        Text(
            text = "Cursos Patrimoniales",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DoradoPatrimonial
        )
        Spacer(modifier = Modifier.height(8.dp))
        val cursos = listOf(
            Pair("Historia del Arte Colonial", "4 semanas • Modalidad Virtual"),
            Pair("Conservación del Patrimonio Vivo", "6 semanas • Certificado Duoc UC")
        )
        cursos.forEach { (nombre, detalle) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AzulCard)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.School, contentDescription = null, tint = DoradoBrillante, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(nombre, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextoPrimario)
                        Text(detalle, style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = DoradoPatrimonial, contentColor = FondoOscuro),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Inscribir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionProductosTienda() {
    Column {
        Text(
            text = "Tienda Cultural y Catálogo de Obras",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DoradoPatrimonial
        )
        Spacer(modifier = Modifier.height(8.dp))
        val productos = listOf(
            Triple("Lámina Réplica: Arte Colonial", "$14.990 CLP", Icons.Filled.ShoppingBag),
            Triple("Guía Oficial de Museos de Iberoamérica", "$9.990 CLP", Icons.Filled.ShoppingBag)
        )
        productos.forEach { (nombre, precio, icono) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AzulCard)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icono, contentDescription = null, tint = DoradoBrillante, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(nombre, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextoPrimario)
                        Text(precio, style = MaterialTheme.typography.labelSmall, color = DoradoBrillante)
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = AzulMuseoMedio, contentColor = TextoPrimario),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Comprar", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ============================================================================
// COMPONENTES: ADMINISTRADOR (Solicitudes y novedades de gestión)
// ============================================================================

@Composable
private fun SeccionAdminMetricas() {
    Column {
        Text(
            text = "Panel de Gestión del Sistema",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DoradoPatrimonial
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TarjetaMetrica(titulo = "Museos Activos", valor = "5", modifier = Modifier.weight(1f))
            TarjetaMetrica(titulo = "Solicitudes", valor = "3 Pendientes", modifier = Modifier.weight(1f))
            TarjetaMetrica(titulo = "Investigadores", valor = "12", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TarjetaMetrica(titulo: String, valor: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
            Spacer(modifier = Modifier.height(4.dp))
            Text(valor, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DoradoBrillante)
        }
    }
}

@Composable
private fun SeccionAdminSolicitudes() {
    Column {
        Text(
            text = "Solicitudes de Publicación de Fichas",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DoradoPatrimonial
        )
        Spacer(modifier = Modifier.height(8.dp))
        val solicitudes = listOf(
            Pair("Ficha: Museo de Arte Colonial (Actualización)", "Por: Dra. Elena Ramos"),
            Pair("Nueva ficha: Museo Precolombino", "Por: Lic. Roberto Díaz")
        )
        solicitudes.forEach { (titulo, autor) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AzulCard)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = DoradoBrillante)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(titulo, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextoPrimario)
                        Text(autor, style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeExito, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Revisar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionAdminNovedades() {
    Column {
        Text(
            text = "Novedades de Gestión Institucional",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DoradoPatrimonial
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AzulCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "• Sincronización exitosa con la base de datos Room y catálogo iberoamericano.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoPrimario
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• 148 lecturas de códigos QR registradas en el último ciclo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }
        }
    }
}

// ============================================================================
// COMPONENTES: INVESTIGADOR (Registros museológicos)
// ============================================================================

@Composable
private fun SeccionInvestigadorAccesos(
    onNavegarAEscanerQr: () -> Unit,
    onNavegarAMapa: () -> Unit
) {
    Column {
        Text(
            text = "Herramientas de Investigación",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DoradoPatrimonial
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TarjetaAccesoBoton(
                titulo = "Lector QR de Campo",
                icono = Icons.Filled.QrCodeScanner,
                modifier = Modifier.weight(1f),
                onClick = onNavegarAEscanerQr
            )
            TarjetaAccesoBoton(
                titulo = "Geolocalización",
                icono = Icons.Filled.Map,
                modifier = Modifier.weight(1f),
                onClick = onNavegarAMapa
            )
        }
    }
}

@Composable
private fun SeccionInvestigadorFichas() {
    Column {
        Text(
            text = "Mis Registros y Fichas Museológicas",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DoradoPatrimonial
        )
        Spacer(modifier = Modifier.height(8.dp))
        val fichas = listOf(
            Triple("Ficha #401: Museo Bellas Artes", "Estado: En revisión editorial", Icons.Filled.Description),
            Triple("Ficha #402: Museo Colonial Bogotá", "Estado: Aprobada y publicada", Icons.Filled.CheckCircle)
        )
        fichas.forEach { (titulo, estado, icono) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AzulCard)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icono, contentDescription = null, tint = DoradoBrillante)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(titulo, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextoPrimario)
                        Text(estado, style = MaterialTheme.typography.labelSmall, color = TextoSecundario)
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionInvestigadorHerramientas() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Registro de Nuevo Museo",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = DoradoPatrimonial
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Como investigador puedes ingresar fichas preliminares con coordenadas GPS y enlace de recorrido virtual.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario
            )
        }
    }
}

// ============================================================================
// OTRAS PESTAÑAS: CATÁLOGO, NOTIFICACIONES, PERFIL
// ============================================================================

@Composable
private fun ContenidoCatalogo(
    museos: List<Museo>,
    onNavegarADetalle: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Catálogo General de Museos Iberoamericanos",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DoradoPatrimonial
            )
            Text(
                text = "Base de datos patrimonial local sincronizada en Room",
                style = MaterialTheme.typography.labelSmall,
                color = TextoSecundario
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(museos) { museo ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavegarADetalle(museo.id) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AzulCard)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AzulMuseoMedio),
                        contentAlignment = Alignment.Center
                    ) {
                        if (museo.imagenUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(museo.imagenUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = museo.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = DoradoBrillante, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(museo.nombre, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextoPrimario)
                        Text("${museo.ciudad}, ${museo.pais}", style = MaterialTheme.typography.labelSmall, color = DoradoPatrimonial)
                        Text(museo.clasificacion, style = MaterialTheme.typography.bodySmall, color = TextoSecundario, maxLines = 1)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextoApagado)
                }
            }
        }
    }
}

@Composable
private fun ContenidoNotificaciones(sesion: SesionActiva?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Avisos y Notificaciones Culturales",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DoradoPatrimonial
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        val avisos = listOf(
            Triple("Nueva Visita Virtual Disponible", "Museo de Arte Moderno de México habilitó una nueva sala 360°.", "Hace 2 horas"),
            Triple("Recordatorio de Curso", "Tu clase de Historia del Arte Colonial comienza este lunes.", "Hace 1 día")
        )

        items(avisos) { (titulo, desc, tiempo) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AzulCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(titulo, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DoradoBrillante)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = TextoPrimario)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(tiempo, style = MaterialTheme.typography.labelSmall, color = TextoApagado)
                }
            }
        }
    }
}

@Composable
private fun ContenidoPerfil(
    sesion: SesionActiva?,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AzulMuseoMedio)
                .border(2.dp, DoradoPatrimonial, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = DoradoBrillante, modifier = Modifier.size(44.dp))
        }

        Text(
            text = sesion?.nombre ?: "Usuario",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = TextoPrimario
        )
        Text(
            text = sesion?.correo ?: "correo@ejemplo.com",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario
        )
        Surface(
            color = DoradoPatrimonial.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Rol activo: ${sesion?.rol ?: "CLIENTE"}",
                color = DoradoBrillante,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DoradoPatrimonial, contentColor = FondoOscuro),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ContenidoPlaceholder(
    titulo: String,
    descripcion: String,
    botonTexto: String,
    onAccion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(titulo, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = DoradoBrillante)
        Spacer(modifier = Modifier.height(8.dp))
        Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAccion,
            colors = ButtonDefaults.buttonColors(containerColor = DoradoPatrimonial, contentColor = FondoOscuro),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(botonTexto, fontWeight = FontWeight.Bold)
        }
    }
}
