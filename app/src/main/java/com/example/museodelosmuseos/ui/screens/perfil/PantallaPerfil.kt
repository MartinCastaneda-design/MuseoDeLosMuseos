package com.example.museodelosmuseos.ui.screens.perfil

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.museodelosmuseos.data.local.ArchivosAvatar
import com.example.museodelosmuseos.ui.screens.registro.CampoFormulario
import com.example.museodelosmuseos.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(viewModel: PerfilViewModel, archivos: ArchivosAvatar, onVolver: () -> Unit, onSalir: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var captura by rememberSaveable { mutableStateOf<String?>(null) }
    var propietarioCaptura by rememberSaveable { mutableStateOf(0L) }
    var propietarioGaleria by rememberSaveable { mutableStateOf(0L) }
    var confirmarSalida by remember { mutableStateOf<String?>(null) }
    var confirmarVolver by remember { mutableStateOf(false) }
    val volver = { if (!estado.guardando) { if (estado.modificado) confirmarVolver = true else { viewModel.descartarBorrador(); onVolver() } } }
    androidx.activity.compose.BackHandler { volver() }
    LaunchedEffect(estado.salida) { if (estado.salida) onSalir() }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = captura
        captura = null
        if (ruta != null && exito) viewModel.recibirImagen(archivos.uriCaptura(File(ruta)), propietarioCaptura) { archivos.borrarCaptura(ruta) }
        else {
            ruta?.let(archivos::borrarCaptura)
            viewModel.mensaje("Captura cancelada. Se conserva el avatar anterior.")
        }
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.recibirImagen(uri, propietarioGaleria)
    }
    Scaffold(containerColor = FondoOscuro, topBar = {
        CenterAlignedTopAppBar(title = { Text("Perfil y configuración", color = DoradoPatrimonial) },
            navigationIcon = { IconButton(onClick = volver, enabled = !estado.guardando) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver al Dashboard", tint = DoradoPatrimonial)
            } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulMuseoProfundo))
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (estado.cargando) CircularProgressIndicator()
            AnimatedVisibility(estado.confirmacion != null, enter = fadeIn() + slideInVertically { it / 2 }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.CheckCircle, null, tint = VerdeExito)
                    Text(estado.confirmacion.orEmpty(), color = VerdeExito)
                }
            }
            AnimatedVisibility(estado.error != null) { Text(estado.error.orEmpty(), color = ErrorRojo) }
            estado.usuario?.let { usuario ->
                Card(colors = CardDefaults.cardColors(containerColor = SuperficieCard), modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(Modifier.size(88.dp).clip(CircleShape).background(AzulCard), contentAlignment = Alignment.Center) {
                                val icono = when (estado.avatar) {
                                    "avatar_1", "avatar_investigador" -> Icons.Filled.School
                                    "avatar_2" -> Icons.Filled.Palette
                                    "avatar_3" -> Icons.Filled.Face
                                    "avatar_admin" -> Icons.Filled.AdminPanelSettings
                                    else -> Icons.Filled.Person
                                }
                                Icon(icono, "Avatar predeterminado", tint = DoradoPatrimonial, modifier = Modifier.size(54.dp))
                                archivos.modelo(estado.avatar)?.let { modelo -> AsyncImage(modelo, "Avatar del usuario activo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                            }
                            Column {
                                Text(usuario.nombre, color = DoradoPatrimonial, style = MaterialTheme.typography.titleMedium)
                                Text(usuario.rol.lowercase().replaceFirstChar { it.uppercase() }, color = TextoSecundario)
                            }
                        }
                        OutlinedButton(onClick = {
                            try {
                                val archivo = archivos.crearCaptura()
                                captura = archivo.path; propietarioCaptura = usuario.id
                                camara.launch(archivos.uriCaptura(archivo))
                            } catch (_: Exception) {
                                captura?.let(archivos::borrarCaptura); captura = null
                                viewModel.mensaje("No se pudo abrir la cámara. Puedes seleccionar una imagen de la galería.")
                            }
                        }, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.PhotoCamera, null); Spacer(Modifier.width(8.dp)); Text("Tomar fotografía")
                        }
                        OutlinedButton(onClick = {
                            propietarioGaleria = usuario.id
                            try { galeria.launch("image/*") } catch (_: Exception) { viewModel.mensaje("No hay un selector de imágenes disponible.") }
                        }, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth()) { Text("Seleccionar de galería") }
                        TextButton(onClick = viewModel::avatarPredeterminado, enabled = !estado.guardando) { Text("Usar avatar predeterminado") }
                        CampoPerfil("Nombre", estado.nombre, viewModel::nombre, !estado.guardando)
                        CampoPerfil("Correo", estado.correo, viewModel::correo, !estado.guardando, KeyboardType.Email)
                        Text("Tu rol está asignado por el sistema y no se puede editar desde el perfil.", color = TextoSecundario)
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = SuperficieCard)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Preferencias de este usuario", color = DoradoPatrimonial, style = MaterialTheme.typography.titleMedium)
                        AjustePerfil("Indicador de avisos sin leer", "Muestra u oculta el contador del Dashboard. La bandeja conserva todos los avisos locales.", estado.indicadorAvisos, !estado.guardando, viewModel::indicador)
                        AjustePerfil("Texto grande", "Aumenta el texto de la aplicación un 15 %, además del tamaño elegido en Android. Se aplica al guardar.", estado.textoGrande, !estado.guardando, viewModel::textoGrande)
                        Text("Idioma: Español", color = TextoPrimario)
                        Text("Único idioma disponible en esta versión. No hay traducciones adicionales activadas.", color = TextoSecundario)
                        Text("Esta versión utiliza avisos dentro de la aplicación; no envía notificaciones del sistema ni push.", color = TextoSecundario)
                    }
                }
                Button(onClick = viewModel::guardar, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth()) {
                    if (estado.guardando) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Guardar cambios")
                }
                Card(colors = CardDefaults.cardColors(containerColor = SuperficieCard)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Permisos del dispositivo", color = DoradoPatrimonial, style = MaterialTheme.typography.titleMedium)
                        Text("Cámara: se abre la aplicación de cámara de Android. MuseoDeLosMuseos no solicita acceso directo a la cámara; comparte únicamente el archivo temporal de captura.", color = TextoSecundario)
                        Text("Galería: solo se lee la imagen que seleccionas y se copia al almacenamiento privado del usuario. No se solicita acceso a todas tus fotos.", color = TextoSecundario)
                        Text("Ubicación: esta versión no solicita permiso de ubicación ni obtiene tu GPS. El mapa muestra las coordenadas de los museos.", color = TextoSecundario)
                        TextButton(onClick = {
                            try { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }
                            catch (_: Exception) { viewModel.mensaje("No fue posible abrir los ajustes de Android.") }
                        }) { Text("Abrir ajustes de Android") }
                    }
                }
                OutlinedButton(onClick = { confirmarSalida = "Cambiar de usuario" }, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth()) { Text("Cambiar de usuario") }
                TextButton(onClick = { confirmarSalida = "Cerrar sesión" }, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
                Text("Los datos se guardan localmente en este dispositivo.", color = TextoSecundario)
            }
        }
    }
    confirmarSalida?.let { titulo -> AlertDialog(onDismissRequest = { confirmarSalida = null }, title = { Text(titulo) },
        text = { Text("Se cerrará tu sesión y podrás iniciar con otra cuenta. ${if (estado.modificado) "Hay cambios sin guardar que se descartarán." else "Tus datos guardados se conservarán."}") },
        confirmButton = { TextButton(onClick = { confirmarSalida = null; viewModel.cerrarSesion() }) { Text("Continuar") } },
        dismissButton = { TextButton(onClick = { confirmarSalida = null }) { Text("Cancelar") } }) }
    if (confirmarVolver) AlertDialog(onDismissRequest = { confirmarVolver = false }, title = { Text("Cambios sin guardar") },
        text = { Text("Guarda los cambios antes de salir o descártalos para volver al Dashboard.") },
        confirmButton = { TextButton(onClick = { confirmarVolver = false; viewModel.descartarBorrador(); onVolver() }) { Text("Descartar y volver") } },
        dismissButton = { TextButton(onClick = { confirmarVolver = false }) { Text("Seguir editando") } })
}

@Composable
private fun CampoPerfil(etiqueta: String, campo: CampoFormulario, cambiar: (String) -> Unit, habilitado: Boolean, teclado: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(campo.valor, cambiar, label = { Text(etiqueta) }, modifier = Modifier.fillMaxWidth(),
        enabled = habilitado, singleLine = true, isError = campo.error != null, keyboardOptions = KeyboardOptions(keyboardType = teclado),
        supportingText = { campo.error?.let { Text(it) } }, trailingIcon = {
            if (campo.error != null) Icon(Icons.Filled.Error, "Error en $etiqueta", tint = ErrorRojo)
            else if (campo.valor.isNotBlank()) Icon(Icons.Filled.CheckCircle, "$etiqueta válido", tint = VerdeExito)
        })
}

@Composable
private fun AjustePerfil(titulo: String, descripcion: String, valor: Boolean, habilitado: Boolean, cambiar: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(titulo, color = TextoPrimario); Text(descripcion, color = TextoSecundario, style = MaterialTheme.typography.bodySmall) }
        Switch(checked = valor, onCheckedChange = cambiar, enabled = habilitado)
    }
}
