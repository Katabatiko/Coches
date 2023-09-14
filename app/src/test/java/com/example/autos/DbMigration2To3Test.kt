package com.example.autos

import android.util.Log
import androidx.room.migration.AutoMigrationSpec
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.autos.data.local.AutosDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.jvm.Throws

@RunWith(AndroidJUnit4::class)
class DbMigration2To3Test {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AutosDatabase::class.java,
        listOf<AutoMigrationSpec>(),
        FrameworkSQLiteOpenHelperFactory()
    )


    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        var db = helper.createDatabase(TEST_DB, 2).apply {
            // Database has schema version 1. Insert some data using SQL queries.
            // You can't use DAO classes because they expect the latest schema.
            execSQL("""insert into dbauto values 1, "Seat", "600", "M-682459", "1964/02/19", 54623, 60000, "1982/09/11" """)
            execSQL("""insert into dbrefueling values 0, 1, "1982/09/20", 55000, 22.68, 0.48, 10.89, 1 """)
            execSQL("""insert into dbrefueling values 0, 1, "1982/10/01", 55450, 22.68, 0.48, 10.89, 1 """)
            execSQL("""insert into dbrefueling values 0, 1, "1982/10/20", 55850, 22.68, 0.48, 10.89, 1 """)
            execSQL("""insert into dbrefueling values 0, 1, "1982/11/11", 56350, 22.68, 0.48, 10.89, 1 """)
            execSQL("""insert into dbrefueling values 0, 1, "1982/11/31", 56700, 22.68, 0.48, 10.89, 1 """)
            execSQL("""insert into dbrefueling values 0, 1, "1982/12/15", 57001, 22.68, 0.48, 10.89, 1 """)
            execSQL("""insert into dbrefueling values 0, 1, "1982/12/31", 57500, 22.68, 0.48, 10.89, 1 """)

            // Prepare for the next version.
            close()
        }

        // Re-open the database with version 3 and provide
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true).apply {
            val cursor = query("select * from dbrefueling")
            while (cursor.moveToNext()){
                Log.d("xxMigrationTest", cursor.getInt(8).toString())
            }
            val itemsColumns = query("select * from dbitem")
            Log.d("xxMigrationTest", itemsColumns.columnNames.toString())
        }



    }
}