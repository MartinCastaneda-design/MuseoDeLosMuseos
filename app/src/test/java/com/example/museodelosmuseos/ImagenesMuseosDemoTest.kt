package com.example.museodelosmuseos

import com.example.museodelosmuseos.data.model.ImagenesMuseosDemo
import org.junit.Assert.*
import org.junit.Test

class ImagenesMuseosDemoTest {
    @Test fun fotografiasLocalesTienenPrioridadParaLasDireccionesDemoConocidas() {
        assertEquals(5, ImagenesMuseosDemo.porCodigo.size)
        for ((codigo, imagen) in ImagenesMuseosDemo.porCodigo) {
            for (url in listOf("", imagen.url, imagen.anteriorWikimedia, imagen.anteriorUnsplash))
                assertTrue(codigo, ImagenesMuseosDemo.usarFotografiaLocal(codigo, url))
        }
    }
    @Test fun imagenPersonalizadaNoSeConfundeConUnaDireccionDemo() {
        for (codigo in ImagenesMuseosDemo.porCodigo.keys) {
            assertFalse(ImagenesMuseosDemo.usarFotografiaLocal(codigo, "https://example.com/museo.jpg"))
            assertFalse(ImagenesMuseosDemo.usarFotografiaLocal(codigo, "https://images.unsplash.com/photo-personalizada"))
        }
    }
    @Test fun museoDesconocidoNoRecibeUnaFotografiaDeOtroMuseo() {
        assertFalse(ImagenesMuseosDemo.usarFotografiaLocal("OTRO_MUSEO", ""))
        assertFalse(ImagenesMuseosDemo.usarFotografiaLocal("OTRO_MUSEO", ImagenesMuseosDemo.porCodigo.values.first().url))
    }
}
