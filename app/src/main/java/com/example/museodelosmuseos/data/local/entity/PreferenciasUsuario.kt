package com.example.museodelosmuseos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "preferencias_usuario", foreignKeys = [ForeignKey(
    entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"], onDelete = ForeignKey.CASCADE
)])
data class PreferenciasUsuario(
    @PrimaryKey val usuarioId: Long,
    val indicadorAvisos: Boolean = true,
    val textoGrande: Boolean = false
)
