package com.example.museodelosmuseos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.museodelosmuseos.data.local.dao.FavoritoDao
import com.example.museodelosmuseos.data.local.dao.HistorialQrDao
import com.example.museodelosmuseos.data.local.dao.MuseoDao
import com.example.museodelosmuseos.data.local.dao.SesionDao
import com.example.museodelosmuseos.data.local.dao.UsuarioDao
import com.example.museodelosmuseos.data.local.entity.FavoritoMuseo
import com.example.museodelosmuseos.data.local.entity.HistorialQr
import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.local.entity.SesionActiva
import com.example.museodelosmuseos.data.local.entity.Usuario
import com.example.museodelosmuseos.data.model.Rol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos Room del proyecto Museo de los Museos.
 * Maneja usuarios, museos de Iberoamérica, favoritos por perfil, historial de QR y sesión.
 */
@Database(
    entities = [
        Usuario::class,
        Museo::class,
        FavoritoMuseo::class,
        HistorialQr::class,
        SesionActiva::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun museoDao(): MuseoDao
    abstract fun favoritoDao(): FavoritoDao
    abstract fun historialQrDao(): HistorialQrDao
    abstract fun sesionDao(): SesionDao

    companion object {
        const val NOMBRE_BD = "museo_de_los_museos.db"

        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtenerInstancia(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE_BD
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                precargarDatosIniciales(obtenerInstancia(context))
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Garantiza que tras una migración o apertura la BD nunca quede vacía
                            CoroutineScope(Dispatchers.IO).launch {
                                val dbInstancia = obtenerInstancia(context)
                                val adminExiste = dbInstancia.usuarioDao().buscarPorCorreo("admin@museo.org")
                                if (adminExiste == null) {
                                    precargarDatosIniciales(dbInstancia)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }

        /**
         * Inserta usuarios y museos de demostración para que la aplicación
         * esté lista para evaluar desde la primera ejecución.
         */
        suspend fun precargarDatosIniciales(database: AppDatabase) {
            val usuariosDemo = listOf(
                Usuario(
                    nombre = "Carlos Administrador",
                    correo = "admin@museo.org",
                    contrasena = "admin123",
                    rol = Rol.ADMINISTRADOR.name,
                    avatar = "avatar_admin"
                ),
                Usuario(
                    nombre = "Dra. Elena Ramos",
                    correo = "investigador@museo.org",
                    contrasena = "inves123",
                    rol = Rol.INVESTIGADOR.name,
                    avatar = "avatar_investigador"
                ),
                Usuario(
                    nombre = "María Fernanda López",
                    correo = "maria@email.com",
                    contrasena = "cliente123",
                    rol = Rol.CLIENTE.name,
                    avatar = "avatar_cliente"
                )
            )
            database.usuarioDao().insertarSiNoExisten(usuariosDemo)

            val museosDemo = listOf(
                Museo(
                    nombre = "Museo de Arte Colonial",
                    pais = "Colombia",
                    ciudad = "Bogotá",
                    descripcion = "Recorre siglos de historia a través de nuestras colecciones de arte colonial iberoamericano. Explora salas virtuales, exposiciones y material educativo.",
                    clasificacion = "Arte Colonial",
                    latitud = 4.5975,
                    longitud = -74.0758,
                    urlVisitaVirtual = "https://artsandculture.google.com/partner/museo-colonial",
                    codigoQr = "MUSEO_COLONIAL_BOGOTA",
                    imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Museo_Colonial_Bogot%C3%A1.jpg/800px-Museo_Colonial_Bogot%C3%A1.jpg"
                ),
                Museo(
                    nombre = "Museo Nacional de Bellas Artes",
                    pais = "Chile",
                    ciudad = "Santiago",
                    descripcion = "Principal centro de difusión de las artes visuales en Chile, con colecciones históricas y salas de exposiciones patrimoniales.",
                    clasificacion = "Bellas Artes",
                    latitud = -33.4358,
                    longitud = -70.6433,
                    urlVisitaVirtual = "https://artsandculture.google.com/partner/museo-nacional-de-bellas-artes-chile",
                    codigoQr = "MUSEO_BELLAS_ARTES_CHILE",
                    imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Museo_Nacional_de_Bellas_Artes_de_Chile_-_Diciembre_2016.jpg/800px-Museo_Nacional_de_Bellas_Artes_de_Chile_-_Diciembre_2016.jpg"
                ),
                Museo(
                    nombre = "Museo Nacional del Prado",
                    pais = "España",
                    ciudad = "Madrid",
                    descripcion = "Una de las pinacotecas más importantes del mundo, con obras maestras de Velázquez, Goya, El Greco y otros maestros universales.",
                    clasificacion = "Historia y Pintura",
                    latitud = 40.4138,
                    longitud = -3.6921,
                    urlVisitaVirtual = "https://artsandculture.google.com/partner/museo-del-prado",
                    codigoQr = "MUSEO_PRADO_MADRID",
                    imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Museo_del_Prado_2016_%2825185969599%29.jpg/800px-Museo_del_Prado_2016_%2825185969599%29.jpg"
                ),
                Museo(
                    nombre = "Museo de Arte Moderno",
                    pais = "México",
                    ciudad = "Ciudad de México",
                    descripcion = "Espacio emblemático dedicado a preservar y difundir el arte moderno mexicano del siglo XX, situado en el bosque de Chapultepec.",
                    clasificacion = "Arte Moderno",
                    latitud = 19.4225,
                    longitud = -99.1795,
                    urlVisitaVirtual = "https://artsandculture.google.com/partner/museo-de-arte-moderno-mexico",
                    codigoQr = "MUSEO_MAM_MEXICO",
                    imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/Museo_de_Arte_Moderno_%28MAM%29_-_Ciudad_de_M%C3%A9xico.jpg/800px-Museo_de_Arte_Moderno_%28MAM%29_-_Ciudad_de_M%C3%A9xico.jpg"
                ),
                Museo(
                    nombre = "Museo de Arte de Lima (MALI)",
                    pais = "Perú",
                    ciudad = "Lima",
                    descripcion = "Exhibe más de 3,000 años de historia del arte peruano, desde la era precolombina, colonial y republicana hasta la contemporánea.",
                    clasificacion = "Arte y Arqueología",
                    latitud = -12.0601,
                    longitud = -77.0369,
                    urlVisitaVirtual = "https://artsandculture.google.com/partner/museo-de-arte-de-lima-mali",
                    codigoQr = "MUSEO_MALI_LIMA",
                    imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Palacio_de_la_Exposici%C3%B3n_%28MALI%29_Lima_Peru.jpg/800px-Palacio_de_la_Exposici%C3%B3n_%28MALI%29_Lima_Peru.jpg"
                )
            )
            database.museoDao().insertarSiNoExisten(museosDemo)

            // Iniciar con la sesión demo de María Fernanda (Cliente) activa por defecto
            val usuarioCliente = database.usuarioDao().buscarPorCorreo("maria@email.com")
            if (usuarioCliente != null) {
                database.sesionDao().guardarSesion(
                    SesionActiva(
                        usuarioId = usuarioCliente.id,
                        nombre = usuarioCliente.nombre,
                        correo = usuarioCliente.correo,
                        rol = usuarioCliente.rol,
                        avatar = usuarioCliente.avatar
                    )
                )
            }
        }
    }
}
