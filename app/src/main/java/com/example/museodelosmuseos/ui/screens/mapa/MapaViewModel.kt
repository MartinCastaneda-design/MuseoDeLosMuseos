package com.example.museodelosmuseos.ui.screens.mapa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: MapaViewModel
 * ============================================================================
 * Propósito:
 * Gestiona el mapa interactivo de Iberoamérica con marcadores de museos,
 * consulta la API REST ficticia sincronizando en SQLite, y filtra los marcadores
 * según el país y proyecto seleccionado.
 * ============================================================================
 */
class MapaViewModel(
    private val repository: MuseoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapaUiState())
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    init {
        observarMuseosLocales()
        sincronizarConApiFicticia()
    }

    private fun observarMuseosLocales() {
        viewModelScope.launch {
            repository.observarMuseos().collect { listaMuseos ->
                val paises = listOf("Todos") + listaMuseos.map { it.pais }.distinct().sorted()
                val proyectos = listOf("Todos") + listaMuseos.map { it.clasificacion }.distinct().sorted()

                _uiState.update { estado ->
                    estado.copy(
                        todosLosMuseos = listaMuseos,
                        listaPaises = paises,
                        listaProyectos = proyectos,
                        museosFiltrados = filtrarMuseos(
                            museos = listaMuseos,
                            pais = estado.paisSeleccionado,
                            proyecto = estado.proyectoSeleccionado
                        )
                    )
                }
            }
        }
    }

    /**
     * Consulta la API REST ficticia y actualiza la caché local SQLite en Room
     */
    fun sincronizarConApiFicticia() {
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizando = true) }
            val resultado = repository.sincronizarMuseosRemotos()
            _uiState.update { estado ->
                estado.copy(
                    sincronizando = false,
                    mensaje = if (resultado.isSuccess) {
                        "Datos sincronizados desde API REST ficticia"
                    } else {
                        "Usando datos locales de SQLite en caché"
                    }
                )
            }
        }
    }

    fun filtrarPorPais(pais: String) {
        _uiState.update { estado ->
            val filtrados = filtrarMuseos(estado.todosLosMuseos, pais, estado.proyectoSeleccionado)
            estado.copy(
                paisSeleccionado = pais,
                museosFiltrados = filtrados,
                // Si el museo seleccionado ya no está en el filtro, deseleccionarlo
                museoSeleccionado = if (estado.museoSeleccionado?.pais == pais || pais == "Todos") estado.museoSeleccionado else null
            )
        }
    }

    fun filtrarPorProyecto(proyecto: String) {
        _uiState.update { estado ->
            val filtrados = filtrarMuseos(estado.todosLosMuseos, estado.paisSeleccionado, proyecto)
            estado.copy(
                proyectoSeleccionado = proyecto,
                museosFiltrados = filtrados,
                museoSeleccionado = if (estado.museoSeleccionado?.clasificacion == proyecto || proyecto == "Todos") estado.museoSeleccionado else null
            )
        }
    }

    fun seleccionarMuseo(museo: Museo?) {
        _uiState.update { it.copy(museoSeleccionado = museo) }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    private fun filtrarMuseos(museos: List<Museo>, pais: String, proyecto: String): List<Museo> {
        return museos.filter { museo ->
            val coincidePais = pais == "Todos" || museo.pais.equals(pais, ignoreCase = true)
            val coincideProyecto = proyecto == "Todos" || museo.clasificacion.equals(proyecto, ignoreCase = true)
            coincidePais && coincideProyecto
        }
    }
}
