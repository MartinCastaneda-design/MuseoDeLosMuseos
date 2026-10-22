package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// El precio numérico permite calcular compras sin usar texto formateado.
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
