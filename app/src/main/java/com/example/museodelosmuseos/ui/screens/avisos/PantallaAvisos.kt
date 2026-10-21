package com.example.museodelosmuseos.ui.screens.avisos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAvisos(viewModel: AvisosViewModel, onVolver: () -> Unit, onAbrirContenido: (EnlaceAviso) -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL"))
    val colores = FilterChipDefaults.filterChipColors(selectedContainerColor = DoradoPatrimonial,
        selectedLabelColor = FondoOscuro, containerColor = AzulCard, labelColor = TextoSecundario)
    LaunchedEffect(estado.enlacePendiente) {
        estado.enlacePendiente?.let { viewModel.consumirEnlace(); onAbrirContenido(it) }
    }
    Scaffold(containerColor = FondoOscuro, topBar = {
        CenterAlignedTopAppBar(title = { Text("Avisos y notificaciones", color = DoradoPatrimonial) },
            navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = DoradoPatrimonial) } },
            actions = { if (estado.esAdministrador) IconButton(onClick = viewModel::crear) { Icon(Icons.Filled.Add, "Crear aviso", tint = DoradoPatrimonial) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulMuseoProfundo))
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Avisos locales de demostración", color = TextoPrimario, style = MaterialTheme.typography.titleLarge)
                Text("Se guardan en este dispositivo. No son notificaciones push ni mensajes de un servicio remoto.", color = TextoSecundario)
                Text("${estado.noLeidos} aviso(s) sin leer", color = DoradoBrillante)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiltroAvisos.entries.forEach { filtro -> FilterChip(selected = estado.filtro == filtro, onClick = { viewModel.filtrar(filtro) },
                        label = { Text(filtro.etiqueta) }, colors = colores) }
                }
                TextButton(onClick = viewModel::marcarTodos, enabled = estado.noLeidos > 0 && !estado.guardando) { Text("Marcar todos como leídos") }
                if (estado.cargando) CircularProgressIndicator()
                AnimatedVisibility(estado.confirmacion != null, enter = fadeIn() + slideInVertically { it / 2 }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.CheckCircle, "Cambio guardado", tint = VerdeExito)
                        Text(estado.confirmacion.orEmpty(), color = VerdeExito)
                    }
                }
                if (!estado.mostrarFormulario) AnimatedVisibility(estado.error != null) { Text(estado.error.orEmpty(), color = ErrorRojo) }
                if (!estado.cargando && estado.filtrados.isEmpty()) {
                    Icon(Icons.Filled.NotificationsNone, "Sin avisos", tint = DoradoPatrimonial, modifier = Modifier.size(48.dp))
                    Text("No tienes avisos para este filtro.", color = TextoSecundario)
                }
            }
            items(estado.filtrados, key = { it.aviso.id }) { registro ->
                Card(Modifier.fillMaxWidth().animateContentSize(), colors = CardDefaults.cardColors(containerColor = SuperficieCard)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (registro.leido) "Leído · Local" else "Nuevo · Local", color = if (registro.leido) TextoSecundario else DoradoBrillante)
                        Text(registro.aviso.titulo, color = DoradoPatrimonial, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(fecha.format(Date(registro.aviso.fechaTimestamp)), color = TextoSecundario)
                        Text(registro.aviso.mensaje, color = TextoPrimario)
                        if (registro.aviso.destino != DestinoAviso.SIN_ENLACE.name) OutlinedButton(
                            onClick = { viewModel.abrir(registro.aviso.id) }, enabled = !estado.guardando) { Text("Abrir contenido relacionado") }
                        TextButton(onClick = { viewModel.lectura(registro.aviso.id, !registro.leido) }, enabled = !estado.guardando) {
                            Text(if (registro.leido) "Marcar como no leído" else "Marcar como leído")
                        }
                    }
                }
            }
        }
    }
    if (estado.mostrarFormulario) {
        var elegirMuseo by remember { mutableStateOf(false) }
        val f = estado.formulario
        AlertDialog(onDismissRequest = viewModel::cerrarFormulario, title = { Text("Nuevo aviso local") }, text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(f.titulo, { valor -> viewModel.formulario { it.copy(titulo = valor) } }, label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(), enabled = !estado.guardando, isError = f.errorTitulo != null,
                    supportingText = { Text(f.errorTitulo ?: "3 a 80 caracteres") }, trailingIcon = { if (f.errorTitulo != null) Icon(Icons.Filled.Error, "Revisa el título") })
                OutlinedTextField(f.mensaje, { valor -> viewModel.formulario { it.copy(mensaje = valor) } }, label = { Text("Mensaje") },
                    modifier = Modifier.fillMaxWidth(), enabled = !estado.guardando, minLines = 2, isError = f.errorMensaje != null,
                    supportingText = { Text(f.errorMensaje ?: "10 a 500 caracteres") }, trailingIcon = { if (f.errorMensaje != null) Icon(Icons.Filled.Error, "Revisa el mensaje") })
                Text("Destinatarios")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AudienciaAviso.entries.forEach { audiencia -> FilterChip(selected = f.audiencia == audiencia, enabled = !estado.guardando,
                        onClick = { viewModel.formulario { it.copy(audiencia = audiencia) } }, label = { Text(audiencia.etiqueta) }, colors = colores) }
                }
                Text("Contenido relacionado")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DestinoAviso.entries.forEach { destino -> FilterChip(selected = f.destino == destino, enabled = !estado.guardando,
                        onClick = { viewModel.formulario { it.copy(destino = destino, museoId = null) } }, label = { Text(destino.etiqueta) }, colors = colores) }
                }
                if (f.destino == DestinoAviso.MUSEO) Box {
                    OutlinedButton(onClick = { elegirMuseo = true }, enabled = !estado.guardando) {
                        Text(estado.museos.firstOrNull { it.id == f.museoId }?.nombre ?: "Seleccionar museo")
                    }
                    DropdownMenu(expanded = elegirMuseo, onDismissRequest = { elegirMuseo = false }) {
                        estado.museos.forEach { museo -> DropdownMenuItem(text = { Text(museo.nombre) }, onClick = {
                            viewModel.formulario { it.copy(museoId = museo.id) }; elegirMuseo = false
                        }) }
                    }
                }
                estado.error?.let { Text(it, color = ErrorRojo) }
                Text("Publicación de prueba guardada solo en este dispositivo.", color = TextoSecundario)
            }
        }, confirmButton = { TextButton(onClick = viewModel::publicar, enabled = !estado.guardando) { Text(if (estado.guardando) "Guardando…" else "Publicar aviso") } },
            dismissButton = { TextButton(onClick = viewModel::cerrarFormulario, enabled = !estado.guardando) { Text("Cancelar") } })
    }
}
