package com.example.desafio03.model

import java.io.Serializable

data class Book(
    var id: Long = 0,
    var userId: String = "",
    var titulo: String = "",
    var autor: String = "",
    var genero: String = "", // "Novela", "Ciencia ficción", "Historia", "Desarrollo personal"
    var totalPaginas: Int = 0,
    var paginasLeidas: Int = 0,
    var estado: String = "Por leer", // "Por leer", "Leyendo", "Terminado"
    var calificacion: Int = 0, // 1 to 5 (solo si estado es Terminado)
    var fechaTerminado: String = "", // Formato legible "dd/MM/yyyy HH:mm"
    var fechaTerminadoTimestamp: Long = 0L,
    var anioTerminado: Int = 0
) : Serializable {

    val porcentajeAvance: Int
        get() {
            if (totalPaginas <= 0) return 0
            val pct = (paginasLeidas.toDouble() / totalPaginas.toDouble()) * 100.0
            return pct.toInt().coerceIn(0, 100)
        }

    companion object {
        val GENEROS_OBLIGATORIOS = listOf(
            "Novela",
            "Ciencia ficción",
            "Historia",
            "Desarrollo personal"
        )

        val ESTADOS_SUGERIDOS = listOf(
            "Por leer",
            "Leyendo",
            "Terminado"
        )
    }
}
