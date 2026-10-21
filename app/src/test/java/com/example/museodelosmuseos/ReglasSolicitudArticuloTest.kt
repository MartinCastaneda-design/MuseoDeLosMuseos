package com.example.museodelosmuseos

import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import com.example.museodelosmuseos.data.local.entity.SesionActiva
import com.example.museodelosmuseos.data.model.ReglasSolicitudArticulo as Reglas
import com.example.museodelosmuseos.ui.screens.detallearticulo.DetalleArticuloUiState
import org.junit.Assert.*
import org.junit.Test

class ReglasSolicitudArticuloTest {
    private val producto = ArticuloCatalogo(id = 3, titulo = "Libro", descripcion = "Descripción completa",
        categoria = "PUBLICACION", precio = 1500.0, stock = 5)
    private val cliente = SesionActiva(usuarioId = 7, nombre = "Cliente", correo = "cliente@example.com", rol = "CLIENTE", avatar = "")

    @Test fun cantidadRespetaStockAcumulado() {
        assertNull(Reglas.validarCantidad("3", stock = 5, enCarrito = 2))
        assertNotNull(Reglas.validarCantidad("4", stock = 5, enCarrito = 2))
        assertNotNull(Reglas.validarCantidad("1", stock = 5, enCarrito = 5))
    }

    @Test fun rechazaCantidadesInvalidasYDesbordamientos() {
        listOf("", "0", "-1", "1.5", "abc", "99999999999999999").forEach {
            assertNotNull(it, Reglas.validarCantidad(it, 5, 0))
        }
        assertNotNull(Reglas.validarCantidad(Int.MAX_VALUE.toString(), Int.MAX_VALUE, 1))
    }

    @Test fun validaDisponibilidadActual() {
        assertNull(Reglas.validarDisponibilidad(producto))
        assertNotNull(Reglas.validarDisponibilidad(null))
        assertNotNull(Reglas.validarDisponibilidad(producto.copy(activo = false)))
        assertNotNull(Reglas.validarDisponibilidad(producto.copy(disponible = false)))
        assertNotNull(Reglas.validarDisponibilidad(producto.copy(stock = 0)))
        assertNotNull(Reglas.validarDisponibilidad(producto.copy(precio = Double.NaN)))
    }

    @Test fun consultaExigeMensajeYLimitaLongitud() {
        assertNotNull(Reglas.validarConsulta("   "))
        assertNotNull(Reglas.validarConsulta("Horario"))
        assertNull(Reglas.validarConsulta("¿Cuáles son los horarios del curso?"))
        assertNull(Reglas.validarConsulta("a".repeat(500)))
        assertNotNull(Reglas.validarConsulta("a".repeat(501)))
    }

    @Test fun soloClienteDeLaSesionEsperadaPuedeSolicitar() {
        assertNull(Reglas.validarAcceso(cliente, 7))
        assertNotNull(Reglas.validarAcceso(null, 7))
        assertNotNull(Reglas.validarAcceso(cliente, 8))
        assertNotNull(Reglas.validarAcceso(cliente.copy(rol = "ADMINISTRADOR"), 7))
        assertNotNull(Reglas.validarAcceso(cliente.copy(rol = "INVESTIGADOR"), 7))
    }

    @Test fun estadoPresentaSubtotalYErroresPorCampo() {
        val estado = DetalleArticuloUiState(articulo = producto, cantidad = "3")
        assertEquals(4500.0, estado.subtotal, 0.0)
        assertNull(estado.copy(cantidad = "0").errorCantidad)
        assertNotNull(estado.copy(cantidad = "0", intentoEnviar = true).errorCantidad)
        assertNotNull(estado.copy(consulta = "", consultaEditada = true).errorConsulta)
        assertTrue(estado.copy(articulo = producto.copy(categoria = "CURSO")).esCurso)
    }
}
