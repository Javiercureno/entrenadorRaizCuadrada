package com.ebc.entrenadorraizcuadrada.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Resultado(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val numero: Int,
    val respuesta: Int,
    val correcto: Boolean,
    val fechaHora: Long
)