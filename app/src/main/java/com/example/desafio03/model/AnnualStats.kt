package com.example.desafio03.model

data class AnnualStats(
    val anio: Int,
    val statsPorGenero: Map<String, Int>, // Género -> cantidad terminados
    val generoMasLeido: String,
    val totalPaginasLeidas: Int,
    val libroMejorCalificado: Book?
)
