package com.example.museodelosmuseos.data.model

import androidx.room.Embedded
import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import java.math.BigDecimal
import java.math.RoundingMode

enum class DestinoEnvio(val etiqueta: String) { NACIONAL("Nacional"), INTERNACIONAL("Internacional") }
enum class ModalidadEnvio(val etiqueta: String) { ESTANDAR("Estándar"), EXPRESS("Express") }

data class LineaCarrito(@Embedded val articulo: ArticuloCatalogo, val cantidad: Int) {
    val subtotal: Long get() = CalculoCarrito.subtotal(articulo.precio, cantidad)
    val error: String? get() = ReglasSolicitudArticulo.validarDisponibilidad(articulo)
        ?: if (articulo.categoria.equals("CURSO", true)) "Los cursos se consultan desde su ficha."
        else ReglasSolicitudArticulo.validarCantidad(cantidad.toString(), articulo.stock, 0)
}

data class CotizacionCarrito(val lineas: List<LineaCarrito>, val subtotal: Long, val envio: Long, val total: Long)

/** Montos ficticios en pesos CLP; redondeo HALF_UP por línea y aritmética comprobada. */
object CalculoCarrito {
    fun subtotal(precio: Double, cantidad: Int): Long {
        require(precio.isFinite() && precio > 0 && cantidad > 0) { "Precio o cantidad inválidos." }
        return BigDecimal.valueOf(precio).multiply(BigDecimal.valueOf(cantidad.toLong()))
            .setScale(0, RoundingMode.HALF_UP).longValueExact()
    }

    fun envio(unidades: Long, destino: DestinoEnvio, modalidad: ModalidadEnvio): Long {
        require(unidades > 0) { "El carrito está vacío." }
        val base = if (destino == DestinoEnvio.NACIONAL) 3500L else 15000L
        val unidad = if (destino == DestinoEnvio.NACIONAL) 500L else 2000L
        val estandar = Math.addExact(base, Math.multiplyExact(unidad, unidades))
        return if (modalidad == ModalidadEnvio.EXPRESS)
            BigDecimal.valueOf(estandar).multiply(BigDecimal("1.5")).setScale(0, RoundingMode.HALF_UP).longValueExact()
        else estandar
    }

    fun cotizar(lineas: List<LineaCarrito>, destino: DestinoEnvio, modalidad: ModalidadEnvio): CotizacionCarrito {
        require(lineas.isNotEmpty()) { "El carrito está vacío." }
        lineas.forEach { it.error?.let { error -> throw IllegalArgumentException("${it.articulo.titulo}: $error") } }
        val subtotal = lineas.fold(0L) { total, linea -> Math.addExact(total, linea.subtotal) }
        val unidades = lineas.fold(0L) { total, linea -> Math.addExact(total, linea.cantidad.toLong()) }
        val envio = envio(unidades, destino, modalidad)
        return CotizacionCarrito(lineas, subtotal, envio, Math.addExact(subtotal, envio))
    }

    fun validarUbicacion(valor: String): String? = if (valor.trim().length in 3..100) null else "Ingresa ciudad/comuna o país y ciudad (3 a 100 caracteres)."
    fun validarDireccion(valor: String): String? = if (valor.trim().length in 8..200) null else "Ingresa calle, número y referencias (8 a 200 caracteres)."
}
