package com.example.museodelosmuseos

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.museodelosmuseos.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.osmdroid.config.Configuration
import java.io.File

class MuseoApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        val config = Configuration.getInstance()
        config.load(this, getSharedPreferences("osmdroid_prefs", MODE_PRIVATE))

        config.userAgentValue = "MuseoPatrimonialApp/2.0 (DuocUC; DSY1105; eval-academica)"

        val osmdroidBasePath = File(filesDir, "osmdroid")
        val osmdroidTileCache = File(osmdroidBasePath, "tiles")
        config.osmdroidBasePath = osmdroidBasePath
        config.osmdroidTileCache = osmdroidTileCache

        CoroutineScope(Dispatchers.IO).launch {
            // El callback de Room centraliza la inicialización y no depende del correo editable.
            AppDatabase.obtenerInstancia(this@MuseoApplication).openHelper.writableDatabase
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header(
                                "User-Agent",
                                "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 MuseoDeLosMuseos/1.0"
                            )
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .build()
    }
}
