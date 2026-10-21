package com.example.museodelosmuseos.ui.screens.catalogo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.museodelosmuseos.R
import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import com.example.museodelosmuseos.data.model.CategoriaArticulo
import com.example.museodelosmuseos.ui.theme.AzulCard
import com.example.museodelosmuseos.ui.theme.AzulMuseoMedio
import com.example.museodelosmuseos.ui.theme.AzulMuseoProfundo
import com.example.museodelosmuseos.ui.theme.DoradoBrillante
import com.example.museodelosmuseos.ui.theme.DoradoPatrimonial
import com.example.museodelosmuseos.ui.theme.ErrorRojo
import com.example.museodelosmuseos.ui.theme.FondoOscuro
import com.example.museodelosmuseos.ui.theme.SuperficieCard
import com.example.museodelosmuseos.ui.theme.Terracota
import com.example.museodelosmuseos.ui.theme.TextoApagado
import com.example.museodelosmuseos.ui.theme.TextoPrimario
import com.example.museodelosmuseos.ui.theme.TextoSecundario
import com.example.museodelosmuseos.ui.theme.VerdeExito

/**
 * ============================================================================
 * PANTALLA 9: CATÁLOGO DE PRODUCTOS Y CURSOS
 * ============================================================================
 * Requerimientos funcionales del caso de estudio:
 * - Roles: Administrador y Cliente.
 * - Exhibe cursos, publicaciones, artículos museológicos y materiales de conservación.
 * - Búsqueda en tiempo real por texto (título y descripción).
 * - Filtros por las 4 categorías temáticas y opción 'Todos'.
 * - Tarjetas estructuradas con imágenes, precios de prueba, stock y disponibilidad.
 * - Para el Cliente: consulta y selección de artículos (preparada para Pantalla 10).
 * - Para el Administrador: controles de alta (FAB y formulario modal), modificación,
 *   baja lógica (desactivación/reactivación) y cambio de disponibilidad.
 * - Retroalimentación visual inmediata con mensajes de confirmación y error.
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCatalogo(
    viewModel: CatalogoViewModel,
    onVolver: () -> Unit,
    onNavegarADetalleArticulo: ((Long) -> Unit)? = null,
    onNavegarACarrito: () -> Unit = {}
) {
    // 1. Suscripción consciente del ciclo de vida al StateFlow del ViewModel
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FondoOscuro,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Catálogo de Productos y Cursos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DoradoPatrimonial,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (estado.esAdministrador) "Modo Administrador" else "Patrimonio cultural e insumos",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (estado.esAdministrador) DoradoBrillante else TextoSecundario
                        )
                    }
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
                    if (estado.sesion?.rol?.equals("CLIENTE", ignoreCase = true) == true) {
                        IconButton(onClick = onNavegarACarrito) {
                            Icon(Icons.Filled.ShoppingCart, "Abrir carrito", tint = DoradoPatrimonial)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulMuseoProfundo
                )
            )
        },
        floatingActionButton = {
            if (estado.esAdministrador) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::iniciarCreacion,
                    containerColor = DoradoPatrimonial,
                    contentColor = FondoOscuro,
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Nuevo artículo") },
                    text = {
                        Text(
                            text = "Nuevo Artículo",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
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
            val articulosVisibles = estado.articulosFiltrados

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // ---- SECCIÓN SUPERIOR: BANNERS DE MENSAJES ----
                item(span = { GridItemSpan(2) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BannerMensaje(
                            mensaje = estado.mensajeConfirmacion,
                            esError = false,
                            onCerrar = viewModel::limpiarMensajeConfirmacion
                        )
                        BannerMensaje(
                            mensaje = estado.mensajeError,
                            esError = true,
                            onCerrar = viewModel::limpiarMensajeError
                        )
                    }
                }

                // ---- SECCIÓN 1: BARRA DE BÚSQUEDA ----
                item(span = { GridItemSpan(2) }) {
                    BarraBusquedaCatalogo(
                        termino = estado.terminoBusqueda,
                        onTerminoCambia = viewModel::actualizarBusqueda,
                        onLimpiar = { viewModel.actualizarBusqueda("") }
                    )
                }

                // ---- SECCIÓN 2: FILTROS POR CATEGORÍA ----
                item(span = { GridItemSpan(2) }) {
                    FilaFiltrosCategoria(
                        categoriaSeleccionada = estado.categoriaSeleccionada,
                        onSeleccionar = viewModel::seleccionarCategoria
                    )
                }

                // ---- SECCIÓN 3: CONTROL DE ADMINISTRADOR PARA INACTIVOS ----
                if (estado.esAdministrador) {
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mostrando ${articulosVisibles.size} de ${estado.articulos.size} artículos",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextoApagado
                            )
                            FilterChip(
                                selected = !estado.mostrarSoloActivos,
                                onClick = viewModel::alternarMostrarSoloActivos,
                                label = {
                                    Text(
                                        text = if (estado.mostrarSoloActivos) "Ver inactivos" else "Ocultar inactivos",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (estado.mostrarSoloActivos) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DoradoPatrimonial.copy(alpha = 0.25f),
                                    selectedLabelColor = DoradoBrillante,
                                    selectedLeadingIconColor = DoradoBrillante
                                )
                            )
                        }
                    }
                }

                // ---- ESTADO DE CARGA ----
                if (estado.cargando && estado.articulos.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = DoradoPatrimonial)
                        }
                    }
                }

                // ---- ESTADO VACÍO ----
                if (articulosVisibles.isEmpty() && !estado.cargando) {
                    item(span = { GridItemSpan(2) }) {
                        EstadoCatalogoVacio(
                            hayBusqueda = estado.terminoBusqueda.isNotBlank() || estado.categoriaSeleccionada != null,
                            onLimpiarFiltros = {
                                viewModel.actualizarBusqueda("")
                                viewModel.seleccionarCategoria(null)
                            }
                        )
                    }
                }

                // ---- LISTADO DE TARJETAS DE PRODUCTOS / CURSOS ----
                items(articulosVisibles, key = { it.id }) { articulo ->
                    TarjetaArticuloCatalogo(
                        articulo = articulo,
                        esAdministrador = estado.esAdministrador,
                        estaSeleccionado = estado.articuloSeleccionadoId == articulo.id,
                        onSeleccionar = {
                            viewModel.seleccionarArticulo(articulo.id)
                            onNavegarADetalleArticulo?.invoke(articulo.id)
                        },
                        onEditar = { viewModel.iniciarEdicion(articulo) },
                        onAlternarActivo = {
                            if (articulo.activo) {
                                viewModel.desactivarArticulo(articulo.id)
                            } else {
                                viewModel.reactivarArticulo(articulo.id)
                            }
                        },
                        onAlternarDisponibilidad = {
                            viewModel.cambiarDisponibilidad(articulo.id, !articulo.disponible)
                        }
                    )
                }

                // Espacio inferior para no tapar el último elemento con el FAB
                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }

            // ---- DIÁLOGO MODAL: FORMULARIO DE ALTA Y EDICIÓN (ADMINISTRADOR) ----
            if (estado.mostrarDialogoFormulario) {
                DialogoFormularioArticulo(
                    formulario = estado.formulario,
                    guardando = estado.guardando,
                    onTituloCambia = viewModel::cambiarTituloFormulario,
                    onDescripcionCambia = viewModel::cambiarDescripcionFormulario,
                    onCategoriaCambia = viewModel::cambiarCategoriaFormulario,
                    onPrecioCambia = viewModel::cambiarPrecioFormulario,
                    onStockCambia = viewModel::cambiarStockFormulario,
                    onImagenUrlCambia = viewModel::cambiarImagenUrlFormulario,
                    onDisponibleCambia = viewModel::cambiarDisponibleFormulario,
                    onActivoCambia = viewModel::cambiarActivoFormulario,
                    onGuardar = viewModel::guardarFormulario,
                    onCancelar = viewModel::cancelarFormulario
                )
            }
        }
    }
}

// ============================================================================
// COMPONENTES AUXILIARES DE LA INTERFAZ
// ============================================================================

/**
 * Barra de búsqueda con ícono principal y botón para limpiar.
 */
@Composable
private fun BarraBusquedaCatalogo(
    termino: String,
    onTerminoCambia: (String) -> Unit,
    onLimpiar: () -> Unit
) {
    OutlinedTextField(
        value = termino,
        onValueChange = onTerminoCambia,
        placeholder = {
            Text(
                text = "Buscar por título, temática o insumo...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoApagado
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Buscar",
                tint = DoradoPatrimonial
            )
        },
        trailingIcon = {
            if (termino.isNotEmpty()) {
                IconButton(onClick = onLimpiar) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Limpiar búsqueda",
                        tint = TextoSecundario
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DoradoPatrimonial,
            unfocusedBorderColor = DoradoPatrimonial.copy(alpha = 0.4f),
            focusedContainerColor = AzulCard,
            unfocusedContainerColor = AzulCard,
            focusedTextColor = TextoPrimario,
            unfocusedTextColor = TextoPrimario
        )
    )
}

/**
 * Carrusel horizontal de chips de filtrado temático.
 */
@Composable
private fun FilaFiltrosCategoria(
    categoriaSeleccionada: CategoriaArticulo?,
    onSeleccionar: (CategoriaArticulo?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = categoriaSeleccionada == null,
                onClick = { onSeleccionar(null) },
                label = { Text("Todos") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DoradoPatrimonial,
                    selectedLabelColor = FondoOscuro,
                    containerColor = AzulCard,
                    labelColor = TextoSecundario
                )
            )
        }
        items(CategoriaArticulo.entries.toTypedArray()) { categoria ->
            val seleccionado = categoriaSeleccionada == categoria
            FilterChip(
                selected = seleccionado,
                onClick = { onSeleccionar(categoria) },
                label = { Text(categoria.etiqueta) },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DoradoPatrimonial,
                    selectedLabelColor = FondoOscuro,
                    containerColor = AzulCard,
                    labelColor = TextoSecundario
                )
            )
        }
    }
}

/**
 * Tarjeta individual para mostrar un artículo cultural (producto o curso).
 */
@Composable
private fun TarjetaArticuloCatalogo(
    articulo: ArticuloCatalogo,
    esAdministrador: Boolean,
    estaSeleccionado: Boolean,
    onSeleccionar: () -> Unit,
    onEditar: () -> Unit,
    onAlternarActivo: () -> Unit,
    onAlternarDisponibilidad: () -> Unit
) {
    val context = LocalContext.current
    val categoriaEnum = CategoriaArticulo.desdeTexto(articulo.categoria)
    val categoriaNombre = categoriaEnum?.etiqueta ?: articulo.categoria
    val imagenRespaldo = painterResource(
        when (categoriaEnum) {
            CategoriaArticulo.CURSO -> R.drawable.catalogo_cursos
            CategoriaArticulo.PUBLICACION -> R.drawable.catalogo_publicaciones
            CategoriaArticulo.ARTICULO_MUSEOLOGICO -> R.drawable.catalogo_articulos
            CategoriaArticulo.MATERIAL_CONSERVACION -> R.drawable.catalogo_conservacion
            null -> R.drawable.catalogo_articulos
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSeleccionar)
            .border(
                width = if (estaSeleccionado) 2.dp else 1.dp,
                color = if (estaSeleccionado) DoradoBrillante else DoradoPatrimonial.copy(alpha = 0.25f),
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ---- IMAGEN Y BADGES ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
                    .background(AzulMuseoMedio)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(articulo.imagenUrl.trim().takeIf { it.isNotEmpty() })
                        .crossfade(true)
                        .build(),
                    contentDescription = articulo.titulo,
                    placeholder = imagenRespaldo,
                    error = imagenRespaldo,
                    fallback = imagenRespaldo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Etiqueta superior de Categoría
                Surface(
                    color = AzulMuseoProfundo.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = categoriaNombre,
                        color = DoradoBrillante,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Indicador de Inactivo o Disponibilidad
                if (!articulo.activo) {
                    Surface(
                        color = ErrorRojo.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "Inactivo",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                } else if (!articulo.disponible) {
                    Surface(
                        color = Terracota.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "Agotado",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // ---- INFORMACIÓN Y PRECIO ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = articulo.titulo,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextoPrimario,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )

                Text(
                    text = articulo.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatearPrecio(articulo.precio),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DoradoBrillante
                    )
                    Text(
                        text = "${articulo.stock} disp.",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (articulo.stock > 0) VerdeExito else ErrorRojo
                    )
                }

                // ---- ACCIONES DE GESTIÓN PARA EL ADMINISTRADOR ----
                if (esAdministrador) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AzulMuseoProfundo.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Editar
                        IconButton(
                            onClick = onEditar,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar artículo",
                                tint = DoradoPatrimonial,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Activar / Desactivar
                        IconButton(
                            onClick = onAlternarActivo,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (articulo.activo) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (articulo.activo) "Desactivar" else "Reactivar",
                                tint = if (articulo.activo) TextoApagado else VerdeExito,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Cambiar disponibilidad (En stock / Sin cupos)
                        IconButton(
                            onClick = onAlternarDisponibilidad,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (articulo.disponible) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = "Cambiar disponibilidad",
                                tint = if (articulo.disponible) VerdeExito else Terracota,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formulario modal de alta y modificación para el Administrador.
 */
@Composable
private fun DialogoFormularioArticulo(
    formulario: FormularioArticuloState,
    guardando: Boolean,
    onTituloCambia: (String) -> Unit,
    onDescripcionCambia: (String) -> Unit,
    onCategoriaCambia: (CategoriaArticulo) -> Unit,
    onPrecioCambia: (String) -> Unit,
    onStockCambia: (String) -> Unit,
    onImagenUrlCambia: (String) -> Unit,
    onDisponibleCambia: (Boolean) -> Unit,
    onActivoCambia: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancelar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AzulCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Encabezado del diálogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (formulario.esEdicion) "Editar Artículo" else "Nuevo Artículo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DoradoBrillante
                    )
                    IconButton(onClick = onCancelar) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = TextoSecundario
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Contenido desplazable del formulario
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. TÍTULO
                    CampoTextoFormulario(
                        etiqueta = "Título del artículo",
                        campo = formulario.titulo,
                        icono = Icons.Filled.Title,
                        intentoEnviar = formulario.intentoEnviar,
                        onValorCambia = onTituloCambia
                    )

                    // 2. CATEGORÍA (SELECTOR DE CHIPS)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Categoría temática",
                            style = MaterialTheme.typography.titleSmall,
                            color = DoradoPatrimonial
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoriaArticulo.entries.forEach { cat ->
                                FilterChip(
                                    selected = formulario.categoria == cat,
                                    onClick = { onCategoriaCambia(cat) },
                                    label = { Text(cat.etiqueta, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DoradoPatrimonial,
                                        selectedLabelColor = FondoOscuro,
                                        containerColor = SuperficieCard,
                                        labelColor = TextoSecundario
                                    )
                                )
                            }
                        }
                    }

                    // 3. DESCRIPCIÓN
                    CampoTextoFormulario(
                        etiqueta = "Descripción o temario",
                        campo = formulario.descripcion,
                        icono = Icons.Filled.Description,
                        intentoEnviar = formulario.intentoEnviar,
                        singleLine = false,
                        lineas = 3,
                        onValorCambia = onDescripcionCambia
                    )

                    // 4. PRECIO Y STOCK EN PARALELO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CampoTextoFormulario(
                                etiqueta = "Precio ($)",
                                campo = formulario.precio,
                                icono = Icons.Filled.Paid,
                                intentoEnviar = formulario.intentoEnviar,
                                tipoTeclado = KeyboardType.Decimal,
                                onValorCambia = onPrecioCambia
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CampoTextoFormulario(
                                etiqueta = "Stock / Cupos",
                                campo = formulario.stock,
                                icono = Icons.Filled.Inventory2,
                                intentoEnviar = formulario.intentoEnviar,
                                tipoTeclado = KeyboardType.Number,
                                onValorCambia = onStockCambia
                            )
                        }
                    }

                    // 5. URL DE IMAGEN
                    OutlinedTextField(
                        value = formulario.imagenUrl,
                        onValueChange = onImagenUrlCambia,
                        label = { Text("URL de la imagen (opcional)") },
                        leadingIcon = {
                            Icon(Icons.Filled.Link, contentDescription = null, tint = DoradoPatrimonial)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DoradoPatrimonial,
                            unfocusedBorderColor = DoradoPatrimonial.copy(alpha = 0.4f),
                            focusedContainerColor = SuperficieCard,
                            unfocusedContainerColor = SuperficieCard,
                            focusedTextColor = TextoPrimario,
                            unfocusedTextColor = TextoPrimario
                        )
                    )

                    // 6. TOGGLES: DISPONIBILIDAD Y ESTADO ACTIVO
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SuperficieCard, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Disponible para solicitudes", fontWeight = FontWeight.SemiBold, color = TextoPrimario)
                            Text("Los clientes podrán seleccionarlo", style = MaterialTheme.typography.bodySmall, color = TextoApagado)
                        }
                        Switch(
                            checked = formulario.disponible,
                            onCheckedChange = onDisponibleCambia,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DoradoBrillante,
                                checkedTrackColor = DoradoPatrimonial.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SuperficieCard, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Visible en catálogo (Activo)", fontWeight = FontWeight.SemiBold, color = TextoPrimario)
                            Text("Desactivar oculta el artículo para clientes", style = MaterialTheme.typography.bodySmall, color = TextoApagado)
                        }
                        Switch(
                            checked = formulario.activo,
                            onCheckedChange = onActivoCambia,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VerdeExito,
                                checkedTrackColor = VerdeExito.copy(alpha = 0.4f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Botones de acción del diálogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelar,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoSecundario)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = onGuardar,
                        enabled = !guardando,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DoradoPatrimonial,
                            contentColor = FondoOscuro
                        )
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = FondoOscuro,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (formulario.esEdicion) "Guardar Cambios" else "Crear Artículo",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Campo de texto reutilizable para el formulario que enlaza con [CampoFormulario]
 * y aplica las convenciones de validación del profesor Cristian.
 */
@Composable
private fun CampoTextoFormulario(
    etiqueta: String,
    campo: com.example.museodelosmuseos.ui.screens.catalogo.CampoFormulario,
    icono: ImageVector,
    intentoEnviar: Boolean,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    lineas: Int = 1,
    onValorCambia: (String) -> Unit
) {
    val tieneError = intentoEnviar && campo.error != null

    OutlinedTextField(
        value = campo.valor,
        onValueChange = onValorCambia,
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (tieneError) ErrorRojo else DoradoPatrimonial
            )
        },
        trailingIcon = {
            if (tieneError) {
                Icon(Icons.Filled.Error, contentDescription = "Error", tint = ErrorRojo)
            } else if (campo.esValido && campo.valor.isNotBlank()) {
                Icon(Icons.Filled.Check, contentDescription = "Válido", tint = VerdeExito)
            }
        },
        isError = tieneError,
        supportingText = if (tieneError) {
            { Text(text = campo.error ?: "", color = ErrorRojo) }
        } else null,
        singleLine = singleLine,
        minLines = lineas,
        maxLines = if (singleLine) 1 else lineas + 2,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DoradoPatrimonial,
            unfocusedBorderColor = DoradoPatrimonial.copy(alpha = 0.4f),
            errorBorderColor = ErrorRojo,
            focusedContainerColor = SuperficieCard,
            unfocusedContainerColor = SuperficieCard,
            focusedTextColor = TextoPrimario,
            unfocusedTextColor = TextoPrimario
        )
    )
}

/**
 * Banner de notificación de éxito o error con botón para cerrar.
 */
@Composable
private fun BannerMensaje(
    mensaje: String?,
    esError: Boolean,
    onCerrar: () -> Unit
) {
    AnimatedVisibility(
        visible = mensaje != null,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        if (mensaje != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (esError) ErrorRojo.copy(alpha = 0.2f) else VerdeExito.copy(alpha = 0.2f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(
                            if (esError) ErrorRojo else VerdeExito,
                            if (esError) ErrorRojo else VerdeExito
                        )
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (esError) Icons.Filled.Error else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (esError) ErrorRojo else VerdeExito,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoPrimario,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onCerrar,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = TextoSecundario,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Vista de catálogo vacío o búsqueda sin resultados.
 */
@Composable
private fun EstadoCatalogoVacio(
    hayBusqueda: Boolean,
    onLimpiarFiltros: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.SearchOff,
            contentDescription = null,
            tint = TextoApagado,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (hayBusqueda) "No se encontraron coincidencias" else "No hay artículos disponibles",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DoradoBrillante,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (hayBusqueda) "Intenta con otro término o selecciona otra categoría." else "El catálogo no cuenta con elementos activos en este momento.",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario,
            textAlign = TextAlign.Center
        )
        if (hayBusqueda) {
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onLimpiarFiltros,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DoradoPatrimonial)
            ) {
                Text("Restablecer filtros", fontSize = 12.sp)
            }
        }
    }
}

/**
 * Función utilitaria para dar formato visual al precio.
 */
private fun formatearPrecio(precio: Double): String {
    return if (precio >= 1000) {
        "$" + "%,d".format(precio.toLong()).replace(',', '.')
    } else {
        "US$ ${if (precio % 1.0 == 0.0) precio.toLong() else precio}"
    }
}
