package com.example.museodelosmuseos.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: DashboardViewModel
 * ============================================================================
 * Propósito:
 * Gestiona el estado reactivo del Dashboard (Pantalla 4), observando la sesión
 * del usuario autenticado en Room y cargando los museos para secciones
 * destacadas y catálogo.
 * ============================================================================
 */
class DashboardViewModel(
    private val repository: MuseoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observarSesion()
        observarMuseos()
        iniciarAnimacion()
    }

    private fun observarSesion() {
        viewModelScope.launch {
            repository.observarSesion().collect { sesion ->
                _uiState.update { it.copy(sesion = sesion) }
            }
        }
    }

    private fun observarMuseos() {
        viewModelScope.launch {
            repository.observarMuseos().collect { listaMuseos ->
                _uiState.update {
                    it.copy(
                        todosLosMuseos = listaMuseos,
                        museosDestacados = listaMuseos.take(3)
                    )
                }
            }
        }
    }

    private fun iniciarAnimacion() {
        _uiState.update { it.copy(animacionLista = true) }
    }

    fun seleccionarTab(tab: TabDashboard) {
        _uiState.update { it.copy(tabSeleccionada = tab) }
    }

    fun cerrarSesion(onCerrarSesion: () -> Unit) {
        viewModelScope.launch {
            repository.cerrarSesion()
            onCerrarSesion()
        }
    }
}
