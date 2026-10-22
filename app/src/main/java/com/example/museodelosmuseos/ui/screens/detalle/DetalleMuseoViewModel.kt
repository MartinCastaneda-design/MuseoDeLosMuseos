package com.example.museodelosmuseos.ui.screens.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class DetalleMuseoViewModel(
    private val repository: MuseoRepository,
    private val museoId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleMuseoUiState())
    val uiState: StateFlow<DetalleMuseoUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {

        viewModelScope.launch {
            repository.observarMuseo(museoId).collect { museo ->
                _uiState.update { estado ->
                    estado.copy(
                        museo = museo,
                        cargando = false,
                        descripcionEditada = museo?.descripcion ?: ""
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.observarSesion().collectLatest { sesion ->
                _uiState.update { it.copy(sesion = sesion, esFavorito = false, mensaje = null) }
                if (sesion != null && sesion.rol.equals("CLIENTE", true)) {
                    repository.esFavorito(sesion.usuarioId, museoId).collect { esFav ->
                        _uiState.update { it.copy(esFavorito = esFav) }
                    }
                }
            }
        }
    }

    fun alternarFavorito() {
        val sesion = _uiState.value.sesion ?: return
        if (!sesion.rol.equals("CLIENTE", true)) return
        val nuevoEstado = !_uiState.value.esFavorito

        viewModelScope.launch {
            try {
                repository.conmutarFavorito(
                    usuarioId = sesion.usuarioId,
                    museoId = museoId,
                    marcar = nuevoEstado
                )
                _uiState.update {
                    it.copy(
                        mensaje = if (nuevoEstado) "Museo añadido a tus favoritos" else "Museo quitado de tus favoritos"
                    )
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(mensaje = e.message ?: "No fue posible actualizar favoritos.") } }
        }
    }

    fun abrirDialogoEdicion() {
        _uiState.update {
            it.copy(
                mostrarDialogoEdicion = true,
                descripcionEditada = it.museo?.descripcion ?: ""
            )
        }
    }

    fun cerrarDialogoEdicion() {
        _uiState.update { it.copy(mostrarDialogoEdicion = false) }
    }

    fun actualizarDescripcionEditada(nuevaDesc: String) {
        _uiState.update { it.copy(descripcionEditada = nuevaDesc) }
    }

    fun actualizarNotasInvestigacion(notas: String) {
        _uiState.update { it.copy(notasInvestigacion = notas) }
    }

    fun guardarCorreccionFicha() {
        val museoActual = _uiState.value.museo ?: return
        val nuevaDescripcion = _uiState.value.descripcionEditada.trim()

        if (nuevaDescripcion.isBlank()) {
            _uiState.update { it.copy(mensaje = "La descripción no puede quedar vacía") }
            return
        }

        viewModelScope.launch {
            val museoActualizado = museoActual.copy(descripcion = nuevaDescripcion)
            repository.guardarMuseo(museoActualizado)
            _uiState.update {
                it.copy(
                    mostrarDialogoEdicion = false,
                    mensaje = "Ficha museológica actualizada y guardada en Room"
                )
            }
        }
    }

    fun abrirDialogoRevision() {
        _uiState.update { it.copy(mostrarDialogoRevision = true) }
    }

    fun cerrarDialogoRevision() {
        _uiState.update { it.copy(mostrarDialogoRevision = false) }
    }

    fun autorizarPublicacion() {
        _uiState.update {
            it.copy(
                mostrarDialogoRevision = false,
                fichaPublicada = true,
                mensaje = "Publicación de la ficha oficial aprobada por el Administrador"
            )
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
}
