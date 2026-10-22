package com.example.museodelosmuseos.ui.screens.catalogo

import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import com.example.museodelosmuseos.data.local.entity.SesionActiva
import com.example.museodelosmuseos.data.model.CategoriaArticulo

/**
 * ============================================================================
 * GUÍA DE ARQUITECTURA: CatalogoUiState
 * ============================================================================
 * Modelo de estado inmutable para la Pantalla 9: Catálogo de Productos y Cursos.
 *
 * Implementa las convenciones del profesor Cristian en '102 Fuente Ejemplo Formularios':
 * - Modelo atómico [CampoFormulario] que combina valor y mensaje de error.
 * - Single Source of Truth: todos los estados de la interfaz unificados en este objeto.
 * - Getters computados para validación de formulario y filtrado reactivo de colecciones.
 * ============================================================================
 */

/**
 * Modelo de datos atómico para un campo individual del formulario de catálogo.
 * Exactamente idéntico al estándar de '102 Fuente Ejemplo Formularios'.
 *
 * @param valor Texto ingresado por el usuario en el campo.
 * @param error Mensaje explicativo si no supera la regla de negocio; null si es válido.
 */
data class CampoFormulario(
    val valor: String = "",
    val error: String? = null
) {
    /**
     * Propiedad calculada (Custom Getter): evalúa si el campo es conforme
     * y contiene texto no vacío.
     */
    val esValido: Boolean get() = error == null && valor.isNotBlank()
}

/**
 * Estado inmutable del formulario de captura y edición de artículos.
 * Utilizado por el perfil Administrador para gestionar el catálogo.
 *
 * @param id Identificador del artículo (0L si es creación nueva, > 0L si es edición).
 * @param titulo Nombre del producto o curso.
 * @param descripcion Reseña, temario o especificaciones técnicas.
 * @param categoria Clasificación cultural del artículo según [CategoriaArticulo].
 * @param precio Representación en texto del valor monetario para validación reactiva.
 * @param stock Cantidad de cupos o unidades en inventario.
 * @param imagenUrl URL de la imagen referencial.
 * @param disponible Indica si los clientes pueden solicitarlo o inscribirse.
 * @param activo Permite la baja lógica para preservar historial sin borrar de la base de datos.
 * @param intentoEnviar Marca si el usuario ya presionó el botón de guardar para mostrar errores.
 */
data class FormularioArticuloState(
    val id: Long = 0L,
    val titulo: CampoFormulario = CampoFormulario(),
    val descripcion: CampoFormulario = CampoFormulario(),
    val categoria: CategoriaArticulo = CategoriaArticulo.CURSO,
    val precio: CampoFormulario = CampoFormulario(),
    val stock: CampoFormulario = CampoFormulario(),
    val imagenUrl: String = "",
    val disponible: Boolean = true,
    val activo: Boolean = true,
    val intentoEnviar: Boolean = false
) {
    /** Determina si el formulario se encuentra en modo de edición o de creación. */
    val esEdicion: Boolean get() = id > 0L

    /**
     * Getter computado que evalúa la validez completa del formulario:
     * Requiere que todos los campos requeridos tengan valor y que su propiedad [esValido] sea true.
     */
    val formularioValido: Boolean
        get() = titulo.esValido &&
                descripcion.esValido &&
                precio.esValido &&
                stock.esValido
}

/**
 * Estado global inmutable de la pantalla de catálogo.
 * Fuente única de verdad consumida por la interfaz de usuario en Compose.
 */
data class CatalogoUiState(
    // 1. Colección de artículos y banderas de sincronización
    val articulos: List<ArticuloCatalogo> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,

    // 2. Parámetros de búsqueda y filtros
    val terminoBusqueda: String = "",
    val categoriaSeleccionada: CategoriaArticulo? = null, // null indica visualización de 'Todos'

    // 3. Sesión activa y control de visibilidad por rol
    val sesion: SesionActiva? = null,
    val mostrarSoloActivos: Boolean = true, // El Administrador puede alternar para ver inactivos

    // 4. Selección de artículo para navegación a Pantalla 10 (Detalle y Compra)
    val articuloSeleccionadoId: Long? = null,

    // 5. Gestión del formulario de administración (Alta y Edición)
    val mostrarDialogoFormulario: Boolean = false,
    val formulario: FormularioArticuloState = FormularioArticuloState(),

    // 6. Retroalimentación visual
    val mensajeError: String? = null,
    val mensajeConfirmacion: String? = null
) {
    /**
     * Determina si el usuario autenticado tiene el rol de Administrador.
     */
    val esAdministrador: Boolean
        get() = sesion?.rol?.equals("ADMINISTRADOR", ignoreCase = true) == true

    /**
     * Colección de artículos resultante de aplicar en tiempo real:
     * 1. Reglas de visibilidad por rol (Clientes solo ven activos; Admin puede ver todos).
     * 2. Filtro por categoría temática seleccionada.
     * 3. Filtro de búsqueda por texto en título o descripción.
     */
    val articulosFiltrados: List<ArticuloCatalogo>
        get() {
            return articulos.filter { articulo ->
                // Regla de visibilidad por rol
                val cumpleVisibilidad = if (esAdministrador) {
                    if (mostrarSoloActivos) articulo.activo else true
                } else {
                    articulo.activo
                }

                // Regla de categoría
                val cumpleCategoria = categoriaSeleccionada == null ||
                        articulo.categoria.equals(categoriaSeleccionada.name, ignoreCase = true)

                // Regla de búsqueda por coincidencia textual
                val cumpleBusqueda = terminoBusqueda.isBlank() ||
                        articulo.titulo.contains(terminoBusqueda.trim(), ignoreCase = true) ||
                        articulo.descripcion.contains(terminoBusqueda.trim(), ignoreCase = true)

                cumpleVisibilidad && cumpleCategoria && cumpleBusqueda
            }
        }
}
