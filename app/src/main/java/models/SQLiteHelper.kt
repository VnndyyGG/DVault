package models

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper(context: Context) : SQLiteOpenHelper (context,DATABASE_NAME, null,  DATABASE_VERSION) {


    companion object{

    private const val DATABASE_NAME = "MiBaseDeDatos.db"
    private const val DATABASE_VERSION = 1

    private const val TABLE_NAME = "Usuarios"

    private const val COLUMN_ID = "id"

    private const val COLUMN_NOMBRE = "nombre"

    private const val COLUMN_EMAIL = "email"

}

    override fun onCreate(db: SQLiteDatabase?) {
        TODO("Not yet implemented")
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME {
                $COLUMN_ID INTEGER PRIMARY KEY AUTOICREMENT,
                $COLUMN_NOMBRE TEXT,
                $COLUMN_EMAIL TEXT
            }
        """.trimIndent()
        db.execSQL(createTable)
    }





}