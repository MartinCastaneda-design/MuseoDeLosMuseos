package com.example.museodelosmuseos

import android.app.Application
import org.osmdroid.config.Configuration
import java.io.File

/**
 * Clase Application personalizada para inicializar dependencias globales
 * de OSMDroid a nivel de proceso antes de que se instancie cualquier vista.
 */
class MuseoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configuración requerida de OSMDroid con User Agent único y rutas de caché válidas
        val config = Configuration.getInstance()
        config.load(this, getSharedPreferences("osmdroid_prefs", MODE_PRIVATE))
        
        // Formato estricto requerido por la política de OpenStreetMap:
        config.userAgentValue = "MuseoPatrimonialApp/2.0 (DuocUC; DSY1105; eval-academica)"
        
        // Directorio de caché local seguro dentro del almacenamiento interno de la app
        val osmdroidBasePath = File(filesDir, "osmdroid")
        val osmdroidTileCache = File(osmdroidBasePath, "tiles")
        config.osmdroidBasePath = osmdroidBasePath
        config.osmdroidTileCache = osmdroidTileCache
    }
}
