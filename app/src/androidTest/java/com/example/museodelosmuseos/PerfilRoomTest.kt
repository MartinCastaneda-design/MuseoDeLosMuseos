package com.example.museodelosmuseos

import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.museodelosmuseos.data.local.*
import com.example.museodelosmuseos.data.local.entity.*
import com.example.museodelosmuseos.data.repository.MuseoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PerfilRoomTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private suspend fun preparar(db: AppDatabase) {
        for (id in 1L..3L) db.usuarioDao().registrar(Usuario(id, "Usuario $id", "usuario$id@correo.cl", "Clave123",
            when(id) { 1L -> "CLIENTE"; 2L -> "ADMINISTRADOR"; else -> "INVESTIGADOR" }))
        sesion(db, 1)
    }
    private suspend fun sesion(db: AppDatabase, id: Long) {
        val u = db.usuarioDao().obtenerPorId(id)!!
        db.sesionDao().guardarSesion(SesionActiva(usuarioId = id, nombre = u.nombre, correo = u.correo, rol = u.rol, avatar = u.avatar))
    }
    @Test fun edicionAtomicaPermisosYPreferenciasSeparadas() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            preparar(db); val repo = MuseoRepository(db)
            repo.guardarPerfil(1, "Cliente Editado", " EDITADO@correo.cl ", "avatar_2", PreferenciasUsuario(1, false, true))
            assertEquals("editado@correo.cl", repo.obtenerSesion()!!.correo)
            assertEquals("editado@correo.cl", repo.correoPerfilDemo("CLIENTE"))
            assertNull(db.usuarioDao().autenticar("usuario1@correo.cl", "Clave123"))
            assertNotNull(db.usuarioDao().autenticar("EDITADO@correo.cl", "Clave123"))
            assertEquals("Cliente Editado", repo.observarPerfilActivo(1).first()!!.nombre)
            assertEquals("CLIENTE", db.usuarioDao().obtenerPorId(1)!!.rol)
            assertEquals("Clave123", db.usuarioDao().obtenerPorId(1)!!.contrasena)
            assertTrue(repo.observarPreferencias(1).first()!!.textoGrande)
            assertTrue(runCatching { repo.guardarPerfil(1, "Otro nombre", "USUARIO2@correo.cl", "avatar_1", PreferenciasUsuario(1)) }.isFailure)
            assertEquals("Cliente Editado", db.usuarioDao().obtenerPorId(1)!!.nombre)
            assertTrue(runCatching { repo.guardarPerfil(2, "Intruso", "intruso@correo.cl", "avatar_1", PreferenciasUsuario(2)) }.isFailure)
            sesion(db, 2)
            assertNull(repo.observarPerfilActivo(1).first())
            assertNull(repo.observarPreferencias(1).first())
            assertNull(repo.observarPreferencias(2).first())
            assertTrue(runCatching { repo.cerrarSesionDePerfil(1) }.isFailure)
            for (id in 2L..3L) {
                sesion(db, id)
                repo.guardarPerfil(id, "Perfil $id Editado", "nuevo$id@correo.cl", "avatar_1", PreferenciasUsuario(id))
                assertEquals("Perfil $id Editado", repo.obtenerSesion()!!.nombre)
            }
            repo.cerrarSesionDePerfil(3)
            assertNull(repo.obtenerSesion())
            assertNull(repo.observarPerfilActivo(3).first())
            assertTrue(runCatching { repo.guardarPerfil(3, "No activo", "noactivo@correo.cl", "avatar_1", PreferenciasUsuario(3)) }.isFailure)
        } finally { db.close() }
    }
    @Test fun migracionSeisASieteYPersistenciaSinPerderDatos() = runBlocking {
        val nombre = "perfil-migracion-${System.nanoTime()}.db"
        try {
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).build().let { db ->
                try { preparar(db); db.avisoDao().insertar(AvisoLocal(titulo = "Aviso previo", mensaje = "Se conserva", fechaTimestamp = 1)) }
                finally { db.close() }
            }
            SQLiteDatabase.openDatabase(context.getDatabasePath(nombre).path, null, SQLiteDatabase.OPEN_READWRITE).let {
                try { it.execSQL("DROP TABLE preferencias_usuario"); it.version = 6 } finally { it.close() }
            }
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).addMigrations(AppDatabase.MIGRACION_6_7).build().let { db ->
                try {
                    val repo = MuseoRepository(db)
                    assertEquals(3, db.usuarioDao().observarTodos().first().size)
                    assertEquals("Aviso previo", repo.observarAvisos(1).first().single().aviso.titulo)
                    repo.guardarPerfil(1, "Perfil Persistente", "persistente@correo.cl", "avatar_3", PreferenciasUsuario(1, false, true))
                } finally { db.close() }
            }
            Room.databaseBuilder(context, AppDatabase::class.java, nombre).build().let { db ->
                try {
                    val repo = MuseoRepository(db)
                    assertEquals("Perfil Persistente", repo.observarPerfilActivo(1).first()!!.nombre)
                    assertFalse(repo.observarPreferencias(1).first()!!.indicadorAvisos)
                    assertTrue(repo.observarPreferencias(1).first()!!.textoGrande)
                    assertEquals("avatar_3", repo.obtenerSesion()!!.avatar)
                } finally { db.close() }
            }
        } finally { context.deleteDatabase(nombre) }
    }
    @Test fun copiaAvatarDuraderoYRechazaArchivoInvalido() = runBlocking {
        val archivos = ArchivosAvatar(context)
        val captura = archivos.crearCaptura()
        var avatar: String? = null
        try {
            val uri = archivos.uriCaptura(captura)
            assertEquals("content", uri.scheme)
            Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888).let { bitmap ->
                captura.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }; bitmap.recycle()
            }
            avatar = archivos.importar(uri, 90001)
            archivos.borrarCaptura(captura.path)
            assertTrue((archivos.modelo(avatar) as File).exists())
            assertFalse(captura.exists())
            captura.writeText("No es una imagen")
            assertTrue(runCatching { archivos.importar(Uri.fromFile(captura), 90001) }.isFailure)
        } finally {
            captura.delete()
            avatar?.let { archivos.borrarBorrador(it, 90001) }
        }
    }
}
