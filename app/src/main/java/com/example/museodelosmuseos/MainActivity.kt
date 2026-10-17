package com.example.museodelosmuseos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.museodelosmuseos.ui.MuseoApp
import com.example.museodelosmuseos.ui.theme.MuseoDeLosMuseosTheme

/**
 * Punto de entrada principal de la aplicación.
 * Sigue la arquitectura Single-Activity recomendada para Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dibuja la app detrás de las barras del sistema (edge-to-edge)
        enableEdgeToEdge()

        setContent {
            MuseoDeLosMuseosTheme {
                MuseoApp()
            }
        }
    }
    )
}