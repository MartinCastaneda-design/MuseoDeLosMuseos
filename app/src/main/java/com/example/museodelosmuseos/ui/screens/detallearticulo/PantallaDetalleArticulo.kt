package com.example.museodelosmuseos.ui.screens.detallearticulo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.museodelosmuseos.data.model.CategoriaArticulo
import com.example.museodelosmuseos.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleArticulo(viewModel: DetalleArticuloViewModel, onVolver: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))
    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle y solicitud", color = DoradoPatrimonial) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver al catálogo", tint = DoradoPatrimonial)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulMuseoProfundo)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding()
                .verticalScroll(rememberScrollState()).padding(20.dp).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(color = DoradoPatrimonial)
                Text("Cargando artículo…", color = TextoSecundario)
            }
            estado.articulo?.let { articulo ->
                if (articulo.imagenUrl.isNotBlank()) {
                    AsyncImage(model = articulo.imagenUrl, contentDescription = articulo.titulo,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(220.dp))
                } else {
                    Icon(Icons.Filled.ShoppingBag, "Artículo sin imagen", tint = DoradoPatrimonial, modifier = Modifier.size(72.dp))
                }
                Text(articulo.titulo, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = TextoPrimario)
                Text(CategoriaArticulo.desdeTexto(articulo.categoria)?.etiqueta ?: articulo.categoria,
                    color = DoradoPatrimonial)
                Text(articulo.descripcion, color = TextoPrimario)
                Text("Precio de prueba: ${moneda.format(articulo.precio)}", color = DoradoBrillante,
                    style = MaterialTheme.typography.titleLarge)
                Text("${if (estado.esCurso) "Cupos" else "Stock"}: ${articulo.stock}", color = TextoSecundario)
                estado.errorDisponibilidad?.let { Text(it, color = ErrorRojo) }
                if (estado.esCurso) {
                    Text("Solicita detalles del programa. Tu consulta se guardará localmente; aún no se envía a la organización.", color = TextoSecundario)
                    OutlinedTextField(
                        value = estado.consulta, onValueChange = viewModel::cambiarConsulta,
                        label = { Text("¿Qué quieres saber del curso?") },
                        minLines = 3, maxLines = 6, modifier = Modifier.fillMaxWidth(),
                        enabled = !estado.guardando && !estado.solicitudRegistrada,
                        isError = estado.errorConsulta != null,
                        trailingIcon = {
                            if (estado.errorConsulta != null) Icon(Icons.Filled.Error, "Revisa tu consulta", tint = ErrorRojo)
                        },
                        supportingText = { Text(estado.errorConsulta ?: "${estado.consulta.length}/500 caracteres") }
                    )
                    if (estado.solicitudRegistrada) Text("Ya registraste una consulta para este curso.", color = VerdeExito)
                } else {
                    Text("En tu carrito: ${estado.enCarrito} unidad(es).", color = TextoSecundario)
                    OutlinedTextField(
                        value = estado.cantidad, onValueChange = viewModel::cambiarCantidad,
                        label = { Text("Cantidad a agregar") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !estado.guardando, modifier = Modifier.fillMaxWidth(),
                        isError = estado.errorCantidad != null,
                        trailingIcon = {
                            if (estado.errorCantidad != null) Icon(Icons.Filled.Error, "Revisa la cantidad", tint = ErrorRojo)
                        },
                        supportingText = { Text(estado.errorCantidad ?: "La cantidad acumulada no puede superar el stock.") }
                    )
                    if (estado.errorCantidad == null && estado.cantidad.toIntOrNull() != null) {
                        Text("Subtotal a agregar: ${moneda.format(estado.subtotal)}", color = TextoPrimario)
                    }
                    Text("Agregar al carrito no reserva unidades ni confirma una compra.", color = TextoSecundario)
                }
                Button(
                    onClick = viewModel::enviar,
                    enabled = !estado.guardando && estado.errorDisponibilidad == null &&
                        !(estado.esCurso && estado.solicitudRegistrada),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DoradoPatrimonial, contentColor = FondoOscuro)
                ) {
                    if (estado.guardando) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = FondoOscuro)
                    else Text(if (estado.esCurso) "Solicitar información" else "Agregar al carrito")
                }
            }
            if (!estado.cargando && estado.articulo == null && estado.error == null) {
                Text("El artículo ya no existe. Vuelve al catálogo para seleccionar otro.", color = ErrorRojo)
            }
            AnimatedVisibility(visible = estado.error != null, enter = fadeIn(), exit = fadeOut()) {
                Text(estado.error.orEmpty(), color = ErrorRojo)
            }
            AnimatedVisibility(visible = estado.confirmacion != null,
                enter = fadeIn() + slideInVertically { it / 2 }, exit = fadeOut()) {
                Surface(color = AzulCard, shape = MaterialTheme.shapes.medium) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.CheckCircle, "Acción completada", tint = VerdeExito)
                        Text(estado.confirmacion.orEmpty(), color = VerdeExito)
                    }
                }
            }
        }
    }
}
