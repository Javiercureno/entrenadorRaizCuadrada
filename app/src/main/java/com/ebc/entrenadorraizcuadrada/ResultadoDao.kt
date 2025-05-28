package com.ebc.entrenadorraizcuadrada

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ebc.entrenadorraizcuadrada.db.Resultado

@Dao
interface ResultadoDao {
    @Insert
    suspend fun insertar(resultado: Resultado)

    @Query("SELECT * FROM Resultado ORDER BY fechaHora DESC LIMIT 20")
    suspend fun obtenerUltimos(): List<Resultado>
}