package com.example.museodelosmuseos

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.museodelosmuseos.ui.MuseoApp
import com.example.museodelosmuseos.ui.theme.MuseoDeLosMuseosTheme
import org.osmdroid.config.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.museodelosmuseos.data.local.AppDatabase
import com.example.museodelosmuseos.data.repository.MuseoRepository
import com.example.museodelosmuseos.data.local.entity.PreferenciasUsuario
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Punto de entrada principal de la aplicación.
 * Sigue la arquitectura Single-Activity recomendada para Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración requerida de OSMDroid previa a la creación de vistas (política de OpenStreetMap)
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "MuseoDeLosMuseosApp/1.0 (cl.duoc.museo; contact: boxin@student.duoc.cl)"

        // Dibuja la app detrás de las barras del sistema (edge-to-edge)
        enableEdgeToEdge()

        setContent {
            val repo = remember { MuseoRepository(AppDatabase.obtenerInstancia(applicationContext)) }
            val preferenciasFlow = remember { repo.observarSesion().flatMapLatest { sesion ->
                if (sesion == null) flowOf<PreferenciasUsuario?>(null) else repo.observarPreferencias(sesion.usuarioId)
            } }
            val preferencias by preferenciasFlow.collectAsStateWithLifecycle(initialValue = null)
            val densidad = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(densidad.density,
                densidad.fontScale * if (preferencias?.textoGrande == true) 1.15f else 1f)) {
            MuseoDeLosMuseosTheme {
                MuseoApp()
            }
            }
        }
    }
}
