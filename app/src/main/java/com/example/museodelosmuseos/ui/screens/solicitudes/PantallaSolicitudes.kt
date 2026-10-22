package com.example.museodelosmuseos.ui.screens.solicitudes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSolicitudes(viewModel: SolicitudesViewModel, onVolver: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))
    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL"))
    val coloresFiltro = FilterChipDefaults.filterChipColors(selectedContainerColor = DoradoPatrimonial,
        selectedLabelColor = FondoOscuro, containerColor = AzulCard, labelColor = TextoSecundario)
    Scaffold(containerColor = FondoOscuro, topBar = {
        CenterAlignedTopAppBar(title = { Text("Historial de solicitudes", color = DoradoPatrimonial) },
            navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = DoradoPatrimonial) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulMuseoProfundo))
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(if (estado.esAdministrador) "Gestión de todos los clientes" else "Mis solicitudes", color = TextoPrimario,
                    style = MaterialTheme.typography.titleLarge)
                Text("Registros locales y ficticios. No están sincronizados con un backend ni representan pagos reales.", color = TextoSecundario)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = estado.filtro == null, onClick = { viewModel.filtrar(null) }, colors = coloresFiltro, label = { Text("Todos") })
                    EstadoSolicitud.entries.forEach { filtro -> FilterChip(selected = estado.filtro == filtro,
                        onClick = { viewModel.filtrar(filtro) }, colors = coloresFiltro, label = { Text(filtro.etiqueta) }) }
                }
                if (estado.cargando) CircularProgressIndicator()
                AnimatedVisibility(estado.confirmacion != null, enter = fadeIn() + slideInVertically { it / 2 }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.CheckCircle, "Cambio guardado", tint = VerdeExito)
                        Text(estado.confirmacion.orEmpty(), color = VerdeExito)
                    }
                }
                AnimatedVisibility(estado.error != null && estado.editando == null) { Text(estado.error.orEmpty(), color = ErrorRojo) }
                if (!estado.cargando && estado.filtradas.isEmpty()) Text("No hay solicitudes para este filtro.", color = TextoSecundario)
            }
            items(estado.filtradas, key = { it.clave }) { solicitud ->
                Card(modifier = Modifier.fillMaxWidth().animateContentSize(),
                    colors = CardDefaults.cardColors(containerColor = SuperficieCard)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(solicitud.titulo, color = DoradoPatrimonial, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(fecha.format(Date(solicitud.fecha)), color = TextoSecundario)
                        if (estado.esAdministrador) Text("Cliente #${solicitud.usuarioId}", color = TextoPrimario)
                        Text("Estado: ${EstadoSolicitud.desde(solicitud.estado)?.etiqueta ?: solicitud.estado} · Local", color = DoradoBrillante)
                        if (solicitud.pedido != null) {
                            Text(solicitud.lineas.joinToString("\n") { "${it.cantidad} × ${it.titulo}" }, color = TextoPrimario)
                            Text("Total histórico: ${moneda.format(solicitud.pedido.total)} CLP", color = DoradoBrillante)
                        } else Text("Consulta de curso · Sin compra ni monto asociado", color = TextoPrimario)
                        TextButton(onClick = { viewModel.expandir(solicitud.clave) }) {
                            Text(if (estado.expandida == solicitud.clave) "Ocultar detalle" else "Ver detalle")
                        }
                        AnimatedVisibility(estado.expandida == solicitud.clave) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                solicitud.pedido?.let { pedido ->
                                    Text("Referencia: ${pedido.id}", color = TextoSecundario)
                                    solicitud.lineas.forEach { linea ->
                                        Text("${linea.titulo}\n${linea.cantidad} unidad(es) · Precio histórico: ${moneda.format(linea.precioUnitario)}\nSubtotal histórico: ${moneda.format(linea.subtotal)}", color = TextoPrimario)
                                    }
                                    Text("Envío: ${DestinoEnvio.entries.firstOrNull { it.name == pedido.destino }?.etiqueta ?: pedido.destino} · ${ModalidadEnvio.entries.firstOrNull { it.name == pedido.modalidad }?.etiqueta ?: pedido.modalidad}", color = TextoSecundario)
                                    Text("${pedido.ubicacion}\n${pedido.direccion}", color = TextoPrimario)
                                    Text("Subtotal: ${moneda.format(pedido.subtotal)}\nEnvío: ${moneda.format(pedido.envio)}\nTotal: ${moneda.format(pedido.total)} CLP", color = DoradoBrillante)
                                }
                                solicitud.consulta?.let { Text("Consulta registrada:\n${it.mensaje}", color = TextoPrimario) }
                            }
                        }
                        if (estado.esAdministrador) OutlinedButton(onClick = { viewModel.editar(solicitud) }, enabled = !estado.guardando) {
                            Text("Actualizar estado")
                        }
                    }
                }
            }
        }
    }
    estado.editando?.let { solicitud ->
        AlertDialog(onDismissRequest = viewModel::cancelar, title = { Text("Actualizar estado local") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(solicitud.titulo)
                    Text("Actual: ${EstadoSolicitud.desde(solicitud.estado)?.etiqueta ?: solicitud.estado}")
                    EstadoSolicitud.entries.forEach { nuevo ->
                        FilterChip(selected = estado.nuevoEstado == nuevo, onClick = { viewModel.seleccionar(nuevo) },
                            enabled = !estado.guardando, colors = coloresFiltro, label = { Text(nuevo.etiqueta) })
                    }
                    if (estado.error != null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Error, "Revisa el estado", tint = ErrorRojo)
                        Text(estado.error.orEmpty(), color = ErrorRojo)
                    }
                    Text("El cambio se guarda únicamente en este dispositivo.")
                }
            },
            confirmButton = { TextButton(onClick = viewModel::guardar, enabled = !estado.guardando) { Text(if (estado.guardando) "Guardando…" else "Guardar estado") } },
            dismissButton = { TextButton(onClick = viewModel::cancelar, enabled = !estado.guardando) { Text("Cancelar") } })
    }
}
