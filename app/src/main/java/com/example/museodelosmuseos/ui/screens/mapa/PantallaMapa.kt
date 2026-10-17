package com.example.museodelosmuseos.ui.screens.mapa

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.ui.theme.AzulCard
import com.example.museodelosmuseos.ui.theme.AzulMuseoMedio
import com.example.museodelosmuseos.ui.theme.AzulMuseoProfundo
import com.example.museodelosmuseos.ui.theme.DoradoBrillante
import com.example.museodelosmuseos.ui.theme.DoradoPatrimonial
import com.example.museodelosmuseos.ui.theme.FondoOscuro
import com.example.museodelosmuseos.ui.theme.TextoApagado
import com.example.museodelosmuseos.ui.theme.TextoPrimario
import com.example.museodelosmuseos.ui.theme.TextoSecundario
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// Servidor de mapas abierto de Wikimedia (OpenStreetMap) libre de bloqueos 403 y sin necesidad de API Key
private val TILE_SOURCE_WIKIMEDIA = XYTileSource(
    "Wikimedia",
    1, 19, 256, ".png",
    arrayOf("https://maps.wikimedia.org/osm-intl/")
)

/**
 * ============================================================================
 * PANTALLA 5: MAPA DE MUSEOS VIRTUALES
 * ============================================================================
 * Cumple con los requerimientos de la pauta:
 * - Roles: Administrador, Investigador y Cliente.
 * - Muestra un mapa interactivo de Iberoamérica con marcadores de museos.
 * - Consulta datos de una API REST ficticia con caché en SQLite (Room).
 * - Filtra por país o proyecto/clasificación.
 * - Al seleccionar un marcador presenta información básica y acceso al detalle.
 * - Diferencia claramente la ubicación geográfica real del museo del enlace
 *   a su visita virtual.
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(
    viewModel: MapaViewModel,
    onVolver: () -> Unit,
    onNavegarADetalle: (Long) -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar feedback de sincronización con la API REST ficticia
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        containerColor = FondoOscuro,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mapa de Museos Virtuales",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DoradoPatrimonial
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = DoradoPatrimonial
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.sincronizarConApiFicticia() },
                        enabled = !estado.sincronizando
                    ) {
                        if (estado.sincronizando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = DoradoBrillante,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Sincronizar API REST",
                                tint = DoradoPatrimonial
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulMuseoProfundo
                )
            )
        }
    ) { paddingValores ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValores)
        ) {
            // ---- MAPA INTERACTIVO OSMDROID ----
            MapaOsmDroid(
                museos = estado.museosFiltrados,
                museoSeleccionado = estado.museoSeleccionado,
                onSeleccionarMuseo = { viewModel.seleccionarMuseo(it) }
            )

            // ---- BARRA SUPERIOR DE FILTROS (PAÍS Y PROYECTO) ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                AzulMuseoProfundo.copy(alpha = 0.95f),
                                AzulMuseoProfundo.copy(alpha = 0.8f),
                                FondoOscuro.copy(alpha = 0.0f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Filtro 1: Países
                Text(
                    text = "Filtrar por País:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = DoradoPatrimonial
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    estado.listaPaises.forEach { pais ->
                        val seleccionado = estado.paisSeleccionado == pais
                        FilterChip(
                            selected = seleccionado,
                            onClick = { viewModel.filtrarPorPais(pais) },
                            label = { Text(pais, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = AzulCard,
                                labelColor = TextoPrimario,
                                selectedContainerColor = DoradoPatrimonial,
                                selectedLabelColor = FondoOscuro
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Filtro 2: Proyecto o Clasificación
                Text(
                    text = "Filtrar por Clasificación / Proyecto:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = DoradoPatrimonial
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    estado.listaProyectos.forEach { proyecto ->
                        val seleccionado = estado.proyectoSeleccionado == proyecto
                        FilterChip(
                            selected = seleccionado,
                            onClick = { viewModel.filtrarPorProyecto(proyecto) },
                            label = { Text(proyecto, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = AzulCard,
                                labelColor = TextoPrimario,
                                selectedContainerColor = AzulMuseoMedio,
                                selectedLabelColor = DoradoBrillante
                            )
                        )
                    }
                }
            }

            // ---- TARJETA INFERIOR FLOTANTE AL SELECCIONAR UN MARCADOR ----
            AnimatedVisibility(
                visible = estado.museoSeleccionado != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                estado.museoSeleccionado?.let { museo ->
                    TarjetaDetalleMarcador(
                        museo = museo,
                        onCerrar = { viewModel.seleccionarMuseo(null) },
                        onVisitaVirtual = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(museo.urlVisitaVirtual))
                            context.startActivity(intent)
                        },
                        onVerDetalle = { onNavegarADetalle(museo.id) }
                    )
                }
            }
        }
    }
}

/**
 * Componente OSMDroid incrustado mediante AndroidView.
 */
@Composable
private fun MapaOsmDroid(
    museos: List<Museo>,
    museoSeleccionado: Museo?,
    onSeleccionarMuseo: (Museo) -> Unit
) {
    val context = LocalContext.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TILE_SOURCE_WIKIMEDIA)
            setMultiTouchControls(true)
            controller.setZoom(3.5)
            // Centrado inicial en el océano Atlántico entre Iberoamérica y España
            controller.setCenter(GeoPoint(12.0, -45.0))
        }
    }

    // Gestionar pausa y reanudación del MapView con el ciclo de vida de Compose
    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
        }
    }

    // Actualizar marcadores reactivamente cuando cambie la lista filtrada
    LaunchedEffect(museos) {
        mapView.overlays.clear()

        museos.forEach { museo ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(museo.latitud, museo.longitud)
                title = museo.nombre
                snippet = "${museo.ciudad}, ${museo.pais}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    onSeleccionarMuseo(museo)
                    mapView.controller.animateTo(GeoPoint(museo.latitud, museo.longitud), 7.0, 1000L)
                    true
                }
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    // Si hay un museo seleccionado externamente, centrar el mapa
    LaunchedEffect(museoSeleccionado) {
        museoSeleccionado?.let {
            mapView.controller.animateTo(GeoPoint(it.latitud, it.longitud), 8.0, 800L)
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Tarjeta interactiva al tocar un marcador.
 * Satisface el requerimiento:
 * "Al seleccionar un marcador presenta información básica y acceso al detalle.
 * Debe diferenciar la ubicación geográfica del museo de su enlace a la visita virtual"
 */
@Composable
private fun TarjetaDetalleMarcador(
    museo: Museo,
    onCerrar: () -> Unit,
    onVisitaVirtual: () -> Unit,
    onVerDetalle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(DoradoBrillante, DoradoPatrimonial))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Encabezado con nombre y botón cerrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AzulMuseoMedio),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = DoradoBrillante,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = museo.nombre,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextoPrimario,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${museo.ciudad}, ${museo.pais} • ${museo.clasificacion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DoradoPatrimonial
                    )
                }

                IconButton(onClick = onCerrar) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = TextoApagado)
                }
            }

            // ---- DIFERENCIACIÓN EXPLICITA DE REQUERIMIENTO ----
            // 1. Ubicación Geográfica Física
            Surface(
                color = AzulMuseoProfundo,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = DoradoBrillante, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Ubicación Geográfica Física:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextoSecundario
                        )
                        Text(
                            text = "Lat: %.4f • Lon: %.4f (%s)".format(museo.latitud, museo.longitud, museo.ciudad),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoPrimario
                        )
                    }
                }
            }

            // 2. Enlace a la Visita Virtual
            Surface(
                color = AzulMuseoProfundo,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Language, contentDescription = null, tint = DoradoPatrimonial, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Recorrido Patrimonial Remoto:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextoSecundario
                        )
                        Text(
                            text = "Visita virtual 360° disponible en línea",
                            style = MaterialTheme.typography.bodySmall,
                            color = DoradoBrillante
                        )
                    }
                }
            }

            // Botones de acción: Visita Virtual y Acceso al Detalle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onVisitaVirtual,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DoradoBrillante),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = Brush.horizontalGradient(listOf(DoradoPatrimonial, DoradoBrillante))
                    )
                ) {
                    Icon(Icons.Filled.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recorrido Virtual", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onVerDetalle,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoPatrimonial, contentColor = FondoOscuro)
                ) {
                    Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Detalle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
