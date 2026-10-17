package com.example.museodelosmuseos

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.museodelosmuseos.ui.MuseoApp
import com.example.museodelosmuseos.ui.theme.MuseoDeLosMuseosTheme
import org.osmdroid.config.Configuration

/**
 * Punto de entrada principal de la aplicación.
 * Sigue la arquitectura Single-Activity recomendada para Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración requerida de OSMDroid previa a la creación de vistas (política de OpenStreetMap)
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "MuseoDeLosMuseosApp/1.0 (cl.duoc.museo; contact: boxin@student.duoc.cl)"

        // Dibuja la app detrás de las barras del sistema (edge-to-edge)
        enableEdgeToEdge()

        setContent {
            MuseoDeLosMuseosTheme {
                MuseoApp()
            }
        }
    }
}