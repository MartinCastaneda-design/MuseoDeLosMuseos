package com.example.museodelosmuseos.ui.screens.perfil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museodelosmuseos.data.local.ArchivosAvatar
import com.example.museodelosmuseos.data.local.entity.PreferenciasUsuario
import com.example.museodelosmuseos.data.model.ReglasPerfil
import com.example.museodelosmuseos.data.repository.MuseoRepository
import com.example.museodelosmuseos.ui.screens.registro.CampoFormulario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PerfilViewModel(private val repository: MuseoRepository, private val usuarioId: Long,
    private val archivos: ArchivosAvatar) : ViewModel() {
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState = _uiState.asStateFlow()
    private var borradorAvatar: String? = null
    private suspend fun limpiarBorrador(conservar: String? = null) {
        val pendiente = borradorAvatar
        borradorAvatar = null
        if (pendiente != null && pendiente != conservar) archivos.borrarBorrador(pendiente, usuarioId)
    }
    fun descartarBorrador() { viewModelScope.launch { limpiarBorrador() } }
    init {
        viewModelScope.launch {
            repository.observarSesion().collectLatest { sesion ->
                if (ReglasPerfil.acceso(sesion, usuarioId) != null) {
                    limpiarBorrador()
                    _uiState.value = PerfilUiState(cargando = false, error = "La sesión ya no está activa.")
                } else combine(repository.observarPerfilActivo(usuarioId), repository.observarPreferencias(usuarioId)) { usuario, preferencias ->
                    usuario to preferencias
                }.catch { _uiState.update { it.copy(cargando = false, error = "No fue posible cargar el perfil.") } }
                    .collect { (usuario, preferencias) ->
                        if (usuario == null) _uiState.value = PerfilUiState(cargando = false)
                        else _uiState.update { anterior ->
                            if (anterior.usuario == null || !anterior.modificado) anterior.copy(usuario = usuario,
                                nombre = CampoFormulario(usuario.nombre), correo = CampoFormulario(usuario.correo), avatar = usuario.avatar,
                                indicadorAvisos = preferencias?.indicadorAvisos ?: true, textoGrande = preferencias?.textoGrande ?: false, cargando = false)
                            else anterior.copy(usuario = usuario, cargando = false)
                        }
                    }
            }
        }
    }
    fun nombre(valor: String) { editar { it.copy(nombre = CampoFormulario(valor, ReglasPerfil.nombre(valor))) } }
    fun correo(valor: String) { editar { it.copy(correo = CampoFormulario(valor, ReglasPerfil.correo(valor))) } }
    fun indicador(valor: Boolean) { editar { it.copy(indicadorAvisos = valor) } }
    fun textoGrande(valor: Boolean) { editar { it.copy(textoGrande = valor) } }
    private fun editar(cambio: (PerfilUiState) -> PerfilUiState) {
        if (!_uiState.value.guardando && _uiState.value.usuario != null)
            _uiState.update { cambio(it).copy(modificado = true, confirmacion = null, error = null) }
    }
    fun mensaje(texto: String) { _uiState.update { it.copy(error = texto, confirmacion = null) } }
    fun avatarPredeterminado() { editar { it.copy(avatar = "avatar_1") } }
    fun recibirImagen(uri: Uri?, propietario: Long, alFinalizar: () -> Unit = {}) {
        if (uri == null) { mensaje("Selección cancelada. Se conserva el avatar anterior."); alFinalizar(); return }
        if (_uiState.value.guardando || propietario != usuarioId || _uiState.value.usuario == null) { alFinalizar(); return }
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                ReglasPerfil.acceso(repository.obtenerSesion(), usuarioId)?.let { error(it) }
                val avatar = archivos.importar(uri, usuarioId)
                if (ReglasPerfil.acceso(repository.obtenerSesion(), usuarioId) == null) {
                    limpiarBorrador()
                    borradorAvatar = avatar
                    _uiState.update { it.copy(avatar = avatar, modificado = true, confirmacion = "Imagen lista. Guarda los cambios para conservarla.") }
                } else archivos.borrarBorrador(avatar, usuarioId)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { mensaje(e.message ?: "No se pudo cargar la imagen.") }
            finally { alFinalizar(); _uiState.update { it.copy(guardando = false) } }
        }
    }
    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando || estado.usuario == null) return
        val nombre = ReglasPerfil.nombre(estado.nombre.valor)
        val correo = ReglasPerfil.correo(estado.correo.valor)
        _uiState.update { it.copy(nombre = it.nombre.copy(error = nombre), correo = it.correo.copy(error = correo), error = null, confirmacion = null) }
        if (nombre != null || correo != null) return
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                repository.guardarPerfil(usuarioId, estado.nombre.valor, estado.correo.valor, estado.avatar,
                    PreferenciasUsuario(usuarioId, estado.indicadorAvisos, estado.textoGrande))
                limpiarBorrador(conservar = estado.avatar)
                _uiState.update { it.copy(modificado = false, confirmacion = "Perfil y preferencias guardados en este dispositivo.") }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { mensaje(e.message ?: "No fue posible guardar el perfil.") }
            finally { _uiState.update { it.copy(guardando = false) } }
        }
    }
    fun cerrarSesion() {
        if (_uiState.value.guardando) return
        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                limpiarBorrador()
                repository.cerrarSesionDePerfil(usuarioId)
                _uiState.value = PerfilUiState(cargando = false, salida = true)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(guardando = false, error = e.message) } }
        }
    }
}
