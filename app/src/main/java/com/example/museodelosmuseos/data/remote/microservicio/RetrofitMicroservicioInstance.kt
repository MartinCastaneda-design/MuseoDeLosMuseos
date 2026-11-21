package com.example.museodelosmuseos.data.remote.microservicio

import android.os.Build
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitMicroservicioInstance {

    private fun resolverUrlPorDefecto(): String {
        val esEmulador = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
        return if (esEmulador) "http://10.0.2.2:8080/" else "http://localhost:8080/"
    }

    private var baseUrlActual: String = resolverUrlPorDefecto()

    private var retrofitInstancia: Retrofit = construirRetrofit(baseUrlActual)

    var api: SolicitudMicroservicioApi = retrofitInstancia.create(SolicitudMicroservicioApi::class.java)
        private set

    private fun construirRetrofit(url: String): Retrofit {
        val urlNormalizada = if (url.endsWith("/")) url else "$url/"
        return Retrofit.Builder()
            .baseUrl(urlNormalizada)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun configurarBaseUrl(nuevaUrl: String) {
        if (nuevaUrl.isNotBlank()) {
            val normalizada = if (nuevaUrl.endsWith("/")) nuevaUrl else "$nuevaUrl/"
            baseUrlActual = normalizada
            retrofitInstancia = construirRetrofit(normalizada)
            api = retrofitInstancia.create(SolicitudMicroservicioApi::class.java)
        }
    }

    fun obtenerBaseUrl(): String = baseUrlActual
}
