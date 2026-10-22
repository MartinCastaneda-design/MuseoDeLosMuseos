package com.example.museodelosmuseos.data.model

import androidx.room.Embedded
import com.example.museodelosmuseos.data.local.entity.AvisoLocal
import com.example.museodelosmuseos.data.local.entity.SesionActiva

enum class DestinoAviso(val etiqueta: String) {
    SIN_ENLACE("Sin enlace"), MUSEO("Ficha de museo"), CATALOGO("Catálogo"), SOLICITUDES("Solicitudes")
}
enum class AudienciaAviso(val etiqueta: String) {
    TODOS("Todos"), CLIENTE("Cliente"), INVESTIGADOR("Investigador"), ADMINISTRADOR("Administrador")
}
data class AvisoConLectura(@Embedded val aviso: AvisoLocal, val fechaLectura: Long?) {
    val leido: Boolean get() = fechaLectura != null
}
data class FormularioAviso(val titulo: String = "", val mensaje: String = "",
    val audiencia: AudienciaAviso = AudienciaAviso.TODOS,
    val destino: DestinoAviso = DestinoAviso.SIN_ENLACE, val museoId: Long? = null,
    val intentoEnviar: Boolean = false) {
    val errorTitulo: String? get() = if (intentoEnviar) ReglasAvisos.validarTitulo(titulo) else null
    val errorMensaje: String? get() = if (intentoEnviar) ReglasAvisos.validarMensaje(mensaje) else null
}
data class EnlaceAviso(val destino: DestinoAviso, val museoId: Long? = null, val solicitudClave: String? = null)

object ReglasAvisos {
    fun acceso(sesion: SesionActiva?, actorId: Long): String? = when {
        sesion == null || sesion.usuarioId != actorId -> "La sesión cambió. Vuelve a abrir los avisos."
        !setOf("CLIENTE", "INVESTIGADOR", "ADMINISTRADOR").any { it.equals(sesion.rol, true) } -> "El rol actual no tiene acceso a avisos."
        else -> null
    }
    fun validarTitulo(valor: String): String? = if (valor.trim().length in 3..80) null else "Escribe un título de 3 a 80 caracteres."
    fun validarMensaje(valor: String): String? = if (valor.trim().length in 10..500) null else "Escribe un mensaje de 10 a 500 caracteres."
    fun puedeAbrir(rol: String, destino: DestinoAviso): Boolean = when (destino) {
        DestinoAviso.SIN_ENLACE, DestinoAviso.MUSEO -> setOf("CLIENTE", "INVESTIGADOR", "ADMINISTRADOR").any { it.equals(rol, true) }
        else -> rol.equals("CLIENTE", true) || rol.equals("ADMINISTRADOR", true)
    }
    fun validarFormulario(sesion: SesionActiva?, actorId: Long, formulario: FormularioAviso): String? =
        acceso(sesion, actorId) ?: when {
            !sesion!!.rol.equals("ADMINISTRADOR", true) -> "Solo el Administrador puede publicar avisos."
            validarTitulo(formulario.titulo) != null -> validarTitulo(formulario.titulo)
            validarMensaje(formulario.mensaje) != null -> validarMensaje(formulario.mensaje)
            formulario.destino == DestinoAviso.MUSEO && (formulario.museoId ?: 0) <= 0 -> "Selecciona un museo."
            formulario.destino in listOf(DestinoAviso.CATALOGO, DestinoAviso.SOLICITUDES) &&
                formulario.audiencia !in listOf(AudienciaAviso.CLIENTE, AudienciaAviso.ADMINISTRADOR) ->
                "Ese enlace requiere una audiencia Cliente o Administrador."
            else -> null
        }
}
