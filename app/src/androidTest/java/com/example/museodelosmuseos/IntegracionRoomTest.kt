package com.example.museodelosmuseos

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.museodelosmuseos.data.local.AppDatabase
import com.example.museodelosmuseos.data.local.entity.*
import com.example.museodelosmuseos.data.model.ImagenesMuseosDemo
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IntegracionRoomTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun actualizacionDeImagenesEsSelectivaYRepetible() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            AppDatabase.inicializarDatos(db)
            val museos = db.museoDao().obtenerTodosDirecto()
            val primero = museos[0]; val segundo = museos[1]; val tercero = museos[2]
            val imagen = ImagenesMuseosDemo.porCodigo.getValue(primero.codigoQr)
            db.museoDao().actualizar(primero.copy(imagenUrl = imagen.anteriorUnsplash))
            db.museoDao().actualizar(segundo.copy(imagenUrl = "https://example.com/personalizada.jpg"))
            db.museoDao().actualizar(tercero.copy(imagenUrl = ""))
            repeat(2) { AppDatabase.actualizarImagenesSiEsNecesario(db) }
            assertEquals(imagen.url, db.museoDao().obtenerPorId(primero.id)!!.imagenUrl)
            assertEquals("https://example.com/personalizada.jpg", db.museoDao().obtenerPorId(segundo.id)!!.imagenUrl)
            assertEquals("", db.museoDao().obtenerPorId(tercero.id)!!.imagenUrl)
            assertEquals(museos.size, db.museoDao().obtenerTodosDirecto().size)
        } finally { db.close() }
    }

    @Test fun inicializacionConservaPerfilEditadoStockSesionYPreferencias() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            AppDatabase.inicializarDatos(db)
            assertNull(db.sesionDao().obtenerSesion())
            val admin = db.usuarioDao().obtenerPorRol("ADMINISTRADOR").single()
            val repo = MuseoRepository(db)
            db.sesionDao().guardarSesion(SesionActiva(usuarioId = admin.id, nombre = admin.nombre,
                correo = admin.correo, rol = admin.rol, avatar = admin.avatar))
            repo.guardarPerfil(admin.id, "Administrador Editado", "editado@prueba.cl", "avatar_2", PreferenciasUsuario(admin.id, false, true))
            val articulo = db.articuloCatalogoDao().obtenerPorId(1)!!
            db.articuloCatalogoDao().actualizar(articulo.copy(stock = 2))
            repeat(2) { AppDatabase.inicializarDatos(db) }
            assertEquals(3, db.usuarioDao().observarTodos().first().size)
            assertEquals("editado@prueba.cl", repo.obtenerSesion()!!.correo)
            assertEquals("avatar_2", repo.obtenerSesion()!!.avatar)
            assertTrue(repo.observarPreferencias(admin.id).first()!!.textoGrande)
            assertEquals(2, db.articuloCatalogoDao().obtenerPorId(1)!!.stock)
            repo.cerrarSesionDePerfil(admin.id)
            AppDatabase.inicializarDatos(db)
            assertNull(repo.obtenerSesion())
        } finally { db.close() }
    }

    @Test fun migracionDeMainVersionDosASieteConservaDatosYCompletaCatalogo() = runBlocking {
        val nombre = "integracion-v2-v7-${System.nanoTime()}.db"
        try {
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).build().let { db ->
                try {
                    AppDatabase.inicializarDatos(db)
                    val cliente = db.usuarioDao().obtenerPorRol("CLIENTE").single()
                    val museo = db.museoDao().obtenerTodosDirecto().first()
                    db.museoDao().actualizar(museo.copy(imagenUrl = "https://example.com/historica.jpg"))
                    db.favoritoDao().agregarFavorito(FavoritoMuseo(usuarioId = cliente.id, museoId = museo.id))
                    db.sesionDao().guardarSesion(SesionActiva(usuarioId = cliente.id, nombre = cliente.nombre,
                        correo = cliente.correo, rol = cliente.rol, avatar = cliente.avatar))
                } finally { db.close() }
            }
            // Base aislada: conserva las tablas v2 y elimina únicamente las incorporadas después.
            SQLiteDatabase.openDatabase(context.getDatabasePath(nombre).path, null, SQLiteDatabase.OPEN_READWRITE).let { sqlite ->
                try {
                    for (tabla in listOf("preferencias_usuario", "avisos_lecturas", "avisos_locales", "pedido_lineas", "pedidos_locales",
                        "carrito_items", "solicitudes_informacion", "articulos_catalogo", "control_inicializacion")) sqlite.execSQL("DROP TABLE $tabla")
                    sqlite.version = 2
                } finally { sqlite.close() }
            }
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).addMigrations(AppDatabase.MIGRACION_2_3,
                AppDatabase.MIGRACION_3_4, AppDatabase.MIGRACION_4_5, AppDatabase.MIGRACION_5_6, AppDatabase.MIGRACION_6_7).build().let { db ->
                try {
                    val antes = db.sesionDao().obtenerSesion()!!
                    AppDatabase.inicializarDatos(db)
                    assertEquals(antes, db.sesionDao().obtenerSesion())
                    assertEquals(3, db.usuarioDao().observarTodos().first().size)
                    assertEquals(5, db.museoDao().obtenerTodosDirecto().size)
                    assertEquals("https://example.com/historica.jpg", db.museoDao().obtenerTodosDirecto().first().imagenUrl)
                    assertEquals(1, MuseoRepository(db).observarFavoritos(antes.usuarioId).first().size)
                    assertNotNull(db.articuloCatalogoDao().obtenerPorId(8))
                    assertEquals(7, db.openHelper.readableDatabase.version)
                } finally { db.close() }
            }
        } finally { context.deleteDatabase(nombre) }
    }
}
