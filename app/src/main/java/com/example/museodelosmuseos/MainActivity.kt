package com.example.museodelosmuseos

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.museodelosmuseos.ui.MuseoApp
import com.example.museodelosmuseos.ui.theme.MuseoDeLosMuseosTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "MuseoDeLosMuseosApp/1.0 (cl.duoc.museo; contact: boxin@student.duoc.cl)"

        enableEdgeToEdge()

        setContent {
            MuseoDeLosMuseosTheme {
                MuseoApp()
            }
        }
    }
}
