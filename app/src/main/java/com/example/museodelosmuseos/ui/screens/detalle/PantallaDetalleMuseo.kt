package com.example.museodelosmuseos.ui.screens.detalle

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.ui.components.ImagenMuseo
import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.ui.theme.AzulCard
import com.example.museodelosmuseos.ui.theme.AzulMuseoMedio
import com.example.museodelosmuseos.ui.theme.AzulMuseoProfundo
import com.example.museodelosmuseos.ui.theme.DoradoBrillante
import com.example.museodelosmuseos.ui.theme.DoradoPatrimonial
import com.example.museodelosmuseos.ui.theme.ErrorRojo
import com.example.museodelosmuseos.ui.theme.FondoOscuro
import com.example.museodelosmuseos.ui.theme.TextoApagado
import com.example.museodelosmuseos.ui.theme.TextoPrimario
import com.example.museodelosmuseos.ui.theme.TextoSecundario
import com.example.museodelosmuseos.ui.theme.VerdeExito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleMuseo(
    viewModel: DetalleMuseoViewModel,
    onVolver: () -> Unit,
    onAbrirFavoritos: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarVisor360 by remember { mutableStateOf(false) }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    val rol = estado.sesion?.rol?.uppercase() ?: "CLIENTE"

    Scaffold(
        containerColor = FondoOscuro,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = estado.museo?.nombre ?: "Detalle del Museo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DoradoPatrimonial,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                    if (rol == "CLIENTE") TextButton(onClick = onAbrirFavoritos) { Text("Mis favoritos", color = DoradoPatrimonial) }
                    // Si el usuario es Cliente, acceso directo al botón de favoritos
                    if (rol == "CLIENTE") {
                        IconButton(onClick = { viewModel.alternarFavorito() }) {
                            Icon(
                                imageVector = if (estado.esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (estado.esFavorito) ErrorRojo else DoradoPatrimonial
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
                .background(
                    Brush.verticalGradient(
                        listOf(AzulMuseoProfundo, FondoOscuro)
                    )
                )
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = DoradoBrillante
                )
            } else {
                estado.museo?.let { museo ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        BannerImagenReferencial(museo = museo)

                        TarjetaInformacionGeneral(museo = museo)

                        TarjetaRecorridoVirtual(
                            museo = museo,
                            onExplorarEnApp = {
                                mostrarVisor360 = true
                            },
                            onAbrirEnNavegador = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(museo.urlVisitaVirtual)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {

                                }
                            }
                        )

                        SeccionAccionesPorRol(
                            rol = rol,
                            esFavorito = estado.esFavorito,
                            fichaPublicada = estado.fichaPublicada,
                            onAlternarFavorito = { viewModel.alternarFavorito() },
                            onAbrirEdicionInvestigador = { viewModel.abrirDialogoEdicion() },
                            onAbrirRevisionAdmin = { viewModel.abrirDialogoRevision() }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    if (estado.mostrarDialogoEdicion) {
        DialogoEdicionInvestigador(
            descripcionActual = estado.descripcionEditada,
            notasActuales = estado.notasInvestigacion,
            onDescripcionCambia = viewModel::actualizarDescripcionEditada,
            onNotasCambia = viewModel::actualizarNotasInvestigacion,
            onGuardar = viewModel::guardarCorreccionFicha,
            onDescartar = viewModel::cerrarDialogoEdicion
        )
    }

    if (estado.mostrarDialogoRevision) {
        DialogoRevisionAdministrador(
            museo = estado.museo,
            onAprobar = viewModel::autorizarPublicacion,
            onCerrar = viewModel::cerrarDialogoRevision
        )
    }

    if (mostrarVisor360) {
        estado.museo?.let { museo ->
            VisorRecorridoVirtualDialog(
                url = museo.urlVisitaVirtual,
                nombreMuseo = museo.nombre,
                onCerrar = { mostrarVisor360 = false }
            )
        }
    }
}

@Composable
private fun BannerImagenReferencial(museo: Museo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(DoradoBrillante, DoradoPatrimonial))
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ImagenMuseo(museo, modifier = Modifier.fillMaxSize(), mostrarCreditosAlTocar = true)
            // Sombreado inferior para mantener la legibilidad del país.
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, FondoOscuro.copy(alpha = 0.85f)))
                )
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                color = FondoOscuro.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = DoradoBrillante, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${museo.ciudad}, ${museo.pais}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextoPrimario
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaInformacionGeneral(museo: Museo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = museo.nombre,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextoPrimario,
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                color = DoradoPatrimonial.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Clasificación: ${museo.clasificacion}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = DoradoBrillante,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "Descripción Patrimonial",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = DoradoPatrimonial
            )

            Text(
                text = museo.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun TarjetaRecorridoVirtual(
    museo: Museo,
    onExplorarEnApp: () -> Unit,
    onAbrirEnNavegador: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(DoradoPatrimonial, AzulMuseoMedio))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Language, contentDescription = null, tint = DoradoBrillante, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Recorrido Virtual Interactivo 360°",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextoPrimario
                    )
                    Text(
                        text = "Explora salas en 360°, obras y colecciones patrimoniales",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSecundario
                    )
                }
            }

            Button(
                onClick = onExplorarEnApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DoradoPatrimonial,
                    contentColor = FondoOscuro
                )
            ) {
                Icon(Icons.Filled.Language, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Explorar Recorrido 360° en la App",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            OutlinedButton(
                onClick = onAbrirEnNavegador,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DoradoBrillante),
                border = ButtonDefaults.outlinedButtonBorder().copy(
                    brush = Brush.horizontalGradient(listOf(DoradoPatrimonial, AzulMuseoMedio))
                )
            ) {
                Icon(Icons.Filled.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Abrir en Navegador Externo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SeccionAccionesPorRol(
    rol: String,
    esFavorito: Boolean,
    fichaPublicada: Boolean,
    onAlternarFavorito: () -> Unit,
    onAbrirEdicionInvestigador: () -> Unit,
    onAbrirRevisionAdmin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Acciones por Rol: $rol",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = DoradoPatrimonial
            )

            when (rol) {
                "CLIENTE" -> {

                    OutlinedButton(
                        onClick = onAlternarFavorito,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (esFavorito) ErrorRojo else DoradoBrillante
                        ),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (esFavorito) listOf(ErrorRojo, ErrorRojo) else listOf(DoradoPatrimonial, DoradoBrillante)
                            )
                        )
                    ) {
                        Icon(
                            imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (esFavorito) "Quitar de mis Favoritos" else "Guardar en mis Favoritos",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                "INVESTIGADOR" -> {

                    Button(
                        onClick = onAbrirEdicionInvestigador,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulMuseoMedio,
                            contentColor = TextoPrimario
                        )
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = DoradoBrillante)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Corregir / Completar Ficha Museológica", fontWeight = FontWeight.Bold)
                    }
                }

                "ADMINISTRADOR" -> {

                    Button(
                        onClick = onAbrirRevisionAdmin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (fichaPublicada) VerdeExito else DoradoPatrimonial,
                            contentColor = if (fichaPublicada) Color.White else FondoOscuro
                        )
                    ) {
                        Icon(
                            imageVector = if (fichaPublicada) Icons.Filled.Verified else Icons.Filled.AdminPanelSettings,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (fichaPublicada) "Ficha Aprobada (Revisar de nuevo)" else "Revisar y Publicar Ficha",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoEdicionInvestigador(
    descripcionActual: String,
    notasActuales: String,
    onDescripcionCambia: (String) -> Unit,
    onNotasCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onDescartar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDescartar,
        containerColor = AzulCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.School, contentDescription = null, tint = DoradoBrillante)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Corrección de Ficha Museológica", color = DoradoPatrimonial, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Como Investigador puedes actualizar la descripción histórica y registrar notas técnicas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
                OutlinedTextField(
                    value = descripcionActual,
                    onValueChange = onDescripcionCambia,
                    label = { Text("Descripción del Museo") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DoradoPatrimonial,
                        unfocusedBorderColor = DoradoPatrimonial.copy(alpha = 0.5f),
                        focusedTextColor = TextoPrimario,
                        unfocusedTextColor = TextoPrimario
                    )
                )
                OutlinedTextField(
                    value = notasActuales,
                    onValueChange = onNotasCambia,
                    label = { Text("Notas de Campo / Investigación") },
                    placeholder = { Text("Ej: Verificación física de sala de arte...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DoradoPatrimonial,
                        unfocusedBorderColor = DoradoPatrimonial.copy(alpha = 0.5f),
                        focusedTextColor = TextoPrimario,
                        unfocusedTextColor = TextoPrimario
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onGuardar,
                colors = ButtonDefaults.buttonColors(containerColor = DoradoPatrimonial, contentColor = FondoOscuro)
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDescartar) {
                Text("Cancelar", color = TextoApagado)
            }
        }
    )
}

@Composable
private fun DialogoRevisionAdministrador(
    museo: Museo?,
    onAprobar: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = AzulCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = DoradoBrillante)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Revisión Editorial de Ficha", color = DoradoPatrimonial, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Museo: ${museo?.nombre ?: ""}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextoPrimario
                )
                Text(
                    text = "País: ${museo?.pais} • Ciudad: ${museo?.ciudad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DoradoPatrimonial
                )
                Text(
                    text = "Estado editorial: Listo para autorizar en el catálogo público iberoamericano.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAprobar,
                colors = ButtonDefaults.buttonColors(containerColor = VerdeExito, contentColor = Color.White)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Aprobar y Publicar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cerrar", color = TextoApagado)
            }
        }
    )
}

@Composable
private fun VisorRecorridoVirtualDialog(
    url: String,
    nombreMuseo: String,
    onCerrar: () -> Unit
) {
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = FondoOscuro
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AzulMuseoProfundo)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = null,
                            tint = DoradoBrillante,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recorrido 360°: $nombreMuseo",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = DoradoPatrimonial,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onCerrar) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar Visor",
                            tint = TextoPrimario
                        )
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.databaseEnabled = true
                            settings.setSupportZoom(true)
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            webViewClient = WebViewClient()
                            loadUrl(url)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
