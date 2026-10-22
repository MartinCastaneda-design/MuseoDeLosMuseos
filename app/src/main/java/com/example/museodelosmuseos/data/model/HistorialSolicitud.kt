package com.example.museodelosmuseos.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.museodelosmuseos.data.local.entity.*

enum class EstadoSolicitud(val valor: String, val etiqueta: String) {
    PENDIENTE("PENDIENTE_LOCAL", "Pendiente"), CONFIRMADO("CONFIRMADO", "Confirmado"),
    ENVIADO("ENVIADO", "Enviado"), ENTREGADO("ENTREGADO", "Entregado");
    companion object {
        fun desde(valor: String): EstadoSolicitud? = entries.firstOrNull {
            it.valor.equals(valor, true) || it.name.equals(valor, true)
        }
    }
}

object PermisosSolicitudes {
    fun puedeConsultar(sesion: SesionActiva?, propietario: Long): Boolean =
        sesion != null && (sesion.rol.equals("ADMINISTRADOR", true) ||
            sesion.rol.equals("CLIENTE", true) && sesion.usuarioId == propietario)

    fun validarCambio(sesion: SesionActiva?, actorId: Long, actual: String, nuevo: EstadoSolicitud): String? = when {
        sesion == null || sesion.usuarioId != actorId -> "La sesión cambió. Vuelve al historial."
        !sesion.rol.equals("ADMINISTRADOR", true) -> "Solo el Administrador puede actualizar estados."
        EstadoSolicitud.desde(actual) == null -> "El estado guardado no es reconocido."
        EstadoSolicitud.desde(actual) == nuevo -> "Selecciona un estado diferente al actual."
        else -> null
    }
}

data class PedidoConLineas(
    @Embedded val pedido: PedidoLocal,
    @Relation(parentColumn = "id", entityColumn = "pedidoId") val lineas: List<PedidoLinea>
)

data class ConsultaConArticulo(
    @Embedded val consulta: SolicitudInformacion,
    @Relation(parentColumn = "articuloId", entityColumn = "id") val articulo: ArticuloCatalogo?
)

data class SolicitudHistorial(val pedido: PedidoLocal? = null, val consulta: SolicitudInformacion? = null,
    val lineas: List<PedidoLinea> = emptyList(), val tituloCurso: String? = null) {
    val clave: String get() = pedido?.let { "pedido-${it.id}" } ?: "curso-${consulta!!.usuarioId}-${consulta.articuloId}"
    val usuarioId: Long get() = pedido?.usuarioId ?: consulta!!.usuarioId
    val fecha: Long get() = pedido?.fechaTimestamp ?: consulta!!.fechaTimestamp
    val estado: String get() = pedido?.estado ?: consulta!!.estado
    val titulo: String get() = pedido?.let { "Pedido ${it.id.take(8)}" } ?: "Consulta: ${tituloCurso ?: "Curso"}"
}
