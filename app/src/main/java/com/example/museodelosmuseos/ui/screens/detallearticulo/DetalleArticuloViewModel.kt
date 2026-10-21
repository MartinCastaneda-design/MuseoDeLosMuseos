package com.example.museodelosmuseos.ui.screens.detallearticulo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.model.ReglasSolicitudArticulo
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetalleArticuloViewModel(
    private val repository: MuseoRepository,
    private val articuloId: Long,
    private val usuarioId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetalleArticuloUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observarSesion().collectLatest { sesion ->
                    val errorAcceso = ReglasSolicitudArticulo.validarAcceso(sesion, usuarioId)
                    _uiState.value = DetalleArticuloUiState(cargando = errorAcceso == null, error = errorAcceso)
                    if (errorAcceso == null) {
                        combine(
                            repository.observarArticuloCatalogo(articuloId),
                            repository.observarCarrito(usuarioId),
                            repository.observarSolicitudesInformacion(usuarioId)
                        ) { articulo, carrito, solicitudes ->
                            Triple(articulo, carrito.firstOrNull { it.articuloId == articuloId }?.cantidad ?: 0,
                                solicitudes.any { it.articuloId == articuloId })
                        }.collect { (articulo, cantidad, solicitada) ->
                            _uiState.update { it.copy(articulo = articulo, enCarrito = cantidad,
                                solicitudRegistrada = solicitada, cargando = false) }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = "No fue posible cargar el detalle. Vuelve al catálogo e intenta otra vez.") }
            }
        }
    }

    fun cambiarCantidad(valor: String) {
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(cantidad = valor, cantidadEditada = true, confirmacion = null) }
    }

    fun cambiarConsulta(valor: String) {
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(consulta = valor, consultaEditada = true, confirmacion = null) }
    }

    fun enviar() {
        if (_uiState.value.guardando || _uiState.value.cargando) return
        _uiState.update { it.copy(intentoEnviar = true, confirmacion = null, error = null) }
        val estado = _uiState.value
        val error = estado.errorDisponibilidad ?: if (estado.esCurso) {
            if (estado.solicitudRegistrada) "Ya tienes una solicitud para este curso." else estado.errorConsulta
        } else estado.errorCantidad
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                if (estado.esCurso) {
                    repository.solicitarInformacion(usuarioId, articuloId, estado.consulta)
                } else {
                    repository.agregarAlCarrito(usuarioId, articuloId, estado.cantidad.toInt())
                }
                _uiState.update { it.copy(guardando = false, intentoEnviar = false,
                    cantidadEditada = false, consultaEditada = false, cantidad = "1",
                    confirmacion = if (estado.esCurso) "Consulta guardada en este dispositivo."
                        else "${estado.cantidad} unidad(es) agregadas al carrito.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = e.message ?: "No fue posible guardar. Intenta nuevamente.") }
            }
        }
    }
}
