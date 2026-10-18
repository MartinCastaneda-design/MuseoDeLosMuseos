package com.example.museodelosmuseos.ui.screens.escaner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.data.local.entity.HistorialQr
import com.example.museodelosmuseos.ui.navigation.DestinoPantalla
import com.example.museodelosmuseos.ui.theme.AzulCard
import com.example.museodelosmuseos.ui.theme.AzulMuseoMedio
import com.example.museodelosmuseos.ui.theme.AzulMuseoProfundo
import com.example.museodelosmuseos.ui.theme.DoradoBrillante
import com.example.museodelosmuseos.ui.theme.DoradoPatrimonial
import com.example.museodelosmuseos.ui.theme.FondoOscuro
import com.example.museodelosmuseos.ui.theme.TextoApagado
import com.example.museodelosmuseos.ui.theme.TextoPrimario
import com.example.museodelosmuseos.ui.theme.TextoSecundario
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ============================================================================
 * PANTALLA 7: ESCÁNER QR
 * ============================================================================
 * Cumple con los requerimientos de la pauta:
 * - Roles: Investigador y Cliente.
 * - Activa la cámara para leer códigos QR asociados a museos/exposiciones.
 * - Implementa Google Code Scanner (ML Kit) según la lógica del profesor.
 * - Valida el código y determina qué ficha debe abrir.
 * - El Investigador puede probar los códigos de los registros que administra.
 * - Las lecturas correctas se agregan al historial local en SQLite (Room)
 *   evitando registros duplicados innecesarios.
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEscanerQr(
    viewModel: EscanerQrViewModel,
    onVolver: () -> Unit,
    onNavegarAResultado: (DestinoPantalla.ResultadoQr) -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val rol = estado.sesion?.rol?.uppercase() ?: "CLIENTE"

    var codigoManual by remember { mutableStateOf("") }

    LaunchedEffect(estado.mensajeError) {
        estado.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    // Configuración oficial del escáner según el ejemplo del profesor
    val scannerOptions = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
    }
    val scanner = remember { GmsBarcodeScanning.getClient(context, scannerOptions) }

    Scaffold(
        containerColor = FondoOscuro,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Escáner QR Patrimonial",
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Tarjeta Principal de Escaneo con Cámara
                item {
                    TarjetaMarcoEscaner(
                        procesando = estado.procesando,
                        onEscanearClick = {
                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    val valor = barcode.rawValue
                                    if (!valor.isNullOrBlank()) {
                                        viewModel.procesarCodigoQr(valor, onNavegarAResultado)
                                    }
                                }
                                .addOnFailureListener {
                                    // Manejo de cancelación o error de escáner
                                }
                        }
                    )
                }

                // 2. Sección para el INVESTIGADOR: Probar códigos de registros administrados
                if (rol == "INVESTIGADOR" || rol == "ADMINISTRADOR") {
                    item {
                        SeccionPruebaInvestigador(
                            codigosDisponibles = estado.codigosParaPrueba,
                            codigoManual = codigoManual,
                            onCodigoManualCambia = { codigoManual = it },
                            onProbarCodigo = { codigo ->
                                viewModel.procesarCodigoQr(codigo, onNavegarAResultado)
                            }
                        )
                    }
                }

                // 3. Historial Local en SQLite (Room)
                item {
                    Text(
                        text = "Historial de Lecturas QR (SQLite)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = DoradoPatrimonial
                    )
                    Text(
                        text = "Registros únicos guardados localmente sin duplicados",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (estado.historialLecturas.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AzulCard)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.History, contentDescription = null, tint = TextoApagado, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aún no has registrado lecturas QR.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoSecundario,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(estado.historialLecturas) { itemHistorial ->
                        TarjetaItemHistorial(
                            item = itemHistorial,
                            onClick = {
                                viewModel.procesarCodigoQr(itemHistorial.codigo, onNavegarAResultado)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 1. Tarjeta visual del escáner con botón para activar Google Code Scanner.
 */
@Composable
private fun TarjetaMarcoEscaner(
    procesando: Boolean,
    onEscanearClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(DoradoBrillante, DoradoPatrimonial))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AzulMuseoMedio)
                    .border(2.dp, DoradoPatrimonial, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "Escáner QR",
                    tint = DoradoBrillante,
                    modifier = Modifier.size(64.dp)
                )
            }

            Text(
                text = "Lector de Códigos QR Patrimoniales",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextoPrimario,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Apunta con la cámara al código QR de un museo, sala o catálogo para validar y abrir su ficha interactiva.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onEscanearClick,
                enabled = !procesando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DoradoPatrimonial,
                    contentColor = FondoOscuro
                )
            ) {
                if (procesando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = FondoOscuro,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Activar Cámara para Escanear",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

/**
 * 2. Sección exclusiva para el Investigador: Probar códigos de registros administrados.
 */
@Composable
private fun SeccionPruebaInvestigador(
    codigosDisponibles: List<String>,
    codigoManual: String,
    onCodigoManualCambia: (String) -> Unit,
    onProbarCodigo: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(DoradoPatrimonial.copy(alpha = 0.5f), AzulMuseoMedio))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AzulMuseoMedio),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.School, contentDescription = null, tint = DoradoBrillante, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Modo Investigador: Pruebas de Registro",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = DoradoBrillante
                    )
                    Text(
                        text = "Prueba códigos QR de registros administrados",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario
                    )
                }
            }

            // Chips rápidos con códigos registrados en Room
            Text(
                text = "Códigos de prueba rápidos:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextoPrimario
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(codigosDisponibles) { codigo ->
                    FilterChip(
                        selected = false,
                        onClick = { onProbarCodigo(codigo) },
                        label = { Text(codigo, fontSize = 10.sp) },
                        leadingIcon = {
                            Icon(Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = AzulMuseoMedio,
                            labelColor = DoradoBrillante
                        )
                    )
                }
            }

            // Entrada manual para validar cualquier código
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = codigoManual,
                    onValueChange = onCodigoManualCambia,
                    label = { Text("Escribir código manual") },
                    placeholder = { Text("Ej: MUSEO_PRADO_MADRID") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DoradoPatrimonial,
                        unfocusedBorderColor = DoradoPatrimonial.copy(alpha = 0.4f),
                        focusedTextColor = TextoPrimario,
                        unfocusedTextColor = TextoPrimario
                    )
                )

                Button(
                    onClick = {
                        if (codigoManual.isNotBlank()) {
                            onProbarCodigo(codigoManual)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DoradoPatrimonial,
                        contentColor = FondoOscuro
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Validar")
                }
            }
        }
    }
}

/**
 * 3. Tarjeta de ítem del historial en SQLite.
 */
@Composable
private fun TarjetaItemHistorial(
    item: HistorialQr,
    onClick: () -> Unit
) {
    val fechaFormateada = remember(item.fechaTimestamp) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(item.fechaTimestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AzulCard)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AzulMuseoMedio),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.QrCode, contentDescription = null, tint = DoradoBrillante, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextoPrimario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.codigo,
                    style = MaterialTheme.typography.labelSmall,
                    color = DoradoPatrimonial
                )
                Text(
                    text = "Leído: $fechaFormateada",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoApagado,
                    fontSize = 10.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextoApagado,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
