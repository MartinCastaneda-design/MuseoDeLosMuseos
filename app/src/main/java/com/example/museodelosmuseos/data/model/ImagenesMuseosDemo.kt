package com.example.museodelosmuseos.data.model

/** Direcciones exactas del conjunto académico; las URL personalizadas quedan fuera. */
data class ImagenMuseoDemo(val url: String, val anteriorWikimedia: String, val anteriorUnsplash: String)

object ImagenesMuseosDemo {
    val porCodigo = mapOf(
        "MUSEO_COLONIAL_BOGOTA" to ImagenMuseoDemo("https://upload.wikimedia.org/wikipedia/commons/f/fd/Fachada_del_Museo_de_Arte_Colonial.JPG", "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Museo_Colonial_Bogot%C3%A1.jpg/800px-Museo_Colonial_Bogot%C3%A1.jpg", "https://images.unsplash.com/photo-1518998053901-5348d3961a04?w=800&auto=format&fit=crop&q=80"),
        "MUSEO_BELLAS_ARTES_CHILE" to ImagenMuseoDemo("https://upload.wikimedia.org/wikipedia/commons/0/0f/Museo_Nacional_de_Bellas_Artes_de_Chile.jpg", "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Museo_Nacional_de_Bellas_Artes_de_Chile_-_Diciembre_2016.jpg/800px-Museo_Nacional_de_Bellas_Artes_de_Chile_-_Diciembre_2016.jpg", "https://images.unsplash.com/photo-1565008447742-97f6f38c985c?w=800&auto=format&fit=crop&q=80"),
        "MUSEO_PRADO_MADRID" to ImagenMuseoDemo("https://upload.wikimedia.org/wikipedia/commons/6/68/Museo_del_Prado_2016_%2825185969599%29.jpg", "https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Museo_del_Prado_2016_%2825185969599%29.jpg/800px-Museo_del_Prado_2016_%2825185969599%29.jpg", "https://images.unsplash.com/photo-1582555172866-f73bb12a2ab3?w=800&auto=format&fit=crop&q=80"),
        "MUSEO_MAM_MEXICO" to ImagenMuseoDemo("https://upload.wikimedia.org/wikipedia/commons/c/cb/Museo_de_arte_Moderno.jpg", "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/Museo_de_Arte_Moderno_%28MAM%29_-_Ciudad_de_M%C3%A9xico.jpg/800px-Museo_de_Arte_Moderno_%28MAM%29_-_Ciudad_de_M%C3%A9xico.jpg", "https://images.unsplash.com/photo-1544531586-fde5298cdd40?w=800&auto=format&fit=crop&q=80"),
        "MUSEO_MALI_LIMA" to ImagenMuseoDemo("https://upload.wikimedia.org/wikipedia/commons/1/16/Museo_de_arte_de_Lima.jpg", "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Palacio_de_la_Exposici%C3%B3n_%28MALI%29_Lima_Peru.jpg/800px-Palacio_de_la_Exposici%C3%B3n_%28MALI%29_Lima_Peru.jpg", "https://images.unsplash.com/photo-1572953109213-3be62398eb95?w=800&auto=format&fit=crop&q=80")
    )

    fun usarFotografiaLocal(codigo: String, url: String): Boolean {
        val imagen = porCodigo[codigo.uppercase(java.util.Locale.ROOT)] ?: return false
        return url.isBlank() || url.trim() in setOf(imagen.url, imagen.anteriorWikimedia, imagen.anteriorUnsplash)
    }
}
