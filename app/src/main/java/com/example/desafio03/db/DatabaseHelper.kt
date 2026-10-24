package com.example.desafio03.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.desafio03.model.AnnualStats
import com.example.desafio03.model.Book
import java.util.Calendar

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "desafio_reading.db"
        private const val DATABASE_VERSION = 1

        // Tabla Libros
        private const val TABLE_BOOKS = "books"
        private const val COL_ID = "id"
        private const val COL_USER_ID = "user_id"
        private const val COL_TITULO = "titulo"
        private const val COL_AUTOR = "autor"
        private const val COL_GENERO = "genero"
        private const val COL_TOTAL_PAGINAS = "total_paginas"
        private const val COL_PAGINAS_LEIDAS = "paginas_leidas"
        private const val COL_ESTADO = "estado"
        private const val COL_CALIFICACION = "calificacion"
        private const val COL_FECHA_TERMINADO = "fecha_terminado"
        private const val COL_FECHA_TIMESTAMP = "fecha_terminado_timestamp"
        private const val COL_ANIO_TERMINADO = "anio_terminado"

        // Tabla Metas
        private const val TABLE_GOALS = "user_goals"
        private const val COL_GOAL_USER_ID = "user_id"
        private const val COL_GOAL_ANIO = "anio"
        private const val COL_GOAL_META = "meta_anual"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createBooksTable = """
            CREATE TABLE $TABLE_BOOKS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID TEXT NOT NULL,
                $COL_TITULO TEXT NOT NULL,
                $COL_AUTOR TEXT NOT NULL,
                $COL_GENERO TEXT NOT NULL,
                $COL_TOTAL_PAGINAS INTEGER NOT NULL,
                $COL_PAGINAS_LEIDAS INTEGER NOT NULL,
                $COL_ESTADO TEXT NOT NULL,
                $COL_CALIFICACION INTEGER DEFAULT 0,
                $COL_FECHA_TERMINADO TEXT,
                $COL_FECHA_TIMESTAMP INTEGER DEFAULT 0,
                $COL_ANIO_TERMINADO INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createGoalsTable = """
            CREATE TABLE $TABLE_GOALS (
                $COL_GOAL_USER_ID TEXT NOT NULL,
                $COL_GOAL_ANIO INTEGER NOT NULL,
                $COL_GOAL_META INTEGER NOT NULL,
                PRIMARY KEY ($COL_GOAL_USER_ID, $COL_GOAL_ANIO)
            )
        """.trimIndent()

        db.execSQL(createBooksTable)
        db.execSQL(createGoalsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GOALS")
        onCreate(db)
    }

    // ==========================================
    // OPERACIONES DE LIBROS
    // ==========================================

    fun insertBook(book: Book): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_ID, book.userId)
            put(COL_TITULO, book.titulo)
            put(COL_AUTOR, book.autor)
            put(COL_GENERO, book.genero)
            put(COL_TOTAL_PAGINAS, book.totalPaginas)
            put(COL_PAGINAS_LEIDAS, book.paginasLeidas)
            put(COL_ESTADO, book.estado)
            put(COL_CALIFICACION, book.calificacion)
            put(COL_FECHA_TERMINADO, book.fechaTerminado)
            put(COL_FECHA_TIMESTAMP, book.fechaTerminadoTimestamp)
            put(COL_ANIO_TERMINADO, book.anioTerminado)
        }
        return db.insert(TABLE_BOOKS, null, values)
    }

    fun updateBook(book: Book): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITULO, book.titulo)
            put(COL_AUTOR, book.autor)
            put(COL_GENERO, book.genero)
            put(COL_TOTAL_PAGINAS, book.totalPaginas)
            put(COL_PAGINAS_LEIDAS, book.paginasLeidas)
            put(COL_ESTADO, book.estado)
            put(COL_CALIFICACION, book.calificacion)
            put(COL_FECHA_TERMINADO, book.fechaTerminado)
            put(COL_FECHA_TIMESTAMP, book.fechaTerminadoTimestamp)
            put(COL_ANIO_TERMINADO, book.anioTerminado)
        }
        return db.update(
            TABLE_BOOKS,
            values,
            "$COL_ID = ? AND $COL_USER_ID = ?",
            arrayOf(book.id.toString(), book.userId)
        )
    }

    fun deleteBook(bookId: Long, userId: String): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_BOOKS,
            "$COL_ID = ? AND $COL_USER_ID = ?",
            arrayOf(bookId.toString(), userId)
        )
    }

    fun getBookById(bookId: Long, userId: String): Book? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BOOKS,
            null,
            "$COL_ID = ? AND $COL_USER_ID = ?",
            arrayOf(bookId.toString(), userId),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) cursorToBook(it) else null
        }
    }

    fun getAllBooksByUser(userId: String): List<Book> {
        val books = mutableListOf<Book>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BOOKS,
            null,
            "$COL_USER_ID = ?",
            arrayOf(userId),
            null, null,
            "$COL_ID DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                books.add(cursorToBook(it))
            }
        }
        return books
    }

    fun getCurrentlyReadingBooks(userId: String): List<Book> {
        val books = mutableListOf<Book>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BOOKS,
            null,
            "$COL_USER_ID = ? AND $COL_ESTADO = ?",
            arrayOf(userId, "Leyendo"),
            null, null,
            "$COL_ID DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                books.add(cursorToBook(it))
            }
        }
        return books
    }

    fun getFinishedBooksCountInYear(userId: String, anio: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) FROM $TABLE_BOOKS 
            WHERE $COL_USER_ID = ? AND $COL_ESTADO = 'Terminado' AND $COL_ANIO_TERMINADO = ?
            """.trimIndent(),
            arrayOf(userId, anio.toString())
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    // ==========================================
    // OPERACIONES DE METAS ANUALES
    // ==========================================

    fun getAnnualGoal(userId: String, anio: Int): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_GOALS,
            arrayOf(COL_GOAL_META),
            "$COL_GOAL_USER_ID = ? AND $COL_GOAL_ANIO = ?",
            arrayOf(userId, anio.toString()),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    fun setAnnualGoal(userId: String, anio: Int, meta: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_GOAL_USER_ID, userId)
            put(COL_GOAL_ANIO, anio)
            put(COL_GOAL_META, meta)
        }
        return db.insertWithOnConflict(
            TABLE_GOALS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // ==========================================
    // ESTADÍSTICAS DEL AÑO
    // ==========================================

    fun getAnnualStats(userId: String, anio: Int): AnnualStats {
        val db = readableDatabase

        // 1. Cantidad de libros terminados por cada uno de los cuatro géneros
        val mapStats = mutableMapOf(
            "Novela" to 0,
            "Ciencia ficción" to 0,
            "Historia" to 0,
            "Desarrollo personal" to 0
        )

        val cursorGeneros = db.rawQuery(
            """
            SELECT $COL_GENERO, COUNT(*) 
            FROM $TABLE_BOOKS 
            WHERE $COL_USER_ID = ? AND $COL_ESTADO = 'Terminado' AND $COL_ANIO_TERMINADO = ?
            GROUP BY $COL_GENERO
            """.trimIndent(),
            arrayOf(userId, anio.toString())
        )

        cursorGeneros.use {
            while (it.moveToNext()) {
                val genero = it.getString(0)
                val count = it.getInt(1)
                mapStats[genero] = count
            }
        }

        // 2. Género más leído del año
        var maxCount = 0
        val topGeneros = mutableListOf<String>()
        for ((genero, count) in mapStats) {
            if (count > maxCount) {
                maxCount = count
                topGeneros.clear()
                topGeneros.add(genero)
            } else if (count == maxCount && count > 0) {
                topGeneros.add(genero)
            }
        }

        val generoMasLeido = when {
            maxCount == 0 -> "Ninguno aún"
            topGeneros.size == 1 -> "${topGeneros[0]} ($maxCount libros)"
            else -> "${topGeneros.joinToString(", ")} (empate: $maxCount libros c/u)"
        }

        // 3. Total de páginas leídas en el año
        // Suma de páginas leídas en libros terminados en este año + páginas leídas de libros actualmente en curso
        val cursorPaginas = db.rawQuery(
            """
            SELECT SUM($COL_PAGINAS_LEIDAS) 
            FROM $TABLE_BOOKS 
            WHERE $COL_USER_ID = ? AND (
                ($COL_ESTADO = 'Terminado' AND $COL_ANIO_TERMINADO = ?) 
                OR ($COL_ESTADO = 'Leyendo')
            )
            """.trimIndent(),
            arrayOf(userId, anio.toString())
        )

        val totalPaginas = cursorPaginas.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

        // 4. Libro mejor calificado del año (en caso de empate, el que se terminó primero)
        val cursorMejorLibro = db.rawQuery(
            """
            SELECT * FROM $TABLE_BOOKS 
            WHERE $COL_USER_ID = ? AND $COL_ESTADO = 'Terminado' AND $COL_ANIO_TERMINADO = ? AND $COL_CALIFICACION > 0
            ORDER BY $COL_CALIFICACION DESC, $COL_FECHA_TIMESTAMP ASC 
            LIMIT 1
            """.trimIndent(),
            arrayOf(userId, anio.toString())
        )

        val mejorLibro = cursorMejorLibro.use {
            if (it.moveToFirst()) cursorToBook(it) else null
        }

        return AnnualStats(
            anio = anio,
            statsPorGenero = mapStats,
            generoMasLeido = generoMasLeido,
            totalPaginasLeidas = totalPaginas,
            libroMejorCalificado = mejorLibro
        )
    }

    private fun cursorToBook(cursor: Cursor): Book {
        return Book(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            userId = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ID)),
            titulo = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITULO)),
            autor = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTOR)),
            genero = cursor.getString(cursor.getColumnIndexOrThrow(COL_GENERO)),
            totalPaginas = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_PAGINAS)),
            paginasLeidas = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PAGINAS_LEIDAS)),
            estado = cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO)),
            calificacion = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CALIFICACION)),
            fechaTerminado = cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA_TERMINADO)) ?: "",
            fechaTerminadoTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_FECHA_TIMESTAMP)),
            anioTerminado = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ANIO_TERMINADO))
        )
    }
}
