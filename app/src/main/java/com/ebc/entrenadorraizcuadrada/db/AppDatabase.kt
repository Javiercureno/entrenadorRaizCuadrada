package com.ebc.entrenadorraizcuadrada.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ebc.entrenadorraizcuadrada.ResultadoDao

@Database(entities = [Resultado::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resultadoDao(): ResultadoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "resultados_db"
                ).build().also { INSTANCE = it }
            }
    }
}