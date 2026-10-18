package com.example.museodelosmuseos.ui.screens.escaner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.repository.MuseoRepository
import com.example.museodelosmuseos.ui.navigation.DestinoPantalla
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: EscanerQrViewModel
 * ============================================================================
 * Propósito:
 * Gestiona el escaneo y validación de códigos QR de museos, exposiciones o
 * piezas patrimoniales.
 *
 * Cumple con los requerimientos de la pauta:
 * - Valida el código y determina qué ficha debe abrir.
 * - El Investigador puede probar los códigos de los registros que administra.
 * - Las lecturas correctas se agregan al historial local en SQLite (Room),
 *   evitando registros duplicados innecesarios.
 * ============================================================================
 */
class EscanerQrViewModel(
    private val repository: MuseoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EscanerQrUiState())
    val uiState: StateFlow<EscanerQrUiState> = _uiState.asStateFlow()

    init {
        observarSesionEHistorial()
        cargarCodigosParaPrueba()
    }

    private fun observarSesionEHistorial() {
        viewModelScope.launch {
            repository.observarSesion().collect { sesion ->
                _uiState.update { it.copy(sesion = sesion) }
                if (sesion != null) {
                    repository.observarHistorialQr(sesion.usuarioId).collect { historial ->
                        _uiState.update { it.copy(historialLecturas = historial) }
                    }
                }
            }
        }
    }

    private fun cargarCodigosParaPrueba() {
        viewModelScope.launch {
            repository.observarMuseos().collect { listaMuseos ->
                val codigos = listaMuseos.map { it.codigoQr }.distinct()
                _uiState.update { it.copy(codigosParaPrueba = codigos) }
            }
        }
    }

    /**
     * Valida el código QR leído por la cámara o seleccionado por el Investigador.
     * Determina la ficha correspondiente y guarda la lectura en SQLite (Room).
     */
    fun procesarCodigoQr(codigoLeido: String, onNavegarAResultado: (DestinoPantalla.ResultadoQr) -> Unit) {
        val codigoLimpio = codigoLeido.trim()
        if (codigoLimpio.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Código QR vacío") }
            return
        }

        val sesion = _uiState.value.sesion
        val usuarioId = sesion?.usuarioId ?: 1L

        viewModelScope.launch {
            _uiState.update { it.copy(procesando = true, mensajeError = null) }

            // 1. Validar el código contra la base de datos de museos
            val museoEncontrado = repository.buscarMuseoPorQr(codigoLimpio)

            if (museoEncontrado != null) {
                // Lectura correcta: agregar al historial SQLite evitando duplicados (vía REPLACE en Room)
                repository.registrarLecturaQr(
                    usuarioId = usuarioId,
                    codigo = codigoLimpio,
                    titulo = museoEncontrado.nombre,
                    descripcion = "${museoEncontrado.ciudad}, ${museoEncontrado.pais} • ${museoEncontrado.clasificacion}",
                    url = museoEncontrado.urlVisitaVirtual
                )

                _uiState.update { it.copy(procesando = false) }

                // Navegar a Resultado QR (Pantalla 8) con la ficha identificada
                onNavegarAResultado(
                    DestinoPantalla.ResultadoQr(
                        codigo = codigoLimpio,
                        titulo = museoEncontrado.nombre,
                        descripcion = museoEncontrado.descripcion,
                        esValido = true,
                        museoId = museoEncontrado.id
                    )
                )
            } else {
                // Código no reconocido
                _uiState.update { it.copy(procesando = false) }

                onNavegarAResultado(
                    DestinoPantalla.ResultadoQr(
                        codigo = codigoLimpio,
                        titulo = "Código No Registrado",
                        descripcion = "El código QR escaneado no corresponde a ningún museo o pieza del catálogo patrimonial actual.",
                        esValido = false,
                        museoId = null
                    )
                )
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(mensajeError = null) }
    }
}
