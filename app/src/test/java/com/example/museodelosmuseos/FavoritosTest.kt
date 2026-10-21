package com.example.museodelosmuseos

import com.example.museodelosmuseos.data.local.entity.*
import com.example.museodelosmuseos.data.model.ReglasFavoritos
import com.example.museodelosmuseos.ui.screens.favoritos.FavoritosUiState
import org.junit.Assert.*
import org.junit.Test

class FavoritosTest {
    private val cliente = SesionActiva(usuarioId = 1, nombre = "Cliente", correo = "cliente@example.com", rol = "CLIENTE", avatar = "")
    @Test fun requiereClienteActivoYPropietario() {
        assertNull(ReglasFavoritos.validarAcceso(cliente, 1))
        assertNotNull(ReglasFavoritos.validarAcceso(cliente, 2))
        assertNotNull(ReglasFavoritos.validarAcceso(null, 1))
        assertNotNull(ReglasFavoritos.validarAcceso(cliente.copy(rol = "ADMINISTRADOR"), 1))
        assertNotNull(ReglasFavoritos.validarAcceso(cliente.copy(rol = "INVESTIGADOR"), 1))
    }
    @Test fun admiteSoloEnlacesWebValidos() {
        assertTrue(ReglasFavoritos.enlaceValido("https://example.com/recorrido"))
        assertTrue(ReglasFavoritos.enlaceValido(" http://example.com/visita "))
        listOf("", "no es una URL", "https://", "javascript:alert(1)", "file:///privado", "intent://app").forEach {
            assertFalse(it, ReglasFavoritos.enlaceValido(it))
        }
    }
    @Test fun filtraNombrePaisYClasificacionSinDuplicarDatos() {
        val museo = Museo(id = 1, nombre = "Museo de Arte", pais = "Chile", ciudad = "Santiago", descripcion = "Demo",
            clasificacion = "Pintura", latitud = 0.0, longitud = 0.0, urlVisitaVirtual = "https://example.com", codigoQr = "DEMO")
        val estado = FavoritosUiState(museos = listOf(museo))
        for (texto in listOf("", " arte ", "CHILE", "pintura")) assertEquals(1, estado.copy(busqueda = texto).filtrados.size)
        assertTrue(estado.copy(busqueda = "España").filtrados.isEmpty())
        assertEquals(1, estado.museos.size)
    }
}
