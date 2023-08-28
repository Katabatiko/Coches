package com.example.autos.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//@Database(entities = [DbAuto::class, DbRefueling::class], version = 1)
@Database(entities = [DbAuto::class, DbRefueling::class, DbGasto::class, DbItem::class], version = 2, autoMigrations = [AutoMigration (from = 1, to = 2)])
abstract class AutosDatabase: RoomDatabase() {
    abstract val autosDao: AutosDao

    companion object {
        private lateinit var INSTANCE: AutosDatabase

        fun getDatabase(context: Context): AutosDatabase {
            synchronized(AutosDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AutosDatabase::class.java,
                        "autos"
                    )
                    .build()
                }
            }
            return INSTANCE
        }
    }
}
