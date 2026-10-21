package com.example.museodelosmuseos

import com.example.museodelosmuseos.data.local.entity.SesionActiva
import com.example.museodelosmuseos.ui.screens.dashboard.DashboardUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogoAccesoTest {
    private fun estado(rol: String) = DashboardUiState(
        sesion = SesionActiva(
            usuarioId = 1L, nombre = "Prueba", correo = "prueba@example.com",
            rol = rol, avatar = ""
        )
    )

    @Test
    fun permiteClienteYAdministrador() {
        assertTrue(estado("CLIENTE").puedeAccederCatalogo)
        assertTrue(estado("ADMINISTRADOR").puedeAccederCatalogo)
        assertTrue(estado("cliente").puedeAccederCatalogo)
    }

    @Test
    fun rechazaInvestigadorSesionAusenteYRolDesconocido() {
        assertFalse(estado("INVESTIGADOR").puedeAccederCatalogo)
        assertFalse(DashboardUiState().puedeAccederCatalogo)
        assertFalse(estado("OTRO").puedeAccederCatalogo)
    }
}
