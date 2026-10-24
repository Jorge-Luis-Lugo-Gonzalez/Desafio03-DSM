package com.example.desafio03

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.desafio03.db.DatabaseHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class StatsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvStatsYear: TextView
    private lateinit var tvTotalPagesRead: TextView
    private lateinit var tvTopGenre: TextView

    private lateinit var tvCountNovela: TextView
    private lateinit var tvCountSciFi: TextView
    private lateinit var tvCountHistoria: TextView
    private lateinit var tvCountDesarrollo: TextView

    private lateinit var layoutBestBookInfo: LinearLayout
    private lateinit var tvBestBookTitle: TextView
    private lateinit var tvBestBookAuthor: TextView
    private lateinit var ratingBarBestBook: RatingBar
    private lateinit var tvBestBookRatingScore: TextView
    private lateinit var tvBestBookGenre: TextView
    private lateinit var tvBestBookFinishDate: TextView
    private lateinit var tvNoBestBook: TextView

    private var currentYear: Int = 0
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            finish()
            return
        }
        currentUserId = currentUser.uid
        currentYear = Calendar.getInstance().get(Calendar.YEAR)
        dbHelper = DatabaseHelper(this)

        initViews()
        cargarEstadisticas()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tvStatsYear = findViewById(R.id.tvStatsYear)
        tvTotalPagesRead = findViewById(R.id.tvTotalPagesRead)
        tvTopGenre = findViewById(R.id.tvTopGenre)

        tvCountNovela = findViewById(R.id.tvCountNovela)
        tvCountSciFi = findViewById(R.id.tvCountSciFi)
        tvCountHistoria = findViewById(R.id.tvCountHistoria)
        tvCountDesarrollo = findViewById(R.id.tvCountDesarrollo)

        layoutBestBookInfo = findViewById(R.id.layoutBestBookInfo)
        tvBestBookTitle = findViewById(R.id.tvBestBookTitle)
        tvBestBookAuthor = findViewById(R.id.tvBestBookAuthor)
        ratingBarBestBook = findViewById(R.id.ratingBarBestBook)
        tvBestBookRatingScore = findViewById(R.id.tvBestBookRatingScore)
        tvBestBookGenre = findViewById(R.id.tvBestBookGenre)
        tvBestBookFinishDate = findViewById(R.id.tvBestBookFinishDate)
        tvNoBestBook = findViewById(R.id.tvNoBestBook)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        tvStatsYear.text = "Año Actual: $currentYear"
    }

    private fun cargarEstadisticas() {
        val stats = dbHelper.getAnnualStats(currentUserId, currentYear)

        // 1. Resumen general
        tvTotalPagesRead.text = "${stats.totalPaginasLeidas} páginas"
        tvTopGenre.text = stats.generoMasLeido

        // 2. Cantidad de libros terminados por los 4 géneros obligatorios
        val novelaCount = stats.statsPorGenero["Novela"] ?: 0
        val scifiCount = stats.statsPorGenero["Ciencia ficción"] ?: 0
        val historiaCount = stats.statsPorGenero["Historia"] ?: 0
        val desarrolloCount = stats.statsPorGenero["Desarrollo personal"] ?: 0

        tvCountNovela.text = "$novelaCount ${if (novelaCount == 1) "libro" else "libros"}"
        tvCountSciFi.text = "$scifiCount ${if (scifiCount == 1) "libro" else "libros"}"
        tvCountHistoria.text = "$historiaCount ${if (historiaCount == 1) "libro" else "libros"}"
        tvCountDesarrollo.text = "$desarrolloCount ${if (desarrolloCount == 1) "libro" else "libros"}"

        // 3. Libro mejor calificado del año (en caso de empate, el que se terminó primero)
        val bestBook = stats.libroMejorCalificado
        if (bestBook != null) {
            layoutBestBookInfo.visibility = View.VISIBLE
            tvNoBestBook.visibility = View.GONE

            tvBestBookTitle.text = bestBook.titulo
            tvBestBookAuthor.text = "Por: ${bestBook.autor}"
            ratingBarBestBook.rating = bestBook.calificacion.toFloat()
            tvBestBookRatingScore.text = "${bestBook.calificacion}.0"
            tvBestBookGenre.text = bestBook.genero
            tvBestBookFinishDate.text = "Terminado el: ${bestBook.fechaTerminado}"
        } else {
            layoutBestBookInfo.visibility = View.GONE
            tvNoBestBook.visibility = View.VISIBLE
            tvNoBestBook.text = "No hay libros terminados calificados en el año $currentYear."
        }
    }
}
