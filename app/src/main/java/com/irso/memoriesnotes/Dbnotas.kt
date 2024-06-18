package com.irso.memoriesnotes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Nota(val id: Long, val titulo: String, val contenido: String, val imagen: ByteArray?, val usuarioId: Int) {
    override fun toString(): String {
        return titulo
    }
}

class DbNotas(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "db_notas"
        private const val DATABASE_VERSION = 4 // Incrementa la versión de la base de datos
        private const val TABLE_NAME = "notas"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_TITULO = "titulo"
        private const val COLUMN_CONTENIDO = "contenido"
        private const val COLUMN_IMAGEN = "imagen"
        private const val COLUMN_USUARIO_ID = "usuario_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_TITULO TEXT," +
                "$COLUMN_CONTENIDO TEXT NOT NULL," +
                "$COLUMN_IMAGEN BLOB," +
                "$COLUMN_USUARIO_ID INTEGER NOT NULL)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_TITULO TEXT")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_IMAGEN BLOB")
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_USUARIO_ID INTEGER NOT NULL")
        }
    }

    fun agregarNota(titulo: String, contenido: String, imagen: ByteArray?, usuarioId: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITULO, titulo)
            put(COLUMN_CONTENIDO, contenido)
            put(COLUMN_IMAGEN, imagen)
            put(COLUMN_USUARIO_ID, usuarioId)
        }
        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun obtenerNotas(usuarioId: Int): List<Nota> {
        val notas = mutableListOf<Nota>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_USUARIO_ID = ?", arrayOf(usuarioId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val nota = Nota(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    titulo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITULO)),
                    contenido = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENIDO)),
                    imagen = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGEN)),
                    usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USUARIO_ID))
                )
                notas.add(nota)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notas
    }

    fun obtenerNotaPorId(id: Long, usuarioId: Int): Nota? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ? AND $COLUMN_USUARIO_ID = ?",
            arrayOf(id.toString(), usuarioId.toString())
        )
        var nota: Nota? = null
        if (cursor.moveToFirst()) {
            val titulo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITULO))
            val contenido = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENIDO))
            val imagen = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGEN))
            nota = Nota(id, titulo, contenido, imagen, usuarioId)
        }
        cursor.close()
        db.close()
        return nota
    }

    fun actualizarNota(id: Long, titulo: String, contenido: String, imagen: ByteArray?, usuarioId: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITULO, titulo)
            put(COLUMN_CONTENIDO, contenido)
            put(COLUMN_IMAGEN, imagen)
            put(COLUMN_USUARIO_ID, usuarioId)
        }
        val rowsAffected = db.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ? AND $COLUMN_USUARIO_ID = ?",
            arrayOf(id.toString(), usuarioId.toString())
        )
        db.close()
        return rowsAffected > 0
    }

    fun eliminarNota(id: Long, usuarioId: Int): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(
            TABLE_NAME,
            "$COLUMN_ID = ? AND $COLUMN_USUARIO_ID = ?",
            arrayOf(id.toString(), usuarioId.toString())
        )
        db.close()
        return rowsDeleted > 0
    }

    fun buscarNotas(query: String, usuarioId: Int): List<Nota> {
        val db = readableDatabase
        val notas = mutableListOf<Nota>()
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_USUARIO_ID = ? AND ($COLUMN_TITULO LIKE ? OR $COLUMN_CONTENIDO LIKE ?)",
            arrayOf(usuarioId.toString(), "%$query%", "%$query%"),
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val titulo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITULO))
                val contenido = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENIDO))
                val imagen = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_IMAGEN))
                val nota = Nota(id, titulo, contenido, imagen, usuarioId)
                notas.add(nota)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return notas
    }
}
