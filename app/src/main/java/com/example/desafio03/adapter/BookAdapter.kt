package com.example.desafio03.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.desafio03.R
import com.example.desafio03.model.Book
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator

class BookAdapter(
    private val context: Context,
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    fun updateBooks(newBooks: List<Book>) {
        this.books = newBooks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)
    }

    override fun getItemCount(): Int = books.size

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardBookItem: MaterialCardView = itemView.findViewById(R.id.cardBookItem)
        private val tvBookTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        private val tvBookAuthor: TextView = itemView.findViewById(R.id.tvBookAuthor)
        private val chipStatus: TextView = itemView.findViewById(R.id.chipStatus)
        private val tvBookGenre: TextView = itemView.findViewById(R.id.tvBookGenre)
        private val tvPagesCount: TextView = itemView.findViewById(R.id.tvPagesCount)
        private val tvProgressPercent: TextView = itemView.findViewById(R.id.tvProgressPercent)
        private val progressBook: LinearProgressIndicator = itemView.findViewById(R.id.progressBook)
        private val layoutRatingInfo: LinearLayout = itemView.findViewById(R.id.layoutRatingInfo)
        private val ratingBarItem: RatingBar = itemView.findViewById(R.id.ratingBarItem)
        private val tvFinishDate: TextView = itemView.findViewById(R.id.tvFinishDate)

        fun bind(book: Book) {
            tvBookTitle.text = book.titulo
            tvBookAuthor.text = "Por: ${book.autor}"
            tvBookGenre.text = book.genero
            chipStatus.text = book.estado

            // Color coding status
            when (book.estado) {
                "Terminado" -> {
                    chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_terminado))
                    chipStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_terminado_bg))
                }
                "Leyendo" -> {
                    chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_leyendo))
                    chipStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_leyendo_bg))
                }
                else -> { // "Por leer"
                    chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_por_leer))
                    chipStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_por_leer_bg))
                }
            }

            // Genre styling
            when (book.genero) {
                "Novela" -> {
                    tvBookGenre.setTextColor(ContextCompat.getColor(context, R.color.genre_novela))
                    tvBookGenre.setBackgroundColor(ContextCompat.getColor(context, R.color.genre_novela_bg))
                }
                "Ciencia ficción" -> {
                    tvBookGenre.setTextColor(ContextCompat.getColor(context, R.color.genre_scifi))
                    tvBookGenre.setBackgroundColor(ContextCompat.getColor(context, R.color.genre_scifi_bg))
                }
                "Historia" -> {
                    tvBookGenre.setTextColor(ContextCompat.getColor(context, R.color.genre_historia))
                    tvBookGenre.setBackgroundColor(ContextCompat.getColor(context, R.color.genre_historia_bg))
                }
                "Desarrollo personal" -> {
                    tvBookGenre.setTextColor(ContextCompat.getColor(context, R.color.genre_desarrollo))
                    tvBookGenre.setBackgroundColor(ContextCompat.getColor(context, R.color.genre_desarrollo_bg))
                }
            }

            // Pages & progress
            tvPagesCount.text = "${book.paginasLeidas} / ${book.totalPaginas} págs"
            val pct = book.porcentajeAvance
            tvProgressPercent.text = "($pct%)"
            progressBook.progress = pct

            // Rating & Finish Date (only if Terminado)
            if (book.estado == "Terminado" && book.calificacion > 0) {
                layoutRatingInfo.visibility = View.VISIBLE
                ratingBarItem.rating = book.calificacion.toFloat()
                if (book.fechaTerminado.isNotBlank()) {
                    tvFinishDate.text = "Terminado el: ${book.fechaTerminado}"
                    tvFinishDate.visibility = View.VISIBLE
                } else {
                    tvFinishDate.visibility = View.GONE
                }
            } else {
                layoutRatingInfo.visibility = View.GONE
            }

            cardBookItem.setOnClickListener {
                onBookClick(book)
            }
        }
    }
}
