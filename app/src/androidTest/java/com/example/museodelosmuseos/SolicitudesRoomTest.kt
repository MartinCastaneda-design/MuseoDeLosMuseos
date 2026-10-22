package com.example.museodelosmuseos

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
class SolicitudesRoomTest {
    private fun base() = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java).build()
    private suspend fun sesion(db: AppDatabase, id: Long, rol: String = "CLIENTE") {
        db.sesionDao().guardarSesion(SesionActiva(usuarioId = id, nombre = "Prueba $id", correo = "prueba$id@example.com", rol = rol, avatar = ""))
    }
    private suspend fun preparar(db: AppDatabase) {
        for (id in 1L..4L) db.usuarioDao().registrar(Usuario(id = id, nombre = "Prueba $id", correo = "prueba$id@example.com",
            contrasena = "Prueba123", rol = if (id == 3L) "ADMINISTRADOR" else if (id == 4L) "INVESTIGADOR" else "CLIENTE"))
        db.articuloCatalogoDao().insertar(ArticuloCatalogo(id = 2, titulo = "Curso actual", descripcion = "Curso demo", categoria = "CURSO", precio = 5000.0))
        for (id in 1L..2L) {
            db.compraDao().insertarPedido(PedidoLocal("pedido$id", id, id, "NACIONAL", "ESTANDAR", "Santiago", "Calle demo 123", 2000, 4000, 6000))
            db.compraDao().insertarLineas(listOf(PedidoLinea("pedido$id", 99, "Título histórico", "", 1000.0, 2, 2000)))
            db.compraDao().guardarSolicitud(SolicitudInformacion(id, 2, "Quiero saber los horarios", id))
        }
        sesion(db, 1)
    }
    @Test fun aislamientoPorSesionYRolIncluyeCursosYProductos() = runBlocking {
        val db = base()
        try {
            preparar(db)
            val repo = MuseoRepository(db)
            val propias = repo.observarHistorialSolicitudes(1).first()
            assertEquals(2, propias.size)
            assertTrue(propias.all { it.usuarioId == 1L })
            assertTrue(repo.observarHistorialSolicitudes(2).first().isEmpty())
            sesion(db, 2)
            assertTrue(repo.observarHistorialSolicitudes(1).first().isEmpty())
            assertTrue(repo.observarHistorialSolicitudes(2).first().all { it.usuarioId == 2L })
            sesion(db, 3, "ADMINISTRADOR")
            assertEquals(4, repo.observarHistorialSolicitudes(3).first().size)
            sesion(db, 4, "INVESTIGADOR")
            assertTrue(repo.observarHistorialSolicitudes(4).first().isEmpty())
        } finally { db.close() }
    }
    @Test fun estadosPersistenConPermisosYProteccionAnteEdicionObsoleta() = runBlocking {
        val db = base()
        try {
            preparar(db)
            val repo = MuseoRepository(db)
            val original = repo.observarHistorialSolicitudes(1).first().first { it.pedido != null }
            assertTrue(runCatching { repo.cambiarEstadoSolicitud(1, original, EstadoSolicitud.CONFIRMADO) }.isFailure)
            sesion(db, 3, "ADMINISTRADOR")
            repo.cambiarEstadoSolicitud(3, original, EstadoSolicitud.CONFIRMADO)
            assertTrue(runCatching { repo.cambiarEstadoSolicitud(3, original, EstadoSolicitud.ENVIADO) }.isFailure)
            var actual = repo.observarHistorialSolicitudes(3).first().first { it.clave == original.clave }
            assertEquals(6000L, actual.pedido!!.total)
            assertEquals("Título histórico", actual.lineas.single().titulo)
            assertEquals(1000.0, actual.lineas.single().precioUnitario, 0.0)
            for (nuevo in listOf(EstadoSolicitud.ENVIADO, EstadoSolicitud.ENTREGADO, EstadoSolicitud.PENDIENTE)) {
                repo.cambiarEstadoSolicitud(3, actual, nuevo)
                actual = repo.observarHistorialSolicitudes(3).first().first { it.clave == original.clave }
                assertEquals(nuevo.valor, actual.estado)
            }
            val curso = repo.observarHistorialSolicitudes(3).first().first { it.consulta != null }
            repo.cambiarEstadoSolicitud(3, curso, EstadoSolicitud.CONFIRMADO)
            assertEquals("CONFIRMADO", db.compraDao().obtenerSolicitud(curso.usuarioId, 2)!!.estado)
            sesion(db, 2)
            assertTrue(runCatching { repo.cambiarEstadoSolicitud(3, actual, EstadoSolicitud.CONFIRMADO) }.isFailure)
        } finally { db.close() }
    }
}
