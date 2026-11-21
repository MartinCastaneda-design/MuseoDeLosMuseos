package com.example.museodelosmuseos.ui.screens.solicitudes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.data.remote.microservicio.SolicitudCulturalDto
import com.example.museodelosmuseos.data.repository.MicroservicioRepository
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SolicitudesViewModel(
    private val repository: MuseoRepository,
    private val actorId: Long,
    private val solicitudInicial: String? = null,
    private val microservicioRepository: MicroservicioRepository = MicroservicioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SolicitudesUiState(urlServidor = microservicioRepository.obtenerBaseUrl())
    )
    private var solicitudInicialAplicada = false
    val uiState = _uiState.asStateFlow()

    init {
        observarSesionYDatosLocales()
        cargarSolicitudesRemotas()
    }

    private fun observarSesionYDatosLocales() {
        viewModelScope.launch {
            try {
                repository.observarSesion().collectLatest { sesion ->
                    solicitudInicialAplicada = false
                    val autorizado = sesion?.usuarioId == actorId && PermisosSolicitudes.puedeConsultar(sesion, actorId)
                    _uiState.update {
                        it.copy(
                            cargando = autorizado,
                            esAdministrador = autorizado && sesion?.rol?.equals("ADMINISTRADOR", true) == true,
                            error = if (autorizado) null else "No tienes acceso a este historial."
                        )
                    }
                    if (autorizado) {
                        repository.observarHistorialSolicitudes(actorId).collect { lista ->
                            val abrir = if (!solicitudInicialAplicada) solicitudInicial?.takeIf { clave -> lista.any { s -> s.clave == clave } } else null
                            solicitudInicialAplicada = true
                            _uiState.update {
                                it.copy(
                                    solicitudes = lista,
                                    cargando = false,
                                    expandida = it.expandida ?: abrir
                                )
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = "No fue posible cargar las solicitudes. Vuelve a abrir el historial.") }
            }
        }
    }

    fun alternarModo(usarMicroservicio: Boolean) {
        _uiState.update { it.copy(modoMicroservicio = usarMicroservicio, error = null, confirmacion = null) }
        if (usarMicroservicio && _uiState.value.solicitudesRemotas.isEmpty()) {
            cargarSolicitudesRemotas()
        }
    }

    fun cambiarServidor(nuevaUrl: String) {
        microservicioRepository.cambiarBaseUrl(nuevaUrl)
        _uiState.update {
            it.copy(
                urlServidor = microservicioRepository.obtenerBaseUrl(),
                mostrandoDialogoServidor = false
            )
        }
        cargarSolicitudesRemotas()
    }

    fun abrirDialogoServidor() {
        _uiState.update { it.copy(mostrandoDialogoServidor = true) }
    }

    fun cerrarDialogoServidor() {
        _uiState.update { it.copy(mostrandoDialogoServidor = false) }
    }

    fun cargarSolicitudesRemotas() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargandoRemoto = true, errorRemoto = null) }
            val resultado = microservicioRepository.listarSolicitudes()
            resultado.fold(
                onSuccess = { lista ->
                    _uiState.update {
                        it.copy(
                            solicitudesRemotas = lista,
                            cargandoRemoto = false,
                            servidorConectado = true,
                            errorRemoto = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            cargandoRemoto = false,
                            servidorConectado = false,
                            errorRemoto = "No fue posible conectar con Spring Boot en ${_uiState.value.urlServidor}: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun crearSolicitudRemota(
        titulo: String,
        descripcion: String,
        tipoSolicitud: String,
        museoNombre: String
    ) {
        if (titulo.isBlank() || descripcion.isBlank()) {
            _uiState.update { it.copy(error = "El título y la descripción son obligatorios.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            val sesion = repository.obtenerSesion()
            val dto = SolicitudCulturalDto(
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                tipoSolicitud = tipoSolicitud,
                museoNombre = museoNombre.ifBlank { "Museo Nacional de Bellas Artes" },
                solicitanteNombre = sesion?.nombre ?: "Usuario Cultural",
                solicitanteCorreo = sesion?.correo ?: "usuario@museo.org",
                estado = "PENDIENTE"
            )

            val resultado = microservicioRepository.crearSolicitud(dto)
            resultado.fold(
                onSuccess = { creada ->
                    _uiState.update {
                        it.copy(
                            solicitudesRemotas = listOf(creada) + it.solicitudesRemotas,
                            guardando = false,
                            mostrandoDialogoCrear = false,
                            confirmacion = "Solicitud #${creada.id} creada en tiempo real en Spring Boot"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            error = "Error al crear en el microservicio: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun editarRemota(solicitud: SolicitudCulturalDto) {
        _uiState.update {
            it.copy(
                editandoRemota = solicitud,
                nuevoEstadoRemoto = solicitud.estado,
                nuevaRespuestaRemota = solicitud.respuesta.orEmpty(),
                error = null,
                confirmacion = null
            )
        }
    }

    fun actualizarEstadoRemotoSeleccionado(estado: String) {
        _uiState.update { it.copy(nuevoEstadoRemoto = estado) }
    }

    fun actualizarRespuestaRemotaTexto(respuesta: String) {
        _uiState.update { it.copy(nuevaRespuestaRemota = respuesta) }
    }

    fun cancelarEdicionRemota() {
        _uiState.update { it.copy(editandoRemota = null, nuevoEstadoRemoto = null, nuevaRespuestaRemota = "") }
    }

    fun guardarActualizacionRemota() {
        val solicitud = _uiState.value.editandoRemota ?: return
        val id = solicitud.id ?: return
        val nuevoEstado = _uiState.value.nuevoEstadoRemoto ?: solicitud.estado
        val nuevaRespuesta = _uiState.value.nuevaRespuestaRemota.trim()

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            val dtoActualizado = solicitud.copy(
                estado = nuevoEstado,
                respuesta = nuevaRespuesta.ifBlank { null }
            )

            val resultado = microservicioRepository.actualizarSolicitud(id, dtoActualizado)
            resultado.fold(
                onSuccess = { actualizada ->
                    _uiState.update { estado ->
                        estado.copy(
                            solicitudesRemotas = estado.solicitudesRemotas.map { if (it.id == id) actualizada else it },
                            guardando = false,
                            editandoRemota = null,
                            confirmacion = "Solicitud #${actualizada.id} actualizada con éxito en Spring Boot"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            error = "Error al actualizar en Spring Boot: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun eliminarSolicitudRemota(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            val resultado = microservicioRepository.eliminarSolicitud(id)
            resultado.fold(
                onSuccess = {
                    _uiState.update { estado ->
                        estado.copy(
                            solicitudesRemotas = estado.solicitudesRemotas.filter { it.id != id },
                            guardando = false,
                            confirmacion = "Solicitud #$id eliminada de la base de datos de Spring Boot"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            error = "Error al eliminar en Spring Boot: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun abrirDialogoCrear() {
        _uiState.update { it.copy(mostrandoDialogoCrear = true, error = null) }
    }

    fun cerrarDialogoCrear() {
        _uiState.update { it.copy(mostrandoDialogoCrear = false) }
    }

    fun filtrar(estado: EstadoSolicitud?) {
        _uiState.update { it.copy(filtro = estado, expandida = null) }
    }

    fun expandir(clave: String) {
        _uiState.update { it.copy(expandida = if (it.expandida == clave) null else clave) }
    }

    fun editar(solicitud: SolicitudHistorial) {
        if (_uiState.value.esAdministrador && !_uiState.value.guardando) {
            _uiState.update { it.copy(editando = solicitud, nuevoEstado = null, error = null, confirmacion = null) }
        }
    }

    fun seleccionar(estado: EstadoSolicitud) {
        if (!_uiState.value.guardando) {
            _uiState.update { it.copy(nuevoEstado = estado, error = null) }
        }
    }

    fun cancelar() {
        if (!_uiState.value.guardando) {
            _uiState.update { it.copy(editando = null, nuevoEstado = null, error = null) }
        }
    }

    fun guardar() {
        val s = _uiState.value
        if (s.guardando || !s.esAdministrador) return
        val solicitud = s.editando ?: return
        val nuevo = s.nuevoEstado
        if (nuevo == null || nuevo == EstadoSolicitud.desde(solicitud.estado)) {
            _uiState.update { it.copy(error = "Selecciona un estado diferente al actual.") }
            return
        }
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                repository.cambiarEstadoSolicitud(actorId, solicitud, nuevo)
                _uiState.update {
                    it.copy(
                        editando = null,
                        nuevoEstado = null,
                        confirmacion = "Estado actualizado a ${nuevo.etiqueta}. Cambio guardado solo en este dispositivo."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "No fue posible guardar el estado.") }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(confirmacion = null, error = null) }
    }
}
