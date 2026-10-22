package com.example.museodelosmuseos.data.model

/**
 * Categorías oficiales del Catálogo de Productos y Cursos (Pantalla 9).
 * Permite filtrar y clasificar los artículos culturales disponibles.
 *
 * - CURSO: Formaciones, talleres y diplomados de museología y patrimonio.
 * - PUBLICACION: Libros, catálogos razonados y manuales de investigación.
 * - ARTICULO_MUSEOLOGICO: Réplicas autorizadas, láminas de archivo y piezas de colección.
 * - MATERIAL_CONSERVACION: Insumos técnicos, kits de restauración y resguardo patrimonial.
 */
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
