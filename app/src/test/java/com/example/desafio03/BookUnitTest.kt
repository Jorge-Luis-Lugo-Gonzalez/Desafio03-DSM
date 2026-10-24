package com.example.desafio03

import com.example.desafio03.model.Book
import org.junit.Assert.*
import org.junit.Test

class BookUnitTest {

    @Test
    fun testBookProgressCalculation() {
        val book = Book(
            titulo = "Cien Años de Soledad",
            autor = "Gabriel García Márquez",
            genero = "Novela",
            totalPaginas = 400,
            paginasLeidas = 200,
            estado = "Leyendo"
        )
        assertEquals(50, book.porcentajeAvance)

        book.paginasLeidas = 400
        assertEquals(100, book.porcentajeAvance)

        book.paginasLeidas = 0
        assertEquals(0, book.porcentajeAvance)
    }

    @Test
    fun testMandatoryGenres() {
        val expectedGenres = listOf(
            "Novela",
            "Ciencia ficción",
            "Historia",
            "Desarrollo personal"
        )
        assertEquals(4, Book.GENEROS_OBLIGATORIOS.size)
        assertEquals(expectedGenres, Book.GENEROS_OBLIGATORIOS)
    }

    @Test
    fun testSuggestedStates() {
        val expectedStates = listOf("Por leer", "Leyendo", "Terminado")
        assertEquals(expectedStates, Book.ESTADOS_SUGERIDOS)
    }

    @Test
    fun testTieBreakingSortingLogic() {
        // En caso de empate en calificacion, gana el que se terminó primero (menor timestamp)
        val book1 = Book(
            id = 1,
            titulo = "Libro A",
            calificacion = 5,
            fechaTerminadoTimestamp = 2000L
        )
        val book2 = Book(
            id = 2,
            titulo = "Libro B",
            calificacion = 5,
            fechaTerminadoTimestamp = 1000L
        )
        val book3 = Book(
            id = 3,
            titulo = "Libro C",
            calificacion = 4,
            fechaTerminadoTimestamp = 500L
        )

        val list = listOf(book1, book2, book3)
        val bestBook = list.sortedWith(
            compareByDescending<Book> { it.calificacion }
                .thenBy { it.fechaTerminadoTimestamp }
        ).first()

        // Libro B tiene calificacion 5 y terminó en 1000L (antes que Libro A en 2000L)
        assertEquals("Libro B", bestBook.titulo)
    }
}
