package com.example.museodelosmuseos

import androidx.room.Room
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.museodelosmuseos.data.local.AppDatabase
import com.example.museodelosmuseos.data.local.entity.*
import com.example.museodelosmuseos.data.repository.MuseoRepository
import com.example.museodelosmuseos.ui.screens.detalle.DetalleMuseoViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritosRoomTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private suspend fun sesion(db: AppDatabase, id: Long, rol: String = "CLIENTE") {
        db.sesionDao().guardarSesion(SesionActiva(usuarioId = id, nombre = "Cliente $id", correo = "cliente$id@example.com", rol = rol, avatar = ""))
    }
    private suspend fun preparar(db: AppDatabase) {
        for (id in 1L..2L) db.usuarioDao().registrar(Usuario(id = id, nombre = "Cliente $id", correo = "cliente$id@example.com", contrasena = "Prueba123", rol = "CLIENTE"))
        db.museoDao().insertar(Museo(id = 1, nombre = "Museo demo", pais = "Chile", ciudad = "Santiago", descripcion = "Demo",
            clasificacion = "Arte", latitud = 0.0, longitud = 0.0, urlVisitaVirtual = "https://example.com/visita", codigoQr = "FAV_TEST"))
        sesion(db, 1)
    }
    @Test fun separaColeccionesYNoPermiteOperarConOtraSesionORol() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            preparar(db)
            val repo = MuseoRepository(db)
            val flujo1 = repo.observarFavoritos(1)
            repo.conmutarFavorito(1, 1, true)
            repo.conmutarFavorito(1, 1, true)
            assertEquals(1, flujo1.first().size)
            assertTrue(repo.observarFavoritos(2).first().isEmpty())
            sesion(db, 2)
            assertTrue(flujo1.first().isEmpty())
            assertTrue(repo.observarFavoritos(2).first().isEmpty())
            assertTrue(runCatching { repo.conmutarFavorito(1, 1, false) }.isFailure)
            repo.conmutarFavorito(2, 1, true)
            repo.conmutarFavorito(2, 1, false)
            assertTrue(repo.observarFavoritos(2).first().isEmpty())
            sesion(db, 1)
            assertEquals(1, flujo1.first().size)
            for (rol in listOf("ADMINISTRADOR", "INVESTIGADOR")) {
                sesion(db, 1, rol)
                assertTrue(flujo1.first().isEmpty())
                assertFalse(repo.esFavorito(1, 1).first())
                assertTrue(runCatching { repo.conmutarFavorito(1, 1, false) }.isFailure)
            }
            db.sesionDao().cerrarSesion()
            assertTrue(flujo1.first().isEmpty())
        } finally { db.close() }
    }
    @Test fun favoritosPersistenAlReabrirBase() = runBlocking {
        val nombre = "favoritos-test-${System.nanoTime()}.db"
        try {
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).build().let { db ->
                try { preparar(db); MuseoRepository(db).conmutarFavorito(1, 1, true) } finally { db.close() }
            }
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).build().let { db ->
                try { assertEquals("Museo demo", MuseoRepository(db).observarFavoritos(1).first().single().nombre) }
                finally { db.close() }
            }
        } finally { context.deleteDatabase(nombre) }
    }
    @Test fun fichaDeMuseoActualizaFavoritoAlCambiarCliente() = runBlocking<Unit> {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val store = ViewModelStore()
        try {
            preparar(db)
            val repo = MuseoRepository(db)
            repo.conmutarFavorito(1, 1, true)
            val vm = withContext(Dispatchers.Main) { DetalleMuseoViewModel(repo, 1).also { store.put("detalle", it) } }
            withTimeout(5000) { vm.uiState.first { it.sesion?.usuarioId == 1L && it.esFavorito } }
            sesion(db, 2)
            withTimeout(5000) { vm.uiState.first { it.sesion?.usuarioId == 2L && !it.esFavorito } }
            repo.conmutarFavorito(2, 1, true)
            withTimeout(5000) { vm.uiState.first { it.sesion?.usuarioId == 2L && it.esFavorito } }
            sesion(db, 1)
            withTimeout(5000) { vm.uiState.first { it.sesion?.usuarioId == 1L && it.esFavorito } }
        } finally { withContext(Dispatchers.Main) { store.clear() }; db.close() }
    }
}
