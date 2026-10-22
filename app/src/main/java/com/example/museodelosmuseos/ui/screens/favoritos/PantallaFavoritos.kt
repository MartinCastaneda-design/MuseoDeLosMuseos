package com.example.museodelosmuseos.ui.screens.favoritos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.ui.components.ImagenMuseo
import com.example.museodelosmuseos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFavoritos(viewModel: FavoritosViewModel, onVolver: () -> Unit, onAbrirMuseo: (Long) -> Unit,
    onExplorar: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val navegador = LocalUriHandler.current
    LaunchedEffect(estado.enlacePendiente) {
        estado.enlacePendiente?.let { url ->
            val fallo = runCatching { navegador.openUri(url) }.isFailure
            viewModel.consumirEnlace(fallo)
        }
    }
    Scaffold(containerColor = FondoOscuro, topBar = {
        CenterAlignedTopAppBar(title = { Text("Mis museos favoritos", color = DoradoPatrimonial) },
            navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = DoradoPatrimonial) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulMuseoProfundo))
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                if (estado.cargando) CircularProgressIndicator()
                AnimatedVisibility(estado.error != null) { Text(estado.error.orEmpty(), color = ErrorRojo) }
                AnimatedVisibility(estado.confirmacion != null, enter = fadeIn() + slideInVertically { it / 2 }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.CheckCircle, "Favoritos actualizados", tint = VerdeExito)
                        Text(estado.confirmacion.orEmpty(), color = VerdeExito)
                    }
                }
                if (estado.museos.isNotEmpty()) OutlinedTextField(value = estado.busqueda, onValueChange = viewModel::buscar,
                    singleLine = true, label = { Text("Buscar por nombre, país o clasificación") }, modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Filled.Search, null) })
                if (!estado.cargando && estado.museos.isEmpty()) {
                    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Filled.FavoriteBorder, "Sin favoritos", tint = DoradoPatrimonial, modifier = Modifier.size(64.dp))
                        Text("Tu colección todavía está vacía", color = TextoPrimario, style = MaterialTheme.typography.titleLarge)
                        Text("Guarda museos desde su ficha para encontrarlos aquí.", color = TextoSecundario)
                        Button(onClick = onExplorar) { Text("Explorar museos") }
                    }
                } else if (!estado.cargando && estado.filtrados.isEmpty()) Text("No hay museos que coincidan con tu búsqueda.", color = TextoSecundario)
            }
            items(estado.filtrados, key = { it.id }) { museo ->
                Card(modifier = Modifier.fillMaxWidth().animateContentSize(), colors = CardDefaults.cardColors(containerColor = SuperficieCard)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ImagenMuseo(museo, modifier = Modifier.fillMaxWidth().height(160.dp))
                        Text(museo.nombre, color = DoradoPatrimonial, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(museo.pais, color = TextoPrimario)
                        if (museo.clasificacion.isNotBlank()) Text(museo.clasificacion, color = TextoSecundario)
                        Button(onClick = { onAbrirMuseo(museo.id) }, modifier = Modifier.fillMaxWidth()) { Text("Abrir ficha del museo") }
                        OutlinedButton(onClick = { viewModel.abrirRecorrido(museo) }, modifier = Modifier.fillMaxWidth()) { Text("Abrir recorrido virtual") }
                        TextButton(onClick = { viewModel.pedirQuitar(museo) }, enabled = !estado.guardando) { Text("Quitar de favoritos", color = ErrorRojo) }
                    }
                }
            }
        }
    }
    estado.porQuitar?.let { museo ->
        AlertDialog(onDismissRequest = viewModel::cancelar, title = { Text("Quitar de favoritos") },
            text = { Text("¿Quieres quitar ${museo.nombre} de tu colección? El museo seguirá disponible para consultar.") },
            confirmButton = { TextButton(onClick = viewModel::confirmarQuitar, enabled = !estado.guardando) { Text(if (estado.guardando) "Guardando…" else "Quitar") } },
            dismissButton = { TextButton(onClick = viewModel::cancelar, enabled = !estado.guardando) { Text("Cancelar") } })
    }
}
