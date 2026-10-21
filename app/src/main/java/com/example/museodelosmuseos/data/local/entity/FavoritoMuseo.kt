package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favoritos_museo",
    indices = [
        Index(value = ["usuarioId", "museoId"], unique = true),
        Index(value = ["museoId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Museo::class,
            parentColumns = ["id"],
            childColumns = ["museoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FavoritoMuseo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val usuarioId: Long,
    val museoId: Long
)
