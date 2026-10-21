package com.example.museodelosmuseos.ui.screens.solicitudes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SolicitudesViewModel(private val repository: MuseoRepository, private val actorId: Long) : ViewModel() {
    private val _uiState = MutableStateFlow(SolicitudesUiState())
    val uiState = _uiState.asStateFlow()
    init {
        viewModelScope.launch {
            try {
                repository.observarSesion().collectLatest { sesion ->
                    val autorizado = sesion?.usuarioId == actorId && PermisosSolicitudes.puedeConsultar(sesion, actorId)
                    _uiState.value = SolicitudesUiState(cargando = autorizado,
                        esAdministrador = autorizado && sesion?.rol?.equals("ADMINISTRADOR", true) == true,
                        error = if (autorizado) null else "No tienes acceso a este historial.")
                    if (autorizado) repository.observarHistorialSolicitudes(actorId).collect { lista ->
                        _uiState.update { it.copy(solicitudes = lista, cargando = false) }
                    }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(cargando = false, error = "No fue posible cargar las solicitudes. Vuelve a abrir el historial.") } }
        }
    }
    fun filtrar(estado: EstadoSolicitud?) { _uiState.update { it.copy(filtro = estado, expandida = null) } }
    fun expandir(clave: String) { _uiState.update { it.copy(expandida = if (it.expandida == clave) null else clave) } }
    fun editar(solicitud: SolicitudHistorial) {
        if (_uiState.value.esAdministrador && !_uiState.value.guardando)
            _uiState.update { it.copy(editando = solicitud, nuevoEstado = null, error = null, confirmacion = null) }
    }
    fun seleccionar(estado: EstadoSolicitud) { if (!_uiState.value.guardando) _uiState.update { it.copy(nuevoEstado = estado, error = null) } }
    fun cancelar() { if (!_uiState.value.guardando) _uiState.update { it.copy(editando = null, nuevoEstado = null, error = null) } }
    fun guardar() {
        val s = _uiState.value
        if (s.guardando || !s.esAdministrador) return
        val solicitud = s.editando ?: return
        val nuevo = s.nuevoEstado
        if (nuevo == null || nuevo == EstadoSolicitud.desde(solicitud.estado)) {
            _uiState.update { it.copy(error = "Selecciona un estado diferente al actual.") }; return
        }
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                repository.cambiarEstadoSolicitud(actorId, solicitud, nuevo)
                _uiState.update { it.copy(editando = null, nuevoEstado = null,
                    confirmacion = "Estado actualizado a ${nuevo.etiqueta}. Cambio guardado solo en este dispositivo.") }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message ?: "No fue posible guardar el estado.") } }
            finally { _uiState.update { it.copy(guardando = false) } }
        }
    }
}
