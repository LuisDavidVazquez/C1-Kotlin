package com.example.myapplication.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StudentDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "StudentDB"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "students"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
        
        // Insertar datos de ejemplo
        insertInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun insertInitialData(db: SQLiteDatabase) {
        val students = listOf(
            "Ana López",
            "Carlos Pérez",
            "María González",
            "José Rodríguez"
        )
        
        students.forEach { name ->
            val values = ContentValues().apply {
                put(COLUMN_NAME, name)
            }
            db.insert(TABLE_NAME, null, values)
        }
    }

    fun getAllStudents(): List<String> {
        val students = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_NAME),
            null,
            null,
            null,
            null,
            "$COLUMN_NAME ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                students.add(name)
            }
        }
        cursor.close()
        return students
    }

    fun addStudent(name: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
        }
        val success = db.insert(TABLE_NAME, null, values) != -1L
        db.close()
        return success
    }
} 