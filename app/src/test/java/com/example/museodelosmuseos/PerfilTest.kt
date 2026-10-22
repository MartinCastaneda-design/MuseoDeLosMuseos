package com.example.museodelosmuseos

import com.example.museodelosmuseos.data.local.entity.SesionActiva
import com.example.museodelosmuseos.data.model.ReglasPerfil
import org.junit.Assert.*
import org.junit.Test

class PerfilTest {
    @Test fun validaNombreConEspaciosAcentosYLimites() {
        assertNull(ReglasPerfil.nombre(" María López "))
        listOf("", "ab", "a".repeat(81), "María\nLópez").forEach { assertNotNull(ReglasPerfil.nombre(it)) }
    }
    @Test fun validaCorreoSinEspaciosYConDominio() {
        assertNull(ReglasPerfil.correo(" Maria.Lopez+perfil@correo.cl "))
        listOf("", "correo", "a@b", "a b@correo.cl", "a@-correo.cl", "a@correo..cl", "a\n@correo.cl").forEach { assertNotNull(it, ReglasPerfil.correo(it)) }
    }
    @Test fun accesoSoloAlPerfilActivoEnLosTresRoles() {
        for (rol in listOf("ADMINISTRADOR", "INVESTIGADOR", "CLIENTE")) {
            val sesion = SesionActiva(usuarioId = 7, nombre = "Prueba", correo = "prueba@correo.cl", rol = rol, avatar = "avatar_1")
            assertNull(ReglasPerfil.acceso(sesion, 7))
            assertNotNull(ReglasPerfil.acceso(sesion, 8))
        }
        assertNotNull(ReglasPerfil.acceso(null, 7))
    }
    @Test fun avatarPrivadoRechazaOtroUsuarioYRutasExternas() {
        assertNull(ReglasPerfil.avatar("avatar_1", 7))
        assertNull(ReglasPerfil.avatar("avatar-local/7/ab01-cdef.img", 7))
        listOf("avatar-local/8/ab01.img", "avatar-local/7/../8/ab01.img", "https://example.com/foto.jpg", "/sdcard/foto.jpg").forEach {
            assertNotNull(ReglasPerfil.avatar(it, 7))
        }
    }
}
