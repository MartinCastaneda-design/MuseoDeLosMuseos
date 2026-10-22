package com.example.museodelosmuseos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.museodelosmuseos.R
import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.model.ImagenesMuseosDemo

/** Mismo respaldo durante carga, error y URL vacía, sin alterar el tamaño del contenedor. */
@Composable
fun ImagenMuseo(museo: Museo, modifier: Modifier = Modifier, mostrarCreditosAlTocar: Boolean = false) {
    val foto = FotosMuseos.paraCodigo(museo.codigoQr)
    val respaldo = painterResource(foto?.recurso ?: R.drawable.favoritos_museo)
    var creditos by remember(museo.codigoQr) { mutableStateOf(false) }
    var errorEnlace by remember { mutableStateOf(false) }
    val navegador = LocalUriHandler.current
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(museo.imagenUrl.trim().takeIf {
                it.isNotEmpty() && !(foto != null && ImagenesMuseosDemo.usarFotografiaLocal(museo.codigoQr, it))
            }).crossfade(true).build(),
        contentDescription = museo.nombre,
        placeholder = respaldo, error = respaldo, fallback = respaldo,
        contentScale = ContentScale.Crop,
        modifier = if (mostrarCreditosAlTocar && foto != null) modifier.clickable(onClickLabel = "Créditos de la fotografía local") { creditos = true } else modifier
    )
    if (creditos && foto != null) AlertDialog(onDismissRequest = { creditos = false }, title = { Text("Fotografía local de respaldo") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(museo.nombre)
            Text("Autor: ${foto.autor}")
            Text("Fuente: ${foto.fuente}")
            Text("Licencia: ${foto.licencia}")
            Text("Copia reducida y comprimida a WebP; recorte de presentación. Conserva la licencia original. Si se muestra una imagen remota distinta, estos créditos corresponden al respaldo local.")
            if (errorEnlace) Text("No se pudo abrir el enlace. La dirección permanece disponible en estos créditos.")
            TextButton(onClick = { errorEnlace = runCatching { navegador.openUri(foto.fuente) }.isFailure }) { Text("Abrir fuente") }
            TextButton(onClick = { errorEnlace = runCatching { navegador.openUri(foto.licenciaUrl) }.isFailure }) { Text("Consultar licencia") }
        }
    }, confirmButton = { TextButton(onClick = { creditos = false }) { Text("Cerrar") } })
}
