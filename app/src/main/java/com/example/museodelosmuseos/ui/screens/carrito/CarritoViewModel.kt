package com.example.museodelosmuseos.ui.screens.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CarritoViewModel(private val repository: MuseoRepository, private val usuarioId: Long) : ViewModel() {
    private val _uiState = MutableStateFlow(CarritoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observarSesion().collectLatest { sesion ->
                    val error = ReglasSolicitudArticulo.validarAcceso(sesion, usuarioId)
                    _uiState.value = CarritoUiState(cargando = error == null, error = error)
                    if (error == null) repository.observarLineasCarrito(usuarioId).collect { lineas ->
                        _uiState.update { it.copy(lineas = lineas, cargando = false) }
                    }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(cargando = false, error = "No fue posible cargar el carrito. Vuelve a abrirlo.") } }
        }
    }

    private fun editar(cambio: (CarritoUiState) -> CarritoUiState) {
        if (!_uiState.value.guardando) _uiState.update { cambio(it).copy(revision = null, error = null) }
    }
    fun destino(valor: DestinoEnvio) = editar { it.copy(destino = valor) }
    fun modalidad(valor: ModalidadEnvio) = editar { it.copy(modalidad = valor) }
    fun ubicacion(valor: String) = editar { it.copy(ubicacion = valor) }
    fun direccion(valor: String) = editar { it.copy(direccion = valor) }
    fun cancelarRevision() = editar { it.copy(revision = null) }

    private fun operar(accion: suspend () -> Unit) {
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(guardando = true, error = null, confirmacion = null) }
        viewModelScope.launch {
            try { accion() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message ?: "No fue posible guardar los cambios.", revision = null) } }
            finally { _uiState.update { it.copy(guardando = false) } }
        }
    }

    fun cantidad(id: Long, cantidad: Int) = operar { repository.cambiarCantidadCarrito(usuarioId, id, cantidad) }
    fun eliminar(id: Long) = operar { repository.eliminarDelCarrito(usuarioId, id) }

    fun revisar() {
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(intentoEnviar = true, error = null, confirmacion = null) }
        val s = _uiState.value
        val error = when {
            s.lineas.isEmpty() -> "Agrega productos al carrito."
            s.destino == null -> "Selecciona el destino del envío."
            s.modalidad == null -> "Selecciona una modalidad de envío."
            s.errorUbicacion != null -> s.errorUbicacion
            s.errorDireccion != null -> s.errorDireccion
            else -> null
        }
        if (error != null) { _uiState.update { it.copy(error = error) }; return }
        try {
            val cotizacion = CalculoCarrito.cotizar(s.lineas, s.destino!!, s.modalidad!!)
            _uiState.update { it.copy(revision = RevisionPedido(cotizacion, s.destino, s.modalidad, s.ubicacion, s.direccion)) }
        } catch (e: Exception) { _uiState.update { it.copy(error = e.message ?: "No fue posible calcular el total.") } }
    }

    fun confirmar() {
        val revision = _uiState.value.revision ?: return
        operar {
            val id = repository.confirmarCarrito(usuarioId, revision.destino, revision.modalidad,
                revision.ubicacion, revision.direccion, revision.cotizacion)
            _uiState.update { it.copy(revision = null, intentoEnviar = false,
                confirmacion = "Solicitud ${id.take(8)} registrada en este dispositivo. Estado: Pendiente local. Total: $${revision.cotizacion.total} CLP.") }
        }
    }
}
