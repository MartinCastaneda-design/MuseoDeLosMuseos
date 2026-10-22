package com.example.museodelosmuseos.ui.screens.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import com.example.museodelosmuseos.data.model.CategoriaArticulo
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: CatalogoViewModel
 * ============================================================================
 * Propósito:
 * Gestiona el estado reactivo, la sincronización con Room (SQLite), la lógica
 * de filtrado y búsqueda, la autorización por roles y las validaciones del
 * formulario para la Pantalla 9: Catálogo de Productos y Cursos.
 *
 * Patrones implementados (según el estándar del profesor Cristian):
 * - MVVM (Model-View-ViewModel): Desacopla la lógica de negocio y validación de la UI.
 * - Patrón Backing Property: Encapsula el estado con _uiState privado y uiState público inmutable.
 * - Flujo Unidireccional de Datos (UDF): Modificaciones seguras mediante .update y .copy().
 * - Single Source of Truth: Emite estados reactivos centralizados con StateFlow.
 * ============================================================================
 */
class CatalogoViewModel(
    private val repository: MuseoRepository
) : ViewModel() {

    // ============================================================================
    // PATRÓN BACKING PROPERTY (Propiedad de respaldo)
    // 1. Privada con guion bajo (_): Solo este ViewModel puede mutar el estado.
    // 2. Pública sin guion bajo: La interfaz de Compose solo puede LEER el estado.
    // ============================================================================
    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    private var observacionArticulosJob: Job? = null

    init {
        observarSesionUsuario()
    }

    // --- 1. OBSERVACIÓN REACTIVA DE DATOS Y ROLES ---

    /**
     * Monitorea la sesión activa del usuario para adaptar las capacidades de la pantalla.
     * Si el rol es Cliente, se limita la consulta a artículos activos.
     * Si el rol es Administrador, se habilita la gestión completa y vista de inactivos.
     */
    private fun observarSesionUsuario() {
        viewModelScope.launch {
            repository.observarSesion().collect { sesion ->
                observacionArticulosJob?.cancel()
                val esAdmin = sesion?.rol?.equals("ADMINISTRADOR", ignoreCase = true) == true
                val puedeAcceder = esAdmin || sesion?.rol?.equals("CLIENTE", ignoreCase = true) == true
                _uiState.update { actual ->
                    CatalogoUiState(
                        sesion = sesion,
                        mostrarSoloActivos = if (esAdmin) actual.mostrarSoloActivos else true
                    )
                }
                if (puedeAcceder) iniciarObservacionCatalogo(esAdmin)
            }
        }
    }

    /**
     * Conecta con Room a través del repositorio.
     * Si el usuario es Administrador consulta todos los registros para su gestión;
     * si es Cliente consulta únicamente los activos desde la base de datos.
     */
    private fun iniciarObservacionCatalogo(esAdmin: Boolean) {
        observacionArticulosJob?.cancel()
        observacionArticulosJob = viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            repository.observarCatalogo(soloActivos = !esAdmin)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensajeError = "Error al consultar el catálogo: ${error.localizedMessage ?: "error desconocido"}"
                        )
                    }
                }
                .collect { articulosObtenidos ->
                    _uiState.update {
                        if (it.sesion?.rol?.equals("ADMINISTRADOR", ignoreCase = true) == true ||
                            it.sesion?.rol?.equals("CLIENTE", ignoreCase = true) == true) it.copy(
                            articulos = articulosObtenidos,
                            cargando = false
                        ) else it
                    }
                }
        }
    }

    // --- 2. BÚSQUEDA Y FILTRADO REACTIVO ---

    /**
     * Actualiza el término de búsqueda ingresado por el usuario.
     */
    fun actualizarBusqueda(termino: String) {
        _uiState.update { it.copy(terminoBusqueda = termino) }
    }

    /**
     * Selecciona una categoría cultural para filtrar o null para ver todas.
     */
    fun seleccionarCategoria(categoria: CategoriaArticulo?) {
        _uiState.update { it.copy(categoriaSeleccionada = categoria) }
    }

    /**
     * Alterna la visualización de artículos inactivos (exclusivo para Administrador).
     */
    fun alternarMostrarSoloActivos() {
        if (!_uiState.value.esAdministrador) return
        _uiState.update { it.copy(mostrarSoloActivos = !it.mostrarSoloActivos) }
    }

    // --- 3. SELECCIÓN DE ARTÍCULO (PARA PANTALLA 10: DETALLE Y COMPRA) ---

    /**
     * Registra el artículo seleccionado por su ID.
     */
    fun seleccionarArticulo(id: Long) {
        _uiState.update { it.copy(articuloSeleccionadoId = id) }
    }

    /**
     * Restablece la selección de artículo.
     */
    fun limpiarSeleccionArticulo() {
        _uiState.update { it.copy(articuloSeleccionadoId = null) }
    }

    // --- 4. ACCIONES DE GESTIÓN PARA ADMINISTRADOR ---

    /**
     * Prepara el formulario para el alta de un nuevo artículo cultural.
     */
    fun iniciarCreacion() {
        if (!_uiState.value.esAdministrador) return
        _uiState.update {
            it.copy(
                formulario = FormularioArticuloState(),
                mostrarDialogoFormulario = true,
                mensajeError = null
            )
        }
    }

    /**
     * Carga los datos de un artículo existente en el formulario para su edición.
     */
    fun iniciarEdicion(articulo: ArticuloCatalogo) {
        if (!_uiState.value.esAdministrador) return
        val categoriaEnum = CategoriaArticulo.desdeTexto(articulo.categoria) ?: CategoriaArticulo.CURSO
        val precioTexto = if (articulo.precio % 1.0 == 0.0) {
            articulo.precio.toLong().toString()
        } else {
            articulo.precio.toString()
        }

        _uiState.update {
            it.copy(
                formulario = FormularioArticuloState(
                    id = articulo.id,
                    titulo = CampoFormulario(valor = articulo.titulo),
                    descripcion = CampoFormulario(valor = articulo.descripcion),
                    categoria = categoriaEnum,
                    precio = CampoFormulario(valor = precioTexto),
                    stock = CampoFormulario(valor = articulo.stock.toString()),
                    imagenUrl = articulo.imagenUrl,
                    disponible = articulo.disponible,
                    activo = articulo.activo,
                    intentoEnviar = false
                ),
                mostrarDialogoFormulario = true,
                mensajeError = null
            )
        }
    }

    /**
     * Cierra el formulario descartando los cambios temporales.
     */
    fun cancelarFormulario() {
        _uiState.update {
            it.copy(
                mostrarDialogoFormulario = false,
                formulario = FormularioArticuloState()
            )
        }
    }

    /**
     * Realiza la baja lógica de un artículo en SQLite/Room.
     * Preserva la integridad de compras y registros históricos.
     */
    fun desactivarArticulo(id: Long) {
        if (!_uiState.value.esAdministrador) return
        viewModelScope.launch {
            try {
                repository.desactivarArticuloCatalogo(id)
                _uiState.update {
                    it.copy(mensajeConfirmacion = "Artículo desactivado del catálogo correctamente.")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(mensajeError = "No fue posible desactivar el artículo: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Reactiva un artículo previamente desactivado.
     */
    fun reactivarArticulo(id: Long) {
        if (!_uiState.value.esAdministrador) return
        viewModelScope.launch {
            try {
                repository.reactivarArticuloCatalogo(id)
                _uiState.update {
                    it.copy(mensajeConfirmacion = "Artículo reactivado en el catálogo correctamente.")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(mensajeError = "No fue posible reactivar el artículo: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Modifica la disponibilidad para solicitudes o inscripciones de clientes.
     */
    fun cambiarDisponibilidad(id: Long, disponible: Boolean) {
        if (!_uiState.value.esAdministrador) return
        viewModelScope.launch {
            try {
                repository.cambiarDisponibilidadArticulo(id, disponible)
                val texto = if (disponible) "disponible para solicitudes" else "marcado como no disponible"
                _uiState.update {
                    it.copy(mensajeConfirmacion = "Artículo $texto.")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(mensajeError = "No fue posible cambiar la disponibilidad: ${e.localizedMessage}")
                }
            }
        }
    }

    // --- 5. ENTRADAS Y VALIDACIONES REACTIVAS DEL FORMULARIO ---

    fun cambiarTituloFormulario(valor: String) {
        _uiState.update { estado ->
            val error = validarTitulo(valor)
            estado.copy(
                formulario = estado.formulario.copy(
                    titulo = CampoFormulario(valor, error)
                )
            )
        }
    }

    fun cambiarDescripcionFormulario(valor: String) {
        _uiState.update { estado ->
            val error = validarDescripcion(valor)
            estado.copy(
                formulario = estado.formulario.copy(
                    descripcion = CampoFormulario(valor, error)
                )
            )
        }
    }

    fun cambiarCategoriaFormulario(categoria: CategoriaArticulo) {
        _uiState.update { estado ->
            estado.copy(
                formulario = estado.formulario.copy(categoria = categoria)
            )
        }
    }

    /**
     * Sanitiza la entrada de precio aceptando números enteros y decimales.
     */
    fun cambiarPrecioFormulario(valor: String) {
        val sanitizado = valor.trim().replace(',', '.')
        if (sanitizado.isEmpty() || sanitizado.matches(Regex("""^\d*(\.\d{0,2})?$"""))) {
            _uiState.update { estado ->
                val error = validarPrecio(sanitizado)
                estado.copy(
                    formulario = estado.formulario.copy(
                        precio = CampoFormulario(sanitizado, error)
                    )
                )
            }
        }
    }

    /**
     * Sanitiza el stock aceptando únicamente dígitos enteros positivos.
     */
    fun cambiarStockFormulario(valor: String) {
        val sanitizado = valor.trim()
        if (sanitizado.isEmpty() || (sanitizado.all(Char::isDigit) && sanitizado.length <= 5)) {
            _uiState.update { estado ->
                val error = validarStock(sanitizado)
                estado.copy(
                    formulario = estado.formulario.copy(
                        stock = CampoFormulario(sanitizado, error)
                    )
                )
            }
        }
    }

    fun cambiarImagenUrlFormulario(valor: String) {
        _uiState.update { estado ->
            estado.copy(
                formulario = estado.formulario.copy(imagenUrl = valor.trim())
            )
        }
    }

    fun cambiarDisponibleFormulario(disponible: Boolean) {
        _uiState.update { estado ->
            estado.copy(
                formulario = estado.formulario.copy(disponible = disponible)
            )
        }
    }

    fun cambiarActivoFormulario(activo: Boolean) {
        _uiState.update { estado ->
            estado.copy(
                formulario = estado.formulario.copy(activo = activo)
            )
        }
    }

    /**
     * Realiza un barrido completo de validaciones sincronizadas y persiste
     * el artículo en Room si todos los campos son conformes.
     */
    fun guardarFormulario() {
        if (!_uiState.value.esAdministrador) {
            _uiState.update { it.copy(mensajeError = "Operación no autorizada para el rol actual.") }
            return
        }

        val actual = _uiState.value.formulario
        val tituloValidado = actual.titulo.copy(error = validarTitulo(actual.titulo.valor))
        val descValidada = actual.descripcion.copy(error = validarDescripcion(actual.descripcion.valor))
        val precioValidado = actual.precio.copy(error = validarPrecio(actual.precio.valor))
        val stockValidado = actual.stock.copy(error = validarStock(actual.stock.valor))

        val formularioValidado = actual.copy(
            titulo = tituloValidado,
            descripcion = descValidada,
            precio = precioValidado,
            stock = stockValidado,
            intentoEnviar = true
        )

        _uiState.update { it.copy(formulario = formularioValidado) }

        if (!formularioValidado.formularioValido) {
            return
        }

        _uiState.update { it.copy(guardando = true, mensajeError = null) }

        viewModelScope.launch {
            try {
                val precioDouble = formularioValidado.precio.valor.toDouble()
                val stockInt = formularioValidado.stock.valor.toInt()
                val imagenFinal = formularioValidado.imagenUrl.ifBlank {
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Museo_Colonial_Bogot%C3%A1.jpg/800px-Museo_Colonial_Bogot%C3%A1.jpg"
                }

                val articulo = ArticuloCatalogo(
                    id = formularioValidado.id,
                    titulo = formularioValidado.titulo.valor.trim(),
                    descripcion = formularioValidado.descripcion.valor.trim(),
                    categoria = formularioValidado.categoria.name,
                    precio = precioDouble,
                    imagenUrl = imagenFinal,
                    disponible = formularioValidado.disponible,
                    activo = formularioValidado.activo,
                    stock = stockInt
                )

                if (formularioValidado.esEdicion) {
                    repository.actualizarArticuloCatalogo(articulo)
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mostrarDialogoFormulario = false,
                            formulario = FormularioArticuloState(),
                            mensajeConfirmacion = "Artículo '${articulo.titulo}' actualizado correctamente."
                        )
                    }
                } else {
                    val nuevoId = repository.guardarArticuloCatalogo(articulo)
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mostrarDialogoFormulario = false,
                            formulario = FormularioArticuloState(),
                            mensajeConfirmacion = "Artículo agregado al catálogo con código #$nuevoId."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeError = "No fue posible guardar el artículo: ${e.localizedMessage ?: "error desconocido"}"
                    )
                }
            }
        }
    }

    // --- 6. RETROALIMENTACIÓN VISUAL ---

    fun limpiarMensajeConfirmacion() {
        _uiState.update { it.copy(mensajeConfirmacion = null) }
    }

    fun limpiarMensajeError() {
        _uiState.update { it.copy(mensajeError = null) }
    }

    // --- 7. REGLAS DE NEGOCIO PRIVADAS (Validaciones) ---

    private fun validarTitulo(valor: String): String? = when {
        valor.isBlank() -> "El título del artículo es obligatorio"
        valor.trim().length < 3 -> "Escribe al menos 3 caracteres para el título"
        else -> null
    }

    private fun validarDescripcion(valor: String): String? = when {
        valor.isBlank() -> "La descripción es obligatoria"
        valor.trim().length < 10 -> "Escribe al menos 10 caracteres explicativos"
        else -> null
    }

    private fun validarPrecio(valor: String): String? {
        if (valor.isBlank()) return "El precio es obligatorio"
        val numero = valor.toDoubleOrNull()
        return when {
            numero == null -> "Ingresa un precio numérico válido"
            numero <= 0.0 -> "El precio debe ser mayor a cero"
            else -> null
        }
    }

    private fun validarStock(valor: String): String? {
        if (valor.isBlank()) return "El stock de unidades es obligatorio"
        val numero = valor.toIntOrNull()
        return when {
            numero == null -> "Ingresa un número entero válido"
            numero < 0 -> "El stock no puede ser negativo"
            else -> null
        }
    }
}
