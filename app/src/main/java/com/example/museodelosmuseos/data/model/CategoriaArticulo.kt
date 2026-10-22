package com.example.museodelosmuseos.data.model

enum class CategoriaArticulo(val etiqueta: String) {
    CURSO("Cursos"),
    PUBLICACION("Publicaciones"),
    ARTICULO_MUSEOLOGICO("Artículos Museológicos"),
    MATERIAL_CONSERVACION("Materiales de Conservación");

    companion object {
        fun desdeTexto(texto: String): CategoriaArticulo? {
            return entries.firstOrNull {
                it.name.equals(texto, ignoreCase = true) || it.etiqueta.equals(texto, ignoreCase = true)
            }
        }
    }
}
