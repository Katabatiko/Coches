package com.example.autos.data.local

import android.content.Context
import android.database.Cursor
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.util.useCursor
import androidx.sqlite.db.SupportSQLiteDatabase

//@Database(entities = [DbAuto::class, DbRefueling::class], version = 1)
//@Database(entities = [DbAuto::class, DbRefueling::class, DbGasto::class, DbItem::class], version = 2, autoMigrations = [AutoMigration (from = 1, to = 2)])
@Database(
    entities = [DbAuto::class, DbRefueling::class, DbGasto::class, DbItem::class],
    version = 3,
    autoMigrations = [
        AutoMigration (from = 1, to = 2),
        AutoMigration (
            from = 2,
            to = 3,
            spec = AutosDatabase.AutoMigration2To3::class
        ),
    ]
)
abstract class AutosDatabase: RoomDatabase() {
    abstract val autosDao: AutosDao

    @RenameColumn("DbItem", fromColumnName = "marca", toColumnName = "detalle")
    class AutoMigration2To3: AutoMigrationSpec {

        override fun onPostMigrate(db: SupportSQLiteDatabase) {

            val cars: Cursor = db.query("select * from dbauto")
            cars.useCursor { autos ->
                while (autos.moveToNext()) {
                    val carId = autos.getInt(0)
                    val carInitialKms = autos.getInt(5)
                    val refuels =
                        db.query("select * from DbRefueling where cocheId= $carId order by kms asc")
                    refuels.useCursor {
                        var prevKms = carInitialKms
                        while (it.moveToNext()) {
                            val refuelId = it.getInt(0)
                            val kms = it.getInt(3)
                            db.execSQL("update DbRefueling set recorrido=${kms - prevKms} where refuelId=$refuelId")
                            prevKms = kms
                        }
                    }
                }
            }
        }
    }

    companion object {
        // El valor de una variable volátil nunca se almacena en caché,
        // y todas las lecturas y escrituras son desde y hacia la memoria principal.
        // Estas funciones ayudan a garantizar que el valor de Instance esté siempre actualizado
        // y sea el mismo para todos los subprocesos de ejecución.
        // Eso significa que los cambios realizados por un subproceso en Instance
        // son visibles de inmediato para todos los demás subprocesos.
        @Volatile
        private var INSTANCE: AutosDatabase? = null

        fun getDatabase(context: Context): AutosDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AutosDatabase::class.java,
                    "autos"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

//    companion object {
//        private lateinit var INSTANCE: AutosDatabase
//
//        fun getDatabase(context: Context): AutosDatabase {
//            synchronized(AutosDatabase::class.java) {
//                if (!::INSTANCE.isInitialized) {
//                    INSTANCE = Room.databaseBuilder(
//                        context.applicationContext,
//                        AutosDatabase::class.java,
//                        "autos"
//                    )
//                    .build()
//                }
//            }
//            return INSTANCE
//        }
//    }
}
