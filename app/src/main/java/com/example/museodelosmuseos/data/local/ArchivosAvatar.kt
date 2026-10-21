package com.example.museodelosmuseos.data.local

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Copias privadas y duraderas; el proveedor solo comparte la carpeta temporal de cámara. */
class ArchivosAvatar(context: Context) {
    private val app = context.applicationContext
    fun crearCaptura(): File = File.createTempFile("captura-", ".jpg", File(app.cacheDir, "avatar-camara").apply { mkdirs() })
    fun uriCaptura(archivo: File): Uri = FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", archivo)
    fun borrarCaptura(ruta: String) {
        val archivo = File(ruta)
        if (archivo.canonicalFile.parentFile == File(app.cacheDir, "avatar-camara").canonicalFile) archivo.delete()
    }
    fun modelo(avatar: String): Any? = if (avatar.startsWith("avatar-local/")) File(app.filesDir, avatar) else null

    suspend fun importar(uri: Uri, usuarioId: Long): String = withContext(Dispatchers.IO) {
        val relativa = "avatar-local/$usuarioId/${UUID.randomUUID()}.img"
        val destino = File(app.filesDir, relativa)
        destino.parentFile!!.mkdirs()
        try {
            app.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida ->
                    val buffer = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val leidos = entrada.read(buffer)
                        if (leidos < 0) break
                        total += leidos
                        require(total <= 10 * 1024 * 1024) { "La imagen debe pesar como máximo 10 MB." }
                        salida.write(buffer, 0, leidos)
                    }
                }
            } ?: error("No fue posible leer la imagen seleccionada.")
            val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(destino.path, medidas)
            require(medidas.outWidth > 0 && medidas.outHeight > 0) { "El archivo no contiene una imagen válida." }
            relativa
        } catch (e: Exception) { destino.delete(); throw e }
    }
    suspend fun borrarBorrador(avatar: String, usuarioId: Long) = withContext(Dispatchers.IO) {
        if (avatar.startsWith("avatar-local/$usuarioId/") && !avatar.contains("..")) File(app.filesDir, avatar).delete()
    }
}
