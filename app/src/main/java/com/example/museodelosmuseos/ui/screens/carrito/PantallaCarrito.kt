package com.example.museodelosmuseos.ui.screens.carrito

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.museodelosmuseos.R
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCarrito(viewModel: CarritoViewModel, onVolver: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))
    Scaffold(containerColor = FondoOscuro, topBar = {
        CenterAlignedTopAppBar(title = { Text("Carrito y envío", color = DoradoPatrimonial) },
            navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = DoradoPatrimonial) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulMuseoProfundo))
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).imePadding(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                if (estado.cargando) CircularProgressIndicator()
                AnimatedVisibility(estado.confirmacion != null, enter = fadeIn() + slideInVertically { it / 2 }) {
                    Card(colors = CardDefaults.cardColors(containerColor = AzulCard)) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CheckCircle, "Solicitud registrada", tint = VerdeExito)
                            Text(estado.confirmacion.orEmpty(), color = VerdeExito)
                        }
                    }
                }
                AnimatedVisibility(estado.error != null) { Text(estado.error.orEmpty(), color = ErrorRojo) }
                if (!estado.cargando && estado.lineas.isEmpty()) Text("Tu carrito está vacío. Vuelve al catálogo para agregar productos.", color = TextoPrimario)
            }
            items(estado.lineas, key = { it.articulo.id }) { linea ->
                val articulo = linea.articulo
                val respaldo = painterResource(when (CategoriaArticulo.desdeTexto(articulo.categoria)) {
                    CategoriaArticulo.PUBLICACION -> R.drawable.catalogo_publicaciones
                    CategoriaArticulo.MATERIAL_CONSERVACION -> R.drawable.catalogo_conservacion
                    CategoriaArticulo.CURSO -> R.drawable.catalogo_cursos
                    else -> R.drawable.catalogo_articulos
                })
                Card(colors = CardDefaults.cardColors(containerColor = SuperficieCard), modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AsyncImage(model = articulo.imagenUrl.trim().takeIf { it.isNotEmpty() }, placeholder = respaldo,
                                error = respaldo, fallback = respaldo, contentDescription = articulo.titulo,
                                contentScale = ContentScale.Crop, modifier = Modifier.size(88.dp))
                            Column(Modifier.weight(1f)) {
                                Text(articulo.titulo, color = TextoPrimario, fontWeight = FontWeight.Bold)
                                Text("Unidad: ${moneda.format(articulo.precio)}", color = DoradoPatrimonial)
                                Text("Stock: ${articulo.stock}", color = TextoSecundario)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.cantidad(articulo.id, linea.cantidad - 1) }, enabled = !estado.guardando && linea.cantidad > 1) {
                                Icon(Icons.Filled.Remove, "Disminuir ${articulo.titulo}")
                            }
                            Text("${linea.cantidad}", color = TextoPrimario)
                            IconButton(onClick = { viewModel.cantidad(articulo.id, linea.cantidad + 1) },
                                enabled = !estado.guardando && linea.cantidad < articulo.stock && linea.error == null) {
                                Icon(Icons.Filled.Add, "Aumentar ${articulo.titulo}")
                            }
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { viewModel.eliminar(articulo.id) }, enabled = !estado.guardando) {
                                Icon(Icons.Filled.Delete, "Eliminar ${articulo.titulo}", tint = ErrorRojo)
                            }
                        }
                        Text("Subtotal: ${moneda.format(runCatching { linea.subtotal }.getOrDefault(0))}", color = DoradoBrillante)
                        linea.error?.let { Text(it, color = ErrorRojo) }
                    }
                }
            }
            if (estado.lineas.isNotEmpty()) item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Datos de envío", color = DoradoPatrimonial, style = MaterialTheme.typography.titleLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DestinoEnvio.entries.forEach { d -> FilterChip(selected = estado.destino == d,
                            onClick = { viewModel.destino(d) }, enabled = !estado.guardando, label = { Text(d.etiqueta) }) }
                    }
                    if (estado.intentoEnviar && estado.destino == null) Text("Selecciona un destino.", color = ErrorRojo)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ModalidadEnvio.entries.forEach { m -> FilterChip(selected = estado.modalidad == m,
                            onClick = { viewModel.modalidad(m) }, enabled = !estado.guardando, label = { Text(m.etiqueta) }) }
                    }
                    if (estado.intentoEnviar && estado.modalidad == null) Text("Selecciona una modalidad.", color = ErrorRojo)
                    OutlinedTextField(value = estado.ubicacion, onValueChange = viewModel::ubicacion,
                        label = { Text(if (estado.destino == DestinoEnvio.INTERNACIONAL) "País y ciudad" else "Ciudad o comuna") },
                        modifier = Modifier.fillMaxWidth(), enabled = !estado.guardando, isError = estado.errorUbicacion != null,
                        supportingText = { Text(estado.errorUbicacion ?: "Usa datos ficticios para la demostración.") },
                        trailingIcon = { if (estado.errorUbicacion != null) Icon(Icons.Filled.Error, "Revisa la ubicación") })
                    OutlinedTextField(value = estado.direccion, onValueChange = viewModel::direccion,
                        label = { Text("Dirección de entrega") }, modifier = Modifier.fillMaxWidth(), enabled = !estado.guardando,
                        isError = estado.errorDireccion != null, supportingText = { Text(estado.errorDireccion ?: "Calle, número y referencias.") },
                        trailingIcon = { if (estado.errorDireccion != null) Icon(Icons.Filled.Error, "Revisa la dirección") })
                    Text("Tarifas ficticias CLP: nacional $3.500 + $500 por unidad; internacional $15.000 + $2.000 por unidad. Express: 50% adicional.", color = TextoSecundario)
                    Text("Subtotal: ${moneda.format(estado.subtotal)}", color = TextoPrimario)
                    Text("Envío: ${estado.cotizacion?.let { moneda.format(it.envio) } ?: "Completa la selección y revisa los productos"}", color = TextoPrimario)
                    Text("Total final: ${estado.cotizacion?.let { moneda.format(it.total) } ?: "Pendiente"}", color = DoradoBrillante,
                        style = MaterialTheme.typography.titleLarge)
                    Text("Solicitud local de demostración, sin pago ni reserva de stock.", color = TextoSecundario)
                    AnimatedVisibility(estado.error != null) { Text(estado.error.orEmpty(), color = ErrorRojo) }
                    Button(onClick = viewModel::revisar, enabled = !estado.guardando && !estado.cargando, modifier = Modifier.fillMaxWidth()) {
                        Text(if (estado.guardando) "Guardando…" else "Revisar solicitud")
                    }
                }
            }
        }
    }
    estado.revision?.let { revision ->
        AlertDialog(onDismissRequest = viewModel::cancelarRevision, title = { Text("Confirmar solicitud local") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${revision.destino.etiqueta} · ${revision.modalidad.etiqueta}")
                Text("${revision.ubicacion}\n${revision.direccion}")
                Text("Subtotal: ${moneda.format(revision.cotizacion.subtotal)}")
                Text("Envío: ${moneda.format(revision.cotizacion.envio)}")
                Text("Total final: ${moneda.format(revision.cotizacion.total)}", fontWeight = FontWeight.Bold)
                Text("Se registrará como Pendiente local. Los productos pasarán del carrito al historial de solicitudes.")
            } },
            confirmButton = { TextButton(onClick = viewModel::confirmar, enabled = !estado.guardando) { Text("Confirmar solicitud") } },
            dismissButton = { TextButton(onClick = viewModel::cancelarRevision, enabled = !estado.guardando) { Text("Volver a revisar") } })
    }
}
