package com.example.museodelosmuseos

import com.example.museodelosmuseos.data.local.entity.*
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.ui.screens.avisos.*
import org.junit.Assert.*
import org.junit.Test

class AvisosTest {
    private val admin = SesionActiva(usuarioId = 3, nombre = "Admin", correo = "admin@example.com", rol = "ADMINISTRADOR", avatar = "")
    private val valido = FormularioAviso(titulo = "Nuevo curso", mensaje = "Consulta el nuevo curso disponible.")
    @Test fun permisosDePublicacionYAcceso() {
        assertNull(ReglasAvisos.validarFormulario(admin, 3, valido))
        for (rol in listOf("CLIENTE", "INVESTIGADOR")) {
            assertNull(ReglasAvisos.acceso(admin.copy(rol = rol), 3))
            assertNotNull(ReglasAvisos.validarFormulario(admin.copy(rol = rol), 3, valido))
        }
        assertNotNull(ReglasAvisos.acceso(null, 3))
        assertNotNull(ReglasAvisos.acceso(admin, 2))
        assertNotNull(ReglasAvisos.acceso(admin.copy(rol = "DESCONOCIDO"), 3))
    }
    @Test fun validaCamposYReferencias() {
        assertNotNull(ReglasAvisos.validarFormulario(admin, 3, valido.copy(titulo = " ")))
        assertNotNull(ReglasAvisos.validarFormulario(admin, 3, valido.copy(mensaje = "Corto")))
        assertNotNull(ReglasAvisos.validarTitulo("x".repeat(81)))
        assertNotNull(ReglasAvisos.validarMensaje("x".repeat(501)))
        assertNotNull(ReglasAvisos.validarFormulario(admin, 3, valido.copy(destino = DestinoAviso.MUSEO)))
        assertNull(ReglasAvisos.validarFormulario(admin, 3, valido.copy(destino = DestinoAviso.MUSEO, museoId = 1)))
    }
    @Test fun enlacesRespetanAudiencias() {
        assertNotNull(ReglasAvisos.validarFormulario(admin, 3, valido.copy(destino = DestinoAviso.CATALOGO)))
        assertNull(ReglasAvisos.validarFormulario(admin, 3, valido.copy(destino = DestinoAviso.CATALOGO, audiencia = AudienciaAviso.CLIENTE)))
        assertFalse(ReglasAvisos.puedeAbrir("INVESTIGADOR", DestinoAviso.SOLICITUDES))
        assertTrue(ReglasAvisos.puedeAbrir("INVESTIGADOR", DestinoAviso.MUSEO))
    }
    @Test fun filtrosYContadorDependenDeLecturaPropia() {
        val aviso = AvisoLocal(id = 1, titulo = "Aviso", mensaje = "Mensaje de prueba", fechaTimestamp = 1)
        val s = AvisosUiState(avisos = listOf(AvisoConLectura(aviso, null), AvisoConLectura(aviso.copy(id = 2), 10)))
        assertEquals(1, s.noLeidos)
        assertEquals(2, s.filtrados.size)
        assertEquals(1L, s.copy(filtro = FiltroAvisos.NO_LEIDOS).filtrados.single().aviso.id)
        assertEquals(2L, s.copy(filtro = FiltroAvisos.LEIDOS).filtrados.single().aviso.id)
    }
}
