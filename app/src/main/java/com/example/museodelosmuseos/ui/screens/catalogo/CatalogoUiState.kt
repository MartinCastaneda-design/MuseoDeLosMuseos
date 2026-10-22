package com.example.museodelosmuseos.ui.screens.catalogo

import com.example.museodelosmuseos.data.local.entity.ArticuloCatalogo
import com.example.museodelosmuseos.data.local.entity.SesionActiva
import com.example.museodelosmuseos.data.model.CategoriaArticulo

data class CampoFormulario(
    val valor: String = "",
    val error: String? = null
) {

    val esValido: Boolean get() = error == null && valor.isNotBlank()
}

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

    val esEdicion: Boolean get() = id > 0L

    val formularioValido: Boolean
        get() = titulo.esValido &&
                descripcion.esValido &&
                precio.esValido &&
                stock.esValido
}

data class CatalogoUiState(

    val articulos: List<ArticuloCatalogo> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,

    val terminoBusqueda: String = "",
    val categoriaSeleccionada: CategoriaArticulo? = null, // null indica visualización de 'Todos'

    val sesion: SesionActiva? = null,
    val mostrarSoloActivos: Boolean = true, // El Administrador puede alternar para ver inactivos

    val articuloSeleccionadoId: Long? = null,

    val mostrarDialogoFormulario: Boolean = false,
    val formulario: FormularioArticuloState = FormularioArticuloState(),

    val mensajeError: String? = null,
    val mensajeConfirmacion: String? = null
) {

    val esAdministrador: Boolean
        get() = sesion?.rol?.equals("ADMINISTRADOR", ignoreCase = true) == true

    // El Cliente solo ve artículos activos; el Administrador puede consultar todos.
    val articulosFiltrados: List<ArticuloCatalogo>
        get() {
            return articulos.filter { articulo ->

                val cumpleVisibilidad = if (esAdministrador) {
                    if (mostrarSoloActivos) articulo.activo else true
                } else {
                    articulo.activo
                }

                val cumpleCategoria = categoriaSeleccionada == null ||
                        articulo.categoria.equals(categoriaSeleccionada.name, ignoreCase = true)

                val cumpleBusqueda = terminoBusqueda.isBlank() ||
                        articulo.titulo.contains(terminoBusqueda.trim(), ignoreCase = true) ||
                        articulo.descripcion.contains(terminoBusqueda.trim(), ignoreCase = true)

                cumpleVisibilidad && cumpleCategoria && cumpleBusqueda
            }
        }
}
