package com.example.museodelosmuseos.ui.screens.solicitudes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.data.remote.microservicio.SolicitudCulturalDto
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
    val coloresFiltro = FilterChipDefaults.filterChipColors(
        selectedContainerColor = DoradoPatrimonial,
        selectedLabelColor = FondoOscuro,
        containerColor = AzulCard,
        labelColor = TextoSecundario
    )

    var tituloNuevo by remember { mutableStateOf("") }
    var descripcionNueva by remember { mutableStateOf("") }
    var tipoNuevo by remember { mutableStateOf("INVESTIGACION") }
    var museoNuevo by remember { mutableStateOf("Museo Nacional de Bellas Artes") }
    var urlManual by remember(estado.urlServidor) { mutableStateOf(estado.urlServidor) }

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestión de Solicitudes", color = DoradoPatrimonial, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = DoradoPatrimonial)
                    }
                },
                actions = {
                    if (estado.modoMicroservicio) {
                        IconButton(onClick = { viewModel.abrirDialogoServidor() }) {
                            Icon(Icons.Filled.Settings, "Servidor IP", tint = DoradoPatrimonial)
                        }
                        IconButton(onClick = { viewModel.cargarSolicitudesRemotas() }) {
                            Icon(Icons.Filled.Refresh, "Recargar", tint = DoradoBrillante)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulMuseoProfundo)
            )
        },
        floatingActionButton = {
            if (estado.modoMicroservicio) {
                FloatingActionButton(
                    onClick = { viewModel.abrirDialogoCrear() },
                    containerColor = DoradoPatrimonial,
                    contentColor = FondoOscuro
                ) {
                    Icon(Icons.Filled.Add, "Nueva Solicitud")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = estado.modoMicroservicio,
                        onClick = { viewModel.alternarModo(true) },
                        leadingIcon = { Icon(Icons.Filled.Dns, null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Spring Boot API (En vivo)", fontWeight = FontWeight.SemiBold) },
                        colors = coloresFiltro,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !estado.modoMicroservicio,
                        onClick = { viewModel.alternarModo(false) },
                        leadingIcon = { Icon(Icons.Filled.Storage, null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Almacén Local (Room)", fontWeight = FontWeight.SemiBold) },
                        colors = coloresFiltro,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (estado.modoMicroservicio) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AzulCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(DoradoPatrimonial.copy(alpha = 0.5f), AzulMuseoMedio))
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (estado.servidorConectado) "🟢" else "🔴",
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (estado.servidorConectado) "Microservicio Spring Boot Activo" else "Microservicio Spring Boot Desconectado",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextoPrimario,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "URL: ${estado.urlServidor}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DoradoPatrimonial
                                    )
                                }
                                TextButton(onClick = { viewModel.abrirDialogoServidor() }) {
                                    Text("IP / Conexión", fontSize = 11.sp, color = DoradoBrillante)
                                }
                            }
                            Text(
                                text = "Operaciones CRUD en tiempo real con base de datos H2 en Spring Boot.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextoSecundario,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (estado.esAdministrador) "Gestión de todos los clientes (Local)" else "Mis solicitudes (Local)",
                        color = TextoPrimario,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Registros almacenados localmente en SQLite (Room).", color = TextoSecundario, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedVisibility(estado.confirmacion != null, enter = fadeIn() + slideInVertically { it / 2 }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VerdeExito.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = VerdeExito, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(estado.confirmacion.orEmpty(), color = VerdeExito, fontSize = 13.sp)
                        }
                    }
                }

                AnimatedVisibility(estado.error != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ErrorRojo.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Error, null, tint = ErrorRojo, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(estado.error.orEmpty(), color = ErrorRojo, fontSize = 13.sp)
                        }
                    }
                }

                AnimatedVisibility(estado.errorRemoto != null && estado.modoMicroservicio) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ErrorRojo.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(estado.errorRemoto.orEmpty(), color = ErrorRojo, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.cargarSolicitudesRemotas() },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DoradoPatrimonial)
                                ) {
                                    Text("Reintentar", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { viewModel.abrirDialogoServidor() },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DoradoPatrimonial, contentColor = FondoOscuro)
                                ) {
                                    Text("Configurar IP", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                if (estado.cargando || estado.cargandoRemoto) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = DoradoBrillante)
                    }
                }
            }

            if (estado.modoMicroservicio) {
                if (!estado.cargandoRemoto && estado.solicitudesRemotas.isEmpty() && estado.errorRemoto == null) {
                    item {
                        Text("No hay solicitudes culturales en el microservicio. Toca + para crear la primera.", color = TextoSecundario)
                    }
                }

                items(estado.solicitudesRemotas, key = { it.id ?: 0L }) { remota ->
                    Card(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SuperficieCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(DoradoPatrimonial.copy(alpha = 0.4f), AzulMuseoMedio))
                        )
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${remota.id} • ${remota.titulo}",
                                    color = DoradoPatrimonial,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = when (remota.estado.uppercase()) {
                                        "APROBADA" -> VerdeExito.copy(alpha = 0.2f)
                                        "RECHAZADA" -> ErrorRojo.copy(alpha = 0.2f)
                                        "EN_REVISION" -> DoradoBrillante.copy(alpha = 0.2f)
                                        else -> AzulMuseoMedio
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = remota.estado,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when (remota.estado.uppercase()) {
                                            "APROBADA" -> VerdeExito
                                            "RECHAZADA" -> ErrorRojo
                                            "EN_REVISION" -> DoradoBrillante
                                            else -> TextoSecundario
                                        },
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Museo: ${remota.museoNombre} • Tipo: ${remota.tipoSolicitud}",
                                color = DoradoBrillante,
                                fontSize = 12.sp
                            )
                            Text(
                                text = remota.descripcion,
                                color = TextoPrimario,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Text(
                                text = "Solicitante: ${remota.solicitanteNombre} (${remota.solicitanteCorreo})",
                                color = TextoSecundario,
                                fontSize = 11.sp
                            )

                            if (!remota.respuesta.isNullOrBlank()) {
                                Surface(
                                    color = AzulMuseoProfundo,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Respuesta oficial:", color = DoradoPatrimonial, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(remota.respuesta, color = TextoPrimario, fontSize = 12.sp)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { viewModel.editarRemota(remota) }) {
                                    Icon(Icons.Filled.Edit, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Actualizar (PUT)", fontSize = 12.sp)
                                }
                                TextButton(
                                    onClick = { remota.id?.let { viewModel.eliminarSolicitudRemota(it) } },
                                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRojo)
                                ) {
                                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Eliminar (DELETE)", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                if (!estado.cargando && estado.filtradas.isEmpty()) {
                    item {
                        Text("No hay solicitudes para este filtro.", color = TextoSecundario)
                    }
                }

                items(estado.filtradas, key = { it.clave }) { solicitud ->
                    Card(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = SuperficieCard)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(solicitud.titulo, color = DoradoPatrimonial, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(fecha.format(Date(solicitud.fecha)), color = TextoSecundario)
                            if (estado.esAdministrador) Text("Cliente #${solicitud.usuarioId}", color = TextoPrimario)
                            Text("Estado: ${EstadoSolicitud.desde(solicitud.estado)?.etiqueta ?: solicitud.estado} · Local", color = DoradoBrillante)
                            if (solicitud.pedido != null) {
                                Text(solicitud.lineas.joinToString("\n") { "${it.cantidad} × ${it.titulo}" }, color = TextoPrimario)
                                Text("Total histórico: ${moneda.format(solicitud.pedido.total)} CLP", color = DoradoBrillante)
                            } else {
                                Text("Consulta cultural · Sin compra ni monto asociado", color = TextoPrimario)
                            }
                            TextButton(onClick = { viewModel.expandir(solicitud.clave) }) {
                                Text(if (estado.expandida == solicitud.clave) "Ocultar detalle" else "Ver detalle")
                            }
                            AnimatedVisibility(estado.expandida == solicitud.clave) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    solicitud.pedido?.let { pedido ->
                                        Text("Referencia: ${pedido.id}", color = TextoSecundario)
                                        solicitud.lineas.forEach { linea ->
                                            Text("${linea.titulo}\n${linea.cantidad} unidad(es) · Precio: ${moneda.format(linea.precioUnitario)}\nSubtotal: ${moneda.format(linea.subtotal)}", color = TextoPrimario)
                                        }
                                        Text("Envío: ${pedido.destino} · ${pedido.modalidad}", color = TextoSecundario)
                                        Text("${pedido.ubicacion}\n${pedido.direccion}", color = TextoPrimario)
                                        Text("Total: ${moneda.format(pedido.total)} CLP", color = DoradoBrillante)
                                    }
                                    solicitud.consulta?.let { Text("Consulta registrada:\n${it.mensaje}", color = TextoPrimario) }
                                }
                            }
                            if (estado.esAdministrador) {
                                OutlinedButton(onClick = { viewModel.editar(solicitud) }, enabled = !estado.guardando) {
                                    Text("Actualizar estado")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (estado.mostrandoDialogoServidor) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarDialogoServidor() },
            title = { Text("Conexión con Spring Boot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selecciona el modo según cómo pruebas tu app:", fontSize = 12.sp, color = TextoSecundario)
                    Button(
                        onClick = { viewModel.cambiarServidor("http://localhost:8080/") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulCard, contentColor = DoradoPatrimonial)
                    ) {
                        Text("Cable USB con adb reverse (localhost:8080)", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { viewModel.cambiarServidor("http://192.168.1.10:8080/") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulCard, contentColor = DoradoPatrimonial)
                    ) {
                        Text("Red Wi-Fi PC (192.168.1.10:8080)", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { viewModel.cambiarServidor("http://10.0.2.2:8080/") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulCard, contentColor = DoradoPatrimonial)
                    ) {
                        Text("Emulador Android Studio (10.0.2.2:8080)", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = urlManual,
                        onValueChange = { urlManual = it },
                        label = { Text("O ingresar URL manual") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.cambiarServidor(urlManual) },
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoPatrimonial, contentColor = FondoOscuro)
                ) {
                    Text("Aplicar URL")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cerrarDialogoServidor() }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (estado.mostrandoDialogoCrear) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarDialogoCrear() },
            title = { Text("Nueva Solicitud Cultural (POST)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tituloNuevo,
                        onValueChange = { tituloNuevo = it },
                        label = { Text("Título de la solicitud") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descripcionNueva,
                        onValueChange = { descripcionNueva = it },
                        label = { Text("Descripción del requerimiento") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = museoNuevo,
                        onValueChange = { museoNuevo = it },
                        label = { Text("Museo de destino") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Tipo de solicitud:", fontSize = 12.sp, color = TextoSecundario)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("INVESTIGACION", "VISITA_GUIADA", "INFORMACION_TECNICA", "PRESTAMO").forEach { t ->
                            FilterChip(
                                selected = tipoNuevo == t,
                                onClick = { tipoNuevo = t },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.crearSolicitudRemota(tituloNuevo, descripcionNueva, tipoNuevo, museoNuevo)
                        tituloNuevo = ""
                        descripcionNueva = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoPatrimonial, contentColor = FondoOscuro)
                ) {
                    Text("Crear en Spring Boot")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cerrarDialogoCrear() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    estado.editandoRemota?.let { remota ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEdicionRemota() },
            title = { Text("Actualizar Solicitud #${remota.id} (PUT)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(remota.titulo, fontWeight = FontWeight.Bold, color = DoradoPatrimonial)
                    Text("Estado actual: ${remota.estado}", fontSize = 12.sp)

                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("PENDIENTE", "EN_REVISION", "APROBADA", "RECHAZADA", "COMPLETADA").forEach { e ->
                            FilterChip(
                                selected = estado.nuevoEstadoRemoto == e,
                                onClick = { viewModel.actualizarEstadoRemotoSeleccionado(e) },
                                label = { Text(e, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = estado.nuevaRespuestaRemota,
                        onValueChange = { viewModel.actualizarRespuestaRemotaTexto(it) },
                        label = { Text("Respuesta institucional") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.guardarActualizacionRemota() },
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoPatrimonial, contentColor = FondoOscuro)
                ) {
                    Text("Guardar (PUT)")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarEdicionRemota() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    estado.editando?.let { solicitud ->
        AlertDialog(
            onDismissRequest = viewModel::cancelar,
            title = { Text("Actualizar estado local") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(solicitud.titulo)
                    Text("Actual: ${EstadoSolicitud.desde(solicitud.estado)?.etiqueta ?: solicitud.estado}")
                    EstadoSolicitud.entries.forEach { nuevo ->
                        FilterChip(
                            selected = estado.nuevoEstado == nuevo,
                            onClick = { viewModel.seleccionar(nuevo) },
                            enabled = !estado.guardando,
                            colors = coloresFiltro,
                            label = { Text(nuevo.etiqueta) }
                        )
                    }
                    if (estado.error != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Error, "Revisa el estado", tint = ErrorRojo)
                            Text(estado.error.orEmpty(), color = ErrorRojo)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::guardar, enabled = !estado.guardando) {
                    Text(if (estado.guardando) "Guardando…" else "Guardar estado")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelar, enabled = !estado.guardando) {
                    Text("Cancelar")
                }
            }
        )
    }
}
