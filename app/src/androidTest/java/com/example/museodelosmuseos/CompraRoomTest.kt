package com.example.museodelosmuseos

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.museodelosmuseos.data.local.AppDatabase
import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import com.example.museodelosmuseos.data.local.entity.SesionActiva
import com.example.museodelosmuseos.data.local.entity.Usuario
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompraRoomTest {
    @Test fun confirmaCarritoConResumenRevalidadoYConservaOtrosUsuarios() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            preparar(db)
            val repo = MuseoRepository(db)
            repo.agregarAlCarrito(1, 1, 2)
            repo.cambiarCantidadCarrito(1, 1, 3)
            assertEquals(3, db.compraDao().obtenerItem(1, 1)!!.cantidad)
            assertTrue(runCatching { repo.cambiarCantidadCarrito(1, 1, 6) }.isFailure)
            repo.cambiarCantidadCarrito(1, 1, 2)
            sesion(db, 2)
            repo.agregarAlCarrito(2, 1, 1)
            assertTrue(runCatching { repo.eliminarDelCarrito(1, 1) }.isFailure)
            sesion(db, 1)
            val revisada = com.example.museodelosmuseos.data.model.CalculoCarrito.cotizar(
                db.compraDao().obtenerLineas(1), com.example.museodelosmuseos.data.model.DestinoEnvio.NACIONAL,
                com.example.museodelosmuseos.data.model.ModalidadEnvio.ESTANDAR)
            val articulo = db.articuloCatalogoDao().obtenerPorId(1)!!
            db.articuloCatalogoDao().actualizar(articulo.copy(precio = 2000.0))
            assertTrue(runCatching { repo.confirmarCarrito(1, com.example.museodelosmuseos.data.model.DestinoEnvio.NACIONAL,
                com.example.museodelosmuseos.data.model.ModalidadEnvio.ESTANDAR, "Santiago", "Calle de prueba 123", revisada) }.isFailure)
            assertTrue(repo.observarPedidos(1).first().isEmpty())
            assertEquals(2, db.compraDao().obtenerItem(1, 1)!!.cantidad)
            db.articuloCatalogoDao().actualizar(articulo)
            val id = repo.confirmarCarrito(1, com.example.museodelosmuseos.data.model.DestinoEnvio.NACIONAL,
                com.example.museodelosmuseos.data.model.ModalidadEnvio.ESTANDAR, "Santiago", "Calle de prueba 123", revisada)
            assertEquals(6500L, repo.observarPedidos(1).first().single().total)
            assertEquals(2, repo.obtenerLineasPedido(1, id).single().cantidad)
            assertTrue(repo.observarCarrito(1).first().isEmpty())
            assertEquals(1, repo.observarCarrito(2).first().single().cantidad)
            assertTrue(repo.obtenerLineasPedido(2, id).isEmpty())
            assertTrue(runCatching { repo.confirmarCarrito(1, com.example.museodelosmuseos.data.model.DestinoEnvio.NACIONAL,
                com.example.museodelosmuseos.data.model.ModalidadEnvio.ESTANDAR, "Santiago", "Calle de prueba 123", revisada) }.isFailure)
            assertEquals(1, repo.observarPedidos(1).first().size)
            sesion(db, 2)
            repo.eliminarDelCarrito(2, 1)
            assertTrue(repo.observarCarrito(2).first().isEmpty())
        } finally { db.close() }
    }

    @Test fun migraVersionCuatroPreservandoCarrito() = runBlocking {
        val nombre = "carrito-migracion-${System.nanoTime()}.db"
        try {
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).build().let { db ->
                try { preparar(db); MuseoRepository(db).agregarAlCarrito(1, 1, 2) } finally { db.close() }
            }
            val sqlite = SQLiteDatabase.openDatabase(context.getDatabasePath(nombre).path, null, SQLiteDatabase.OPEN_READWRITE)
            try {
                sqlite.execSQL("DROP TABLE pedido_lineas")
                sqlite.execSQL("DROP TABLE pedidos_locales")
                sqlite.version = 4
            } finally { sqlite.close() }
            Room.databaseBuilder(context, AppDatabase::class.java, nombre)
                .addMigrations(AppDatabase.MIGRACION_4_5).build().let { db ->
                    try {
                        assertEquals(2, db.compraDao().obtenerItem(1, 1)!!.cantidad)
                        assertEquals("Cliente 1", db.usuarioDao().obtenerPorId(1)!!.nombre)
                        assertTrue(db.compraDao().observarPedidos(1).first().isEmpty())
                    } finally { db.close() }
                }
        } finally { context.deleteDatabase(nombre) }
    }
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private suspend fun preparar(db: AppDatabase) {
        (1L..2L).forEach { id ->
            db.usuarioDao().registrar(Usuario(id = id, nombre = "Cliente $id", correo = "cliente$id@example.com",
                contrasena = "Prueba123", rol = "CLIENTE"))
        }
        db.articuloCatalogoDao().insertar(ArticuloCatalogo(id = 1, titulo = "Libro", descripcion = "Libro de prueba",
            categoria = "PUBLICACION", precio = 1000.0, stock = 5))
        db.articuloCatalogoDao().insertar(ArticuloCatalogo(id = 2, titulo = "Curso", descripcion = "Curso de prueba",
            categoria = "CURSO", precio = 5000.0, stock = 2))
        sesion(db, 1)
    }

    private suspend fun sesion(db: AppDatabase, id: Long) {
        db.sesionDao().guardarSesion(SesionActiva(usuarioId = id, nombre = "Cliente $id",
            correo = "cliente$id@example.com", rol = "CLIENTE", avatar = ""))
    }

    @Test fun acumulaSinExcederStockYSeparaUsuarios() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            preparar(db)
            val repo = MuseoRepository(db)
            repo.agregarAlCarrito(1, 1, 2)
            repo.agregarAlCarrito(1, 1, 3)
            assertTrue(runCatching { repo.agregarAlCarrito(1, 1, 1) }.isFailure)
            assertEquals(5, repo.observarCarrito(1).first().single().cantidad)
            sesion(db, 2)
            assertTrue(repo.observarCarrito(2).first().isEmpty())
            assertTrue(runCatching { repo.agregarAlCarrito(1, 1, 1) }.isFailure)
            repo.agregarAlCarrito(2, 1, 1)
            assertEquals(1, repo.observarCarrito(2).first().single().cantidad)
            assertEquals(5, repo.observarCarrito(1).first().single().cantidad)
            assertEquals(5, db.articuloCatalogoDao().obtenerPorId(1)!!.stock)
        } finally { db.close() }
    }

    @Test fun revalidaDisponibilidadYEvitaConsultasDuplicadas() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            preparar(db)
            val repo = MuseoRepository(db)
            db.articuloCatalogoDao().actualizarDisponibilidad(1, false)
            assertTrue(runCatching { repo.agregarAlCarrito(1, 1, 1) }.isFailure)
            assertTrue(runCatching { repo.agregarAlCarrito(1, 2, 1) }.isFailure)
            assertTrue(runCatching { repo.solicitarInformacion(1, 2, " ") }.isFailure)
            repo.solicitarInformacion(1, 2, "¿Cuál es el horario del curso?")
            assertTrue(runCatching { repo.solicitarInformacion(1, 2, "¿Cuál es el horario del curso?") }.isFailure)
            assertEquals(1, repo.observarSolicitudesInformacion(1).first().size)
            assertTrue(repo.observarSolicitudesInformacion(2).first().isEmpty())
        } finally { db.close() }
    }

    @Test fun migraVersionTresSinPerderUsuariosNiCatalogoYPersisteCarrito() = runBlocking {
        val nombre = "compra-migracion-${System.nanoTime()}.db"
        try {
            // Prepara el esquema base v3 usando las tablas existentes, sin callbacks de demostración.
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).build().let { db ->
                try { preparar(db) } finally { db.close() }
            }
            val sqlite = SQLiteDatabase.openDatabase(context.getDatabasePath(nombre).path, null, SQLiteDatabase.OPEN_READWRITE)
            try {
                sqlite.execSQL("DROP TABLE carrito_items")
                sqlite.execSQL("DROP TABLE solicitudes_informacion")
                sqlite.execSQL("DROP TABLE pedido_lineas")
                sqlite.execSQL("DROP TABLE pedidos_locales")
                sqlite.version = 3
            } finally { sqlite.close() }
            Room.databaseBuilder(context, AppDatabase::class.java, nombre)
                .addMigrations(AppDatabase.MIGRACION_3_4, AppDatabase.MIGRACION_4_5).build().let { db ->
                    try {
                        assertEquals("Cliente 1", db.usuarioDao().obtenerPorId(1)!!.nombre)
                        assertEquals("Libro", db.articuloCatalogoDao().obtenerPorId(1)!!.titulo)
                        MuseoRepository(db).agregarAlCarrito(1, 1, 2)
                    } finally { db.close() }
                }
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).build().let { db ->
                try { assertEquals(2, db.compraDao().obtenerItem(1, 1)!!.cantidad) } finally { db.close() }
            }
        } finally { context.deleteDatabase(nombre) }
    }
}
