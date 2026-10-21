package com.example.museodelosmuseos

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.museodelosmuseos.data.local.*
import com.example.museodelosmuseos.data.local.entity.*
import com.example.museodelosmuseos.data.repository.MuseoRepository
import com.example.museodelosmuseos.ui.screens.perfil.*
import com.example.museodelosmuseos.ui.theme.MuseoDeLosMuseosTheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PerfilPantallaTest {
    @get:Rule val compose = createComposeRule()
    @Test fun formularioValidaGuardaYCierraSesionSinMezclarPerfiles() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val store = ViewModelStore()
        try {
            runBlocking {
                db.usuarioDao().registrar(Usuario(1, "Cliente Prueba", "cliente@prueba.cl", "Clave123", "CLIENTE"))
                db.usuarioDao().registrar(Usuario(2, "Otro Cliente", "otro@prueba.cl", "Clave123", "CLIENTE"))
                db.sesionDao().guardarSesion(SesionActiva(usuarioId = 1, nombre = "Cliente Prueba", correo = "cliente@prueba.cl", rol = "CLIENTE", avatar = "avatar_1"))
            }
            val repo = MuseoRepository(db)
            val archivos = ArchivosAvatar(context)
            val vm = ViewModelProvider(store, object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = PerfilViewModel(repo, 1, archivos) as T
            })[PerfilViewModel::class.java]
            compose.setContent { MuseoDeLosMuseosTheme { PantallaPerfil(vm, archivos, {}, {}) } }
            compose.waitUntil(5000) { vm.uiState.value.usuario != null }
            compose.onNode(hasSetTextAction() and hasText("Nombre")).performScrollTo().performTextReplacement("ab")
            compose.onNodeWithText("Escribe un nombre de 3 a 80 caracteres.").assertExists()
            compose.onNode(hasSetTextAction() and hasText("Nombre")).performTextReplacement("Cliente Editado")
            compose.onNode(hasSetTextAction() and hasText("Correo")).performScrollTo().performTextReplacement("cliente.editado@prueba.cl")
            compose.onNodeWithText("Guardar cambios").performScrollTo().performClick()
            compose.waitUntil(5000) { vm.uiState.value.confirmacion?.startsWith("Perfil y preferencias") == true }
            runBlocking {
                assertEquals("Cliente Editado", repo.obtenerSesion()!!.nombre)
                assertEquals("Otro Cliente", db.usuarioDao().obtenerPorId(2)!!.nombre)
            }
            compose.onNodeWithText("Cambiar de usuario").performScrollTo().performClick()
            compose.onNodeWithText("Continuar").performClick()
            compose.waitUntil(5000) { vm.uiState.value.usuario == null }
            runBlocking { assertNull(repo.obtenerSesion()) }
            compose.onNodeWithText("Cliente Editado").assertDoesNotExist()
            runBlocking {
                db.sesionDao().guardarSesion(SesionActiva(usuarioId = 2, nombre = "Otro Cliente", correo = "otro@prueba.cl", rol = "CLIENTE", avatar = "avatar_1"))
                assertNull(repo.observarPerfilActivo(1).first())
            }
            compose.waitForIdle()
            assertNull(vm.uiState.value.usuario)
        } finally {
            compose.runOnIdle { store.clear() }
            db.close()
        }
    }
}
