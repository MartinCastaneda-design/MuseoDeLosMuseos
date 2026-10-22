package com.example.museodelosmuseos.ui.screens.avisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AvisosViewModel(private val repository: MuseoRepository, private val actorId: Long) : ViewModel() {
    private val _uiState = MutableStateFlow(AvisosUiState())
    val uiState = _uiState.asStateFlow()
    init {
        viewModelScope.launch {
            try {
                repository.observarSesion().collectLatest { sesion ->
                    val error = ReglasAvisos.acceso(sesion, actorId)
                    _uiState.value = AvisosUiState(sesion = if (error == null) sesion else null, cargando = error == null, error = error)
                    if (error == null) combine(repository.observarAvisos(actorId), repository.observarMuseos()) { avisos, museos -> avisos to museos }
                        .collect { (avisos, museos) -> _uiState.update { it.copy(avisos = avisos, museos = museos, cargando = false) } }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(cargando = false, error = "No fue posible cargar los avisos. Vuelve a abrir la pantalla.") } }
        }
    }
    fun filtrar(filtro: FiltroAvisos) { _uiState.update { it.copy(filtro = filtro) } }
    fun crear() { if (_uiState.value.esAdministrador) _uiState.update { it.copy(mostrarFormulario = true, formulario = FormularioAviso(), error = null) } }
    fun cerrarFormulario() { if (!_uiState.value.guardando) _uiState.update { it.copy(mostrarFormulario = false, error = null) } }
    fun formulario(cambio: (FormularioAviso) -> FormularioAviso) {
        if (_uiState.value.esAdministrador && !_uiState.value.guardando) _uiState.update { it.copy(formulario = cambio(it.formulario), error = null) }
    }
    private fun operar(accion: suspend () -> Unit) {
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(guardando = true, error = null, confirmacion = null) }
        viewModelScope.launch {
            try { accion() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message ?: "No fue posible guardar el cambio.") } }
            finally { _uiState.update { it.copy(guardando = false) } }
        }
    }
    fun publicar() {
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(formulario = it.formulario.copy(intentoEnviar = true)) }
        val estado = _uiState.value
        val error = ReglasAvisos.validarFormulario(estado.sesion, actorId, estado.formulario)
        if (error != null) { _uiState.update { it.copy(error = error) }; return }
        operar {
            repository.publicarAviso(actorId, estado.formulario)
            _uiState.update { it.copy(mostrarFormulario = false, confirmacion = "Aviso local publicado para ${estado.formulario.audiencia.etiqueta}.") }
        }
    }
    fun lectura(id: Long, leido: Boolean) = operar {
        repository.cambiarLecturaAviso(actorId, id, leido)
        _uiState.update { it.copy(confirmacion = if (leido) "Aviso marcado como leído." else "Aviso marcado como no leído.") }
    }
    fun marcarTodos() = operar {
        repository.marcarAvisosLeidos(actorId)
        _uiState.update { it.copy(confirmacion = "Tus avisos se marcaron como leídos.") }
    }
    fun abrir(id: Long) = operar {
        val enlace = repository.abrirAviso(actorId, id)
        _uiState.update { it.copy(enlacePendiente = enlace) }
    }
    fun consumirEnlace() { _uiState.update { it.copy(enlacePendiente = null) } }
}
