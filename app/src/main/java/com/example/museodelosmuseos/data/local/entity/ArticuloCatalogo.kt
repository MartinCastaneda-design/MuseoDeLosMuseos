package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para representar los productos y cursos del catálogo (Pantalla 9).
 * Se mantiene un identificador estable y un precio numérico para cálculos futuros
 * en las pantallas 10 y 12 (compras/reservas), sin duplicar representaciones formateadas.
 *
 * @param id Identificador único y estable del artículo.
 * @param titulo Nombre comercial o académico del artículo.
 * @param descripcion Detalle técnico, temario o especificaciones.
 * @param categoria Clave de la categoría (ver [com.example.museodelosmuseos.data.model.CategoriaArticulo]).
 * @param precio Valor monetario numérico (CLP o moneda base).
 * @param imagenUrl URL de la imagen referencial cargada vía Coil.
 * @param disponible Indica si hay cupos/stock para selección y reserva por el Cliente.
 * @param activo Permite al Administrador desactivar o reactivar el artículo en el catálogo.
 * @param stock Cantidad de unidades o cupos disponibles.
 */
@Entity(
    tableName = "articulos_catalogo",
    indices = [
        Index(value = ["categoria"]),
        Index(value = ["activo"])
    ]
)
data class ArticuloCatalogo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val precio: Double,
    val imagenUrl: String = "",
    val disponible: Boolean = true,
    val activo: Boolean = true,
    val stock: Int = 10
)
