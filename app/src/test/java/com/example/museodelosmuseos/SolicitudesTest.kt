package com.example.museodelosmuseos

import com.example.museodelosmuseos.data.local.entity.*
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.ui.screens.solicitudes.SolicitudesUiState
import org.junit.Assert.*
import org.junit.Test

class SolicitudesTest {
    private val cliente = SesionActiva(usuarioId = 1, nombre = "Cliente", correo = "cliente@example.com", rol = "CLIENTE", avatar = "")
    @Test fun clienteSoloConsultaLoPropio() {
        assertTrue(PermisosSolicitudes.puedeConsultar(cliente, 1))
        assertFalse(PermisosSolicitudes.puedeConsultar(cliente, 2))
        assertFalse(PermisosSolicitudes.puedeConsultar(null, 1))
        assertFalse(PermisosSolicitudes.puedeConsultar(cliente.copy(rol = "INVESTIGADOR"), 1))
        assertTrue(PermisosSolicitudes.puedeConsultar(cliente.copy(rol = "ADMINISTRADOR"), 2))
    }
    @Test fun soloAdminPuedeCambiarEstadoConSesionVigente() {
        assertNotNull(PermisosSolicitudes.validarCambio(cliente, 1, "PENDIENTE_LOCAL", EstadoSolicitud.CONFIRMADO))
        val admin = cliente.copy(rol = "ADMINISTRADOR")
        assertNull(PermisosSolicitudes.validarCambio(admin, 1, "PENDIENTE_LOCAL", EstadoSolicitud.CONFIRMADO))
        assertNotNull(PermisosSolicitudes.validarCambio(admin, 2, "PENDIENTE_LOCAL", EstadoSolicitud.CONFIRMADO))
        assertNotNull(PermisosSolicitudes.validarCambio(null, 1, "PENDIENTE_LOCAL", EstadoSolicitud.CONFIRMADO))
    }
    @Test fun validaEstadosReconocidosYCambiosEfectivos() {
        val admin = cliente.copy(rol = "ADMINISTRADOR")
        assertEquals(EstadoSolicitud.PENDIENTE, EstadoSolicitud.desde("PENDIENTE_LOCAL"))
        assertEquals(EstadoSolicitud.PENDIENTE, EstadoSolicitud.desde("PENDIENTE"))
        assertNull(EstadoSolicitud.desde("INVENTADO"))
        assertNotNull(PermisosSolicitudes.validarCambio(admin, 1, "CONFIRMADO", EstadoSolicitud.CONFIRMADO))
        assertNotNull(PermisosSolicitudes.validarCambio(admin, 1, "INVENTADO", EstadoSolicitud.ENVIADO))
        assertNull(PermisosSolicitudes.validarCambio(admin, 1, "ENVIADO", EstadoSolicitud.ENTREGADO))
    }
    @Test fun filtraPedidosYConsultasSinAlterarMontosHistoricos() {
        val pedido = PedidoLocal("p1", 1, 1, "NACIONAL", "ESTANDAR", "Santiago", "Calle demo 123", 2000, 4000, 6000)
        val consulta = SolicitudInformacion(1, 2, "Consulta del curso", 2, "CONFIRMADO")
        val s = SolicitudesUiState(solicitudes = listOf(SolicitudHistorial(pedido = pedido), SolicitudHistorial(consulta = consulta)))
        assertEquals(2, s.filtradas.size)
        assertEquals("p1", s.copy(filtro = EstadoSolicitud.PENDIENTE).filtradas.single().pedido!!.id)
        assertEquals(6000L, s.filtradas.first().pedido!!.total)
        assertNotNull(s.copy(filtro = EstadoSolicitud.CONFIRMADO).filtradas.single().consulta)
        assertTrue(s.copy(filtro = EstadoSolicitud.ENTREGADO).filtradas.isEmpty())
    }
}
