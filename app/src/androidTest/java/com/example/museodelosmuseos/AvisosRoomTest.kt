package com.example.museodelosmuseos

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.museodelosmuseos.data.local.AppDatabase
import com.example.museodelosmuseos.data.local.entity.*
import com.example.museodelosmuseos.data.model.*
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AvisosRoomTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private suspend fun sesion(db: AppDatabase, id: Long) {
        val usuario = db.usuarioDao().obtenerPorId(id)!!
        db.sesionDao().guardarSesion(SesionActiva(usuarioId = id, nombre = usuario.nombre, correo = usuario.correo, rol = usuario.rol, avatar = ""))
    }
    private suspend fun preparar(db: AppDatabase) {
        for (id in 1L..4L) db.usuarioDao().registrar(Usuario(id = id, nombre = "Prueba $id", correo = "prueba$id@example.com", contrasena = "Prueba123",
            rol = when (id) { 3L -> "ADMINISTRADOR"; 4L -> "INVESTIGADOR"; else -> "CLIENTE" }))
        db.museoDao().insertar(Museo(id = 1, nombre = "Museo de prueba", pais = "Chile", ciudad = "Santiago", descripcion = "Prueba",
            clasificacion = "Arte", latitud = 0.0, longitud = 0.0, urlVisitaVirtual = "https://example.com", codigoQr = "AVISO"))
        db.articuloCatalogoDao().insertar(ArticuloCatalogo(id = 1, titulo = "Libro", descripcion = "Libro de prueba", categoria = "PUBLICACION", precio = 1000.0))
        sesion(db, 3)
    }
    @Test fun audienciaYLecturaIndependienteEntreUsuarios() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            preparar(db); val repo = MuseoRepository(db)
            val general = repo.publicarAviso(3, FormularioAviso("Aviso general", "Mensaje para todos los perfiles."))
            val investigador = repo.publicarAviso(3, FormularioAviso("Aviso de museos", "Revisa el museo de demostración.", AudienciaAviso.INVESTIGADOR, DestinoAviso.MUSEO, 1))
            sesion(db, 1)
            assertEquals(1, repo.observarAvisos(1).first().size)
            repo.cambiarLecturaAviso(1, general, true)
            assertTrue(repo.observarAvisos(1).first().single().leido)
            assertTrue(runCatching { repo.abrirAviso(1, investigador) }.isFailure)
            assertTrue(runCatching { repo.publicarAviso(1, FormularioAviso("No autorizado", "Mensaje de demostración.")) }.isFailure)
            sesion(db, 2)
            assertTrue(repo.observarAvisos(1).first().isEmpty())
            assertFalse(repo.observarAvisos(2).first().single().leido)
            assertTrue(runCatching { repo.cambiarLecturaAviso(1, general, false) }.isFailure)
            repo.marcarAvisosLeidos(2)
            assertTrue(repo.observarAvisos(2).first().single().leido)
            repo.cambiarLecturaAviso(2, general, false)
            assertFalse(repo.observarAvisos(2).first().single().leido)
            sesion(db, 4)
            assertEquals(2, repo.observarAvisos(4).first().size)
            assertEquals(1L, repo.abrirAviso(4, investigador).museoId)
            db.sesionDao().cerrarSesion()
            assertTrue(repo.observarAvisos(4).first().isEmpty())
        } finally { db.close() }
    }
    @Test fun solicitudesGeneranAvisosPrivadosYEnlacesValidos() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            preparar(db); val repo = MuseoRepository(db); sesion(db, 1)
            repo.agregarAlCarrito(1, 1, 2)
            val cotizacion = CalculoCarrito.cotizar(db.compraDao().obtenerLineas(1), DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR)
            val pedidoId = repo.confirmarCarrito(1, DestinoEnvio.NACIONAL, ModalidadEnvio.ESTANDAR, "Santiago", "Calle prueba 123", cotizacion)
            val aviso = repo.observarAvisos(1).first().single()
            assertEquals("pedido-$pedidoId", repo.abrirAviso(1, aviso.aviso.id).solicitudClave)
            sesion(db, 2)
            assertTrue(repo.observarAvisos(2).first().isEmpty())
            assertTrue(runCatching { repo.abrirAviso(2, aviso.aviso.id) }.isFailure)
            sesion(db, 3)
            assertTrue(repo.observarAvisos(3).first().isEmpty())
            val solicitud = repo.observarHistorialSolicitudes(3).first().single()
            repo.cambiarEstadoSolicitud(3, solicitud, EstadoSolicitud.CONFIRMADO)
            sesion(db, 1)
            assertEquals(2, repo.observarAvisos(1).first().size)
            assertEquals(1, repo.observarAvisos(1).first().count { !it.leido })
        } finally { db.close() }
    }
    @Test fun migracionCincoASeisConservaDatosYLecturasPersisten() = runBlocking {
        val nombre = "avisos-migracion-${System.nanoTime()}.db"
        try {
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).build().let { db ->
                try {
                    preparar(db); sesion(db, 1)
                    MuseoRepository(db).agregarAlCarrito(1, 1, 2)
                    MuseoRepository(db).conmutarFavorito(1, 1, true)
                    db.compraDao().insertarPedido(PedidoLocal("historico", 1, 1, "NACIONAL", "ESTANDAR", "Santiago", "Calle demo 123", 2000, 4000, 6000))
                } finally { db.close() }
            }
            val sqlite = SQLiteDatabase.openDatabase(context.getDatabasePath(nombre).path, null, SQLiteDatabase.OPEN_READWRITE)
            try { sqlite.execSQL("DROP TABLE avisos_lecturas"); sqlite.execSQL("DROP TABLE avisos_locales"); sqlite.version = 5 } finally { sqlite.close() }
            var avisoId = 0L
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).addMigrations(AppDatabase.MIGRACION_5_6, AppDatabase.MIGRACION_6_7).build().let { db ->
                try {
                    assertEquals(2, db.compraDao().obtenerItem(1, 1)!!.cantidad)
                    assertEquals(6000L, db.compraDao().obtenerPedido("historico")!!.total)
                    assertEquals(1, MuseoRepository(db).observarFavoritos(1).first().size)
                    sesion(db, 3)
                    avisoId = MuseoRepository(db).publicarAviso(3, FormularioAviso("Aviso persistente", "Mensaje local para verificar persistencia."))
                    sesion(db, 1); MuseoRepository(db).cambiarLecturaAviso(1, avisoId, true)
                } finally { db.close() }
            }
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).build().let { db ->
                try { assertTrue(MuseoRepository(db).observarAvisos(1).first().single { it.aviso.id == avisoId }.leido) } finally { db.close() }
            }
        } finally { context.deleteDatabase(nombre) }
    }
}
