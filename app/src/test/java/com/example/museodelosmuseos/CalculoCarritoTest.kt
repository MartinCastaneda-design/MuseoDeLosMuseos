package com.example.museodelosmuseos

import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import com.example.museodelosmuseos.data.model.*
import org.junit.Assert.*
import org.junit.Test

class CalculoCarritoTest {
    private fun linea(precio: Double, cantidad: Int) = LineaCarrito(ArticuloCatalogo(id = 3, titulo = "Libro",
        descripcion = "Prueba", categoria = "PUBLICACION", precio = precio, stock = 10), cantidad)

    @Test fun subtotalSumaLineasYRedondeaEnPesos() {
        val c = CalculoCarrito.cotizar(listOf(linea(1500.0, 2), linea(2990.0, 3)), DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR)
        assertEquals(11970L, c.subtotal)
        assertEquals(6000L, c.envio)
        assertEquals(17970L, c.total)
        assertEquals(201L, CalculoCarrito.subtotal(100.25, 2))
    }
    @Test fun envioNacionalEInternacionalPorModalidad() {
        assertEquals(4000L, CalculoCarrito.envio(1, DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR))
        assertEquals(6000L, CalculoCarrito.envio(1, DestinoEnvio.NACIONAL, ModalidadEnvio.EXPRESS))
        assertEquals(19000L, CalculoCarrito.envio(2, DestinoEnvio.INTERNACIONAL, ModalidadEnvio.ESTANDAR))
        assertEquals(28500L, CalculoCarrito.envio(2, DestinoEnvio.INTERNACIONAL, ModalidadEnvio.EXPRESS))
    }
    @Test fun rechazaCarritoVacioYStockInsuficiente() {
        assertTrue(runCatching { CalculoCarrito.cotizar(emptyList(), DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR) }.isFailure)
        assertTrue(runCatching { CalculoCarrito.cotizar(listOf(linea(1000.0, 11)), DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR) }.isFailure)
        assertNotNull(linea(1000.0, 1).copy(articulo = linea(1000.0, 1).articulo.copy(disponible = false)).error)
    }
    @Test fun rechazaNumerosInvalidosYDesbordamientos() {
        assertTrue(runCatching { CalculoCarrito.subtotal(Double.NaN, 1) }.isFailure)
        assertTrue(runCatching { CalculoCarrito.subtotal(1000.0, 0) }.isFailure)
        assertTrue(runCatching { CalculoCarrito.subtotal(Double.MAX_VALUE, 2) }.isFailure)
        assertTrue(runCatching { CalculoCarrito.envio(Long.MAX_VALUE, DestinoEnvio.INTERNACIONAL, ModalidadEnvio.EXPRESS) }.isFailure)
    }
    @Test fun validaCamposDeEntrega() {
        assertNotNull(CalculoCarrito.validarUbicacion(" "))
        assertNotNull(CalculoCarrito.validarDireccion("Calle"))
        assertNull(CalculoCarrito.validarUbicacion("Santiago"))
        assertNull(CalculoCarrito.validarDireccion("Calle de prueba 123"))
        assertNotNull(CalculoCarrito.validarDireccion("a".repeat(201)))
    }
}
