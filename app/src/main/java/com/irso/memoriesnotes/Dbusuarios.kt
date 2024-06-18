package com.irso.memoriesnotes

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbUsuarios(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "db_usuarios"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "usuarios"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_APELLIDO = "apellido"
        private const val COLUMN_TELEFONO = "telefono"
        private const val COLUMN_CORREO = "correo"
        private const val COLUMN_CONTRASENA = "contrasena"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear la tabla
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NOMBRE TEXT NOT NULL," +
                "$COLUMN_APELLIDO TEXT NOT NULL," +
                "$COLUMN_TELEFONO TEXT NOT NULL," +
                "$COLUMN_CORREO TEXT NOT NULL," +
                "$COLUMN_CONTRASENA TEXT NOT NULL)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Implementar la lógica de actualización de la base de datos si es necesario
    }

    // Verifica que no haya duplicidad de correos registrados
    fun isEmailRegistered(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_CORREO = ?", arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Insertar un nuevo usuario en la tabla
    fun insertarUsuario(nombre: String, apellido: String, telefono: String, correo: String, contrasena: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, nombre)
            put(COLUMN_APELLIDO, apellido)
            put(COLUMN_TELEFONO, telefono)
            put(COLUMN_CORREO, correo)
            put(COLUMN_CONTRASENA, contrasena)
        }

        val id = db.insert(TABLE_NAME, null, values).toInt()
        db.close()
        return id
    }

    // Valida si el usuario existe en la tabla
    fun validarUsuario(correo: String, contrasena: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_CORREO = ? AND $COLUMN_CONTRASENA = ?",
            arrayOf(correo, contrasena)
        )
        return if (cursor.moveToFirst()) {
            val userId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
            cursor.close()
            userId
        } else {
            cursor.close()
            -1
        }
    }

    // Obtiene el ID del usuario por correo
    fun getUserIdByEmail(email: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_ID FROM $TABLE_NAME WHERE $COLUMN_CORREO = ?", arrayOf(email))
        var userId = -1
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
        }
        cursor.close()
        return userId
    }

    // Obtiene el nombre del usuario por ID
    fun obtenerNombreUsuario(id: Int): String? {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT $COLUMN_NOMBRE FROM $TABLE_NAME WHERE $COLUMN_ID = ?", arrayOf(id.toString()))
        var nombre: String? = null
        if (cursor.moveToFirst()) {
            nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE))
        }
        cursor.close()
        db.close()
        return nombre
    }
}
