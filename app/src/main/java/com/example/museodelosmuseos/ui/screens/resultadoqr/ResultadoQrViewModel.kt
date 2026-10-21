package com.example.museodelosmuseos.ui.screens.resultadoqr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.repository.MuseoRepository
import com.example.museodelosmuseos.ui.navigation.DestinoPantalla
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResultadoQrViewModel(
    private val repository: MuseoRepository,
    private val datosResultado: DestinoPantalla.ResultadoQr
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ResultadoQrUiState(
            codigo = datosResultado.codigo,
            titulo = datosResultado.titulo,
            descripcion = datosResultado.descripcion,
            esValido = datosResultado.esValido,
            museoId = datosResultado.museoId
        )
    )
    val uiState: StateFlow<ResultadoQrUiState> = _uiState.asStateFlow()

    init {
        cargarDatosYAnimacion()
    }

    private fun cargarDatosYAnimacion() {
        val museoId = datosResultado.museoId

        if (museoId != null) {
            viewModelScope.launch {
                repository.observarMuseo(museoId).collect { museoEncontrado ->
                    _uiState.update { estado ->
                        estado.copy(
                            museo = museoEncontrado,
                            titulo = museoEncontrado?.nombre ?: estado.titulo,
                            descripcion = museoEncontrado?.descripcion ?: estado.descripcion
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            val sesion = repository.obtenerSesion()
            val usuarioId = sesion?.usuarioId ?: 1L
            if (datosResultado.esValido && datosResultado.codigo.isNotBlank()) {
                repository.registrarLecturaQr(
                    usuarioId = usuarioId,
                    codigo = datosResultado.codigo,
                    titulo = datosResultado.titulo.ifBlank { "Museo Patrimonial" },
                    descripcion = datosResultado.descripcion,
                    url = _uiState.value.museo?.urlVisitaVirtual ?: ""
                )
            }
        }

        viewModelScope.launch {
            delay(150)
            _uiState.update { it.copy(animacionLista = true) }
        }
    }
}
