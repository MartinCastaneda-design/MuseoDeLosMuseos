package com.example.museodelosmuseos.ui.screens.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.model.ReglasFavoritos
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FavoritosViewModel(private val repository: MuseoRepository, private val usuarioId: Long) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritosUiState())
    val uiState = _uiState.asStateFlow()
    init {
        viewModelScope.launch {
            try {
                repository.observarSesion().collectLatest { sesion ->
                    val error = ReglasFavoritos.validarAcceso(sesion, usuarioId)
                    _uiState.value = FavoritosUiState(cargando = error == null, error = error)
                    if (error == null) repository.observarFavoritos(usuarioId).collect { museos ->
                        _uiState.update { it.copy(museos = museos, cargando = false) }
                    }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(cargando = false, error = "No fue posible cargar tus favoritos. Vuelve a abrir la colección.") } }
        }
    }
    fun buscar(valor: String) { _uiState.update { it.copy(busqueda = valor) } }
    fun pedirQuitar(museo: Museo) { if (!_uiState.value.guardando) _uiState.update { it.copy(porQuitar = museo, error = null) } }
    fun cancelar() { if (!_uiState.value.guardando) _uiState.update { it.copy(porQuitar = null) } }
    fun confirmarQuitar() {
        val museo = _uiState.value.porQuitar ?: return
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(guardando = true, error = null, confirmacion = null) }
        viewModelScope.launch {
            try {
                repository.conmutarFavorito(usuarioId, museo.id, false)
                _uiState.update { it.copy(porQuitar = null, confirmacion = "${museo.nombre} se quitó de tus favoritos.") }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(porQuitar = null, error = e.message ?: "No fue posible quitar el museo.") } }
            finally { _uiState.update { it.copy(guardando = false) } }
        }
    }
    fun abrirRecorrido(museo: Museo) {
        if (ReglasFavoritos.enlaceValido(museo.urlVisitaVirtual))
            _uiState.update { it.copy(enlacePendiente = museo.urlVisitaVirtual.trim(), error = null) }
        else _uiState.update { it.copy(error = "Este museo no tiene un enlace válido al recorrido virtual.") }
    }
    fun consumirEnlace(error: Boolean) { _uiState.update { it.copy(enlacePendiente = null,
        error = if (error) "No se pudo abrir el recorrido. Comprueba que haya un navegador disponible." else it.error) } }
}
