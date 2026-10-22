package com.example.museodelosmuseos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.example.museodelosmuseos.data.local.entity.AvisoLocal
import com.example.museodelosmuseos.data.local.entity.AvisoLectura
import com.example.museodelosmuseos.data.local.dao.AvisoDao
import com.example.museodelosmuseos.data.local.entity.PedidoLocal
import com.example.museodelosmuseos.data.local.entity.PedidoLinea
import com.example.museodelosmuseos.data.local.dao.CompraDao
import com.example.museodelosmuseos.data.local.entity.CarritoItem
import com.example.museodelosmuseos.data.local.entity.SolicitudInformacion
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.museodelosmuseos.data.local.dao.ArticuloCatalogoDao
import com.example.museodelosmuseos.data.local.dao.ControlInicializacionDao
import com.example.museodelosmuseos.data.local.dao.FavoritoDao
import com.example.museodelosmuseos.data.local.dao.HistorialQrDao
import com.example.museodelosmuseos.data.local.dao.MuseoDao
import com.example.museodelosmuseos.data.local.dao.SesionDao
import com.example.museodelosmuseos.data.local.dao.UsuarioDao
import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import com.example.museodelosmuseos.data.local.entity.ControlInicializacion
import com.example.museodelosmuseos.data.local.entity.FavoritoMuseo
import com.example.museodelosmuseos.data.local.entity.HistorialQr
import com.example.museodelosmuseos.data.model.ImagenesMuseosDemo
import com.example.museodelosmuseos.data.local.entity.Museo
import com.example.museodelosmuseos.data.local.entity.SesionActiva
import com.example.museodelosmuseos.data.local.entity.Usuario
import com.example.museodelosmuseos.data.model.CategoriaArticulo
import com.example.museodelosmuseos.data.model.Rol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Usuario::class,
        Museo::class,
        FavoritoMuseo::class,
        HistorialQr::class,
        SesionActiva::class,
        ArticuloCatalogo::class,
        ControlInicializacion::class,
        CarritoItem::class,
        SolicitudInformacion::class,
        PedidoLocal::class,
        PedidoLinea::class,
        AvisoLocal::class,
        AvisoLectura::class,
        com.example.museodelosmuseos.data.local.entity.PreferenciasUsuario::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun avisoDao(): AvisoDao
    abstract fun compraDao(): CompraDao

    abstract fun usuarioDao(): UsuarioDao
    abstract fun museoDao(): MuseoDao
    abstract fun favoritoDao(): FavoritoDao
    abstract fun historialQrDao(): HistorialQrDao
    abstract fun sesionDao(): SesionDao
    abstract fun articuloCatalogoDao(): ArticuloCatalogoDao
    abstract fun controlInicializacionDao(): ControlInicializacionDao

    companion object {
        const val NOMBRE_BD = "museo_de_los_museos.db"
        const val CLAVE_CATALOGO_PRECARGADO = "CATALOGO_PRECARGADO_V1"

        val MIGRACION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS `preferencias_usuario` (
                    `usuarioId` INTEGER NOT NULL, `indicadorAvisos` INTEGER NOT NULL, `textoGrande` INTEGER NOT NULL,
                    PRIMARY KEY(`usuarioId`), FOREIGN KEY(`usuarioId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)""")
            }
        }

        /** Solo agrega avisos y lecturas. Conserva todas las tablas y registros anteriores. */
        val MIGRACION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS `avisos_locales` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `titulo` TEXT NOT NULL,
                    `mensaje` TEXT NOT NULL, `fechaTimestamp` INTEGER NOT NULL, `rolDestino` TEXT NOT NULL,
                    `usuarioDestinoId` INTEGER, `destino` TEXT NOT NULL, `museoId` INTEGER, `solicitudClave` TEXT,
                    FOREIGN KEY(`usuarioDestinoId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_avisos_locales_usuarioDestinoId` ON `avisos_locales` (`usuarioDestinoId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_avisos_locales_rolDestino` ON `avisos_locales` (`rolDestino`)")
                db.execSQL("""CREATE TABLE IF NOT EXISTS `avisos_lecturas` (
                    `avisoId` INTEGER NOT NULL, `usuarioId` INTEGER NOT NULL, `fechaLectura` INTEGER NOT NULL,
                    PRIMARY KEY(`avisoId`, `usuarioId`),
                    FOREIGN KEY(`avisoId`) REFERENCES `avisos_locales`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`usuarioId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_avisos_lecturas_usuarioId` ON `avisos_lecturas` (`usuarioId`)")
            }
        }

        val MIGRACION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS `pedidos_locales` (
                    `id` TEXT NOT NULL, `usuarioId` INTEGER NOT NULL, `fechaTimestamp` INTEGER NOT NULL,
                    `destino` TEXT NOT NULL, `modalidad` TEXT NOT NULL, `ubicacion` TEXT NOT NULL,
                    `direccion` TEXT NOT NULL, `subtotal` INTEGER NOT NULL, `envio` INTEGER NOT NULL,
                    `total` INTEGER NOT NULL, `estado` TEXT NOT NULL, PRIMARY KEY(`id`),
                    FOREIGN KEY(`usuarioId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pedidos_locales_usuarioId` ON `pedidos_locales` (`usuarioId`)")
                db.execSQL("""CREATE TABLE IF NOT EXISTS `pedido_lineas` (
                    `pedidoId` TEXT NOT NULL, `articuloId` INTEGER NOT NULL, `titulo` TEXT NOT NULL,
                    `imagenUrl` TEXT NOT NULL, `precioUnitario` REAL NOT NULL, `cantidad` INTEGER NOT NULL,
                    `subtotal` INTEGER NOT NULL, PRIMARY KEY(`pedidoId`, `articuloId`),
                    FOREIGN KEY(`pedidoId`) REFERENCES `pedidos_locales`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)""")
            }
        }

        /** Conserva usuarios, favoritos, historial y catálogo al incorporar compras locales. */
        val MIGRACION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `carrito_items` (
                        `usuarioId` INTEGER NOT NULL, `articuloId` INTEGER NOT NULL,
                        `cantidad` INTEGER NOT NULL, PRIMARY KEY(`usuarioId`, `articuloId`),
                        FOREIGN KEY(`usuarioId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`articuloId`) REFERENCES `articulos_catalogo`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_carrito_items_articuloId` ON `carrito_items` (`articuloId`)")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `solicitudes_informacion` (
                        `usuarioId` INTEGER NOT NULL, `articuloId` INTEGER NOT NULL,
                        `mensaje` TEXT NOT NULL, `fechaTimestamp` INTEGER NOT NULL, `estado` TEXT NOT NULL,
                        PRIMARY KEY(`usuarioId`, `articuloId`),
                        FOREIGN KEY(`usuarioId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`articuloId`) REFERENCES `articulos_catalogo`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_solicitudes_informacion_articuloId` ON `solicitudes_informacion` (`articuloId`)")
            }
        }

        // Crea el catálogo y su control de precarga sin borrar datos de la versión 2.
        val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `articulos_catalogo` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `titulo` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `categoria` TEXT NOT NULL,
                        `precio` REAL NOT NULL,
                        `imagenUrl` TEXT NOT NULL,
                        `disponible` INTEGER NOT NULL,
                        `activo` INTEGER NOT NULL,
                        `stock` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_articulos_catalogo_categoria` ON `articulos_catalogo` (`categoria`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_articulos_catalogo_activo` ON `articulos_catalogo` (`activo`)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `control_inicializacion` (
                        `clave` TEXT NOT NULL,
                        `valor` TEXT NOT NULL,
                        `fechaTimestamp` INTEGER NOT NULL,
                        PRIMARY KEY(`clave`)
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtenerInstancia(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE_BD
                )
                    .addMigrations(MIGRACION_2_3, MIGRACION_3_4, MIGRACION_4_5, MIGRACION_5_6, MIGRACION_6_7)
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Respeta la marca de precarga al abrir o migrar la base de datos.

                            CoroutineScope(Dispatchers.IO).launch {
                                inicializarDatos(obtenerInstancia(context))
                            }
                        }
                    })
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }

        /** Una sola transacción evita precargas simultáneas y conserva la sesión existente. */
        suspend fun inicializarDatos(database: AppDatabase) = database.withTransaction {
            val adminExiste = database.usuarioDao().obtenerPorRol("ADMINISTRADOR").isNotEmpty()
            val catalogoInicializado = database.controlInicializacionDao().existeClave(CLAVE_CATALOGO_PRECARGADO) > 0
            if (!adminExiste || !catalogoInicializado) precargarDatosIniciales(database)
            actualizarImagenesSiEsNecesario(database)
        }

        // Carga los datos de demostración sin sobrescribir los existentes.
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
            // Un correo de perfil puede haber sido editado; no recrear cuentas por su correo anterior.
            for (usuarioDemo in usuariosDemo) {
                if (database.usuarioDao().obtenerPorRol(usuarioDemo.rol).isEmpty()) {
                    database.usuarioDao().insertarSiNoExisten(listOf(usuarioDemo))
                }
            }

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
                    imagenUrl = ImagenesMuseosDemo.porCodigo.getValue("MUSEO_COLONIAL_BOGOTA").url
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
                    imagenUrl = ImagenesMuseosDemo.porCodigo.getValue("MUSEO_BELLAS_ARTES_CHILE").url
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
                    imagenUrl = ImagenesMuseosDemo.porCodigo.getValue("MUSEO_PRADO_MADRID").url
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
                    imagenUrl = ImagenesMuseosDemo.porCodigo.getValue("MUSEO_MAM_MEXICO").url
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
                    imagenUrl = ImagenesMuseosDemo.porCodigo.getValue("MUSEO_MALI_LIMA").url
                )
            )
            database.museoDao().insertarSiNoExisten(museosDemo)
            actualizarImagenesSiEsNecesario(database)

            // Precarga una sola vez para conservar los cambios del Administrador.

            val controlDao = database.controlInicializacionDao()
            val yaPrecargadoCatalogo = controlDao.existeClave(CLAVE_CATALOGO_PRECARGADO) > 0

            if (!yaPrecargadoCatalogo) {
                val articulosDemo = listOf(

                    ArticuloCatalogo(
                        id = 1L,
                        titulo = "Curso Virtual: Conservación y Restauración Textil",
                        descripcion = "Taller intensivo de 4 semanas sobre métodos científicos de preservación preventiva y cuidado de fibras ancestrales andinas.",
                        categoria = CategoriaArticulo.CURSO.name,
                        precio = 45990.0,
                        imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Museo_Colonial_Bogot%C3%A1.jpg/800px-Museo_Colonial_Bogot%C3%A1.jpg",
                        disponible = true,
                        activo = true,
                        stock = 15
                    ),
                    ArticuloCatalogo(
                        id = 2L,
                        titulo = "Diplomado: Curaduría Digital y Museología Iberoamericana",
                        descripcion = "Especialización de 6 semanas con certificación académica en diseño de salas virtuales y mediación cultural contemporánea.",
                        categoria = CategoriaArticulo.CURSO.name,
                        precio = 72000.0,
                        imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Museo_Nacional_de_Bellas_Artes_de_Chile_-_Diciembre_2016.jpg/800px-Museo_Nacional_de_Bellas_Artes_de_Chile_-_Diciembre_2016.jpg",
                        disponible = true,
                        activo = true,
                        stock = 20
                    ),

                    ArticuloCatalogo(
                        id = 3L,
                        titulo = "Catálogo Razonado: Pintura Virreinal en Iberoamérica",
                        descripcion = "Libro de tapa dura con más de 300 páginas de estudios críticos, catalogación razonada e ilustraciones a todo color.",
                        categoria = CategoriaArticulo.PUBLICACION.name,
                        precio = 28990.0,
                        imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Museo_del_Prado_2016_%2825185969599%29.jpg/800px-Museo_del_Prado_2016_%2825185969599%29.jpg",
                        disponible = true,
                        activo = true,
                        stock = 8
                    ),
                    ArticuloCatalogo(
                        id = 4L,
                        titulo = "Manual de Gestión y Conservación para Museos Locales",
                        descripcion = "Guía metodológica para la administración de archivos comunitarios, inventarios patrimoniales y protocolos de seguridad.",
                        categoria = CategoriaArticulo.PUBLICACION.name,
                        precio = 16500.0,
                        imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Palacio_de_la_Exposici%C3%B3n_%28MALI%29_Lima_Peru.jpg/800px-Palacio_de_la_Exposici%C3%B3n_%28MALI%29_Lima_Peru.jpg",
                        disponible = true,
                        activo = true,
                        stock = 12
                    ),

                    ArticuloCatalogo(
                        id = 5L,
                        titulo = "Lámina Giclée de Colección: Retablo Colonial",
                        descripcion = "Impresión de alta fidelidad en papel de algodón de 310g con tintas minerales resistentes a rayos ultravioleta.",
                        categoria = CategoriaArticulo.ARTICULO_MUSEOLOGICO.name,
                        precio = 19990.0,
                        imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Museo_Colonial_Bogot%C3%A1.jpg/800px-Museo_Colonial_Bogot%C3%A1.jpg",
                        disponible = true,
                        activo = true,
                        stock = 25
                    ),
                    ArticuloCatalogo(
                        id = 6L,
                        titulo = "Réplica Museográfica Autorizada: Cerámica Mochica",
                        descripcion = "Reproducción a escala 1:1 en arcilla curada y policromada con sello de autenticidad institucional.",
                        categoria = CategoriaArticulo.ARTICULO_MUSEOLOGICO.name,
                        precio = 34990.0,
                        imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/Museo_de_Arte_Moderno_%28MAM%29_-_Ciudad_de_M%C3%A9xico.jpg/800px-Museo_de_Arte_Moderno_%28MAM%29_-_Ciudad_de_M%C3%A9xico.jpg",
                        disponible = true,
                        activo = true,
                        stock = 5
                    ),

                    ArticuloCatalogo(
                        id = 7L,
                        titulo = "Kit de Limpieza y Estabilización de Metales Antiguos",
                        descripcion = "Compuesto por cera microcristalina Cosmoloid, solventes neutros desengrasantes y juego de pinceles técnicos.",
                        categoria = CategoriaArticulo.MATERIAL_CONSERVACION.name,
                        precio = 24900.0,
                        imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Museo_Nacional_de_Bellas_Artes_de_Chile_-_Diciembre_2016.jpg/800px-Museo_Nacional_de_Bellas_Artes_de_Chile_-_Diciembre_2016.jpg",
                        disponible = true,
                        activo = true,
                        stock = 10
                    ),
                    ArticuloCatalogo(
                        id = 8L,
                        titulo = "Sobres de Archivo de Papel Japonés con pH Neutro (50 u.)",
                        descripcion = "Fundas libres de ácido y lignina para el resguardo de material gráfico antiguo, estampas y manuscritos.",
                        categoria = CategoriaArticulo.MATERIAL_CONSERVACION.name,
                        precio = 18500.0,
                        imagenUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Museo_del_Prado_2016_%2825185969599%29.jpg/800px-Museo_del_Prado_2016_%2825185969599%29.jpg",
                        disponible = false,
                        activo = true,
                        stock = 0
                    )
                )
                database.articuloCatalogoDao().insertarSiNoExisten(articulosDemo)
                controlDao.registrar(ControlInicializacion(clave = CLAVE_CATALOGO_PRECARGADO))
            }

            // La precarga no crea ni cambia sesiones; el acceso se realiza desde Login.

        }

        /** Corrige solo las URL genéricas exactas de main, nunca imágenes personalizadas. */
        suspend fun actualizarImagenesSiEsNecesario(database: AppDatabase) {
            for ((codigo, imagen) in ImagenesMuseosDemo.porCodigo) {
                database.museoDao().actualizarImagenPorQr(codigo, imagen.anteriorUnsplash, imagen.url)
            }
        }
    }
}
