package com.example.desafio03

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.desafio03.db.DatabaseHelper
import com.example.desafio03.model.Book
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BookFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BOOK_ID = "extra_book_id"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tilTitle: TextInputLayout
    private lateinit var etTitle: TextInputEditText
    private lateinit var tilAuthor: TextInputLayout
    private lateinit var etAuthor: TextInputEditText
    private lateinit var spinnerGenre: Spinner
    private lateinit var tilTotalPages: TextInputLayout
    private lateinit var etTotalPages: TextInputEditText
    private lateinit var tilReadPages: TextInputLayout
    private lateinit var etReadPages: TextInputEditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var cardRatingContainer: MaterialCardView
    private lateinit var ratingBarForm: RatingBar
    private lateinit var tvRatingHelper: TextView
    private lateinit var btnSaveBook: MaterialButton
    private lateinit var btnDeleteBook: MaterialButton

    private var editingBookId: Long = -1L
    private var currentBook: Book? = null
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_form)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            finish()
            return
        }
        currentUserId = currentUser.uid
        dbHelper = DatabaseHelper(this)

        initViews()
        setupSpinners()
        checkIntentAndPopulate()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tilTitle = findViewById(R.id.tilTitle)
        etTitle = findViewById(R.id.etTitle)
        tilAuthor = findViewById(R.id.tilAuthor)
        etAuthor = findViewById(R.id.etAuthor)
        spinnerGenre = findViewById(R.id.spinnerGenre)
        tilTotalPages = findViewById(R.id.tilTotalPages)
        etTotalPages = findViewById(R.id.etTotalPages)
        tilReadPages = findViewById(R.id.tilReadPages)
        etReadPages = findViewById(R.id.etReadPages)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        cardRatingContainer = findViewById(R.id.cardRatingContainer)
        ratingBarForm = findViewById(R.id.ratingBarForm)
        tvRatingHelper = findViewById(R.id.tvRatingHelper)
        btnSaveBook = findViewById(R.id.btnSaveBook)
        btnDeleteBook = findViewById(R.id.btnDeleteBook)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSpinners() {
        val genreAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Book.GENEROS_OBLIGATORIOS
        )
        spinnerGenre.adapter = genreAdapter

        val statusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Book.ESTADOS_SUGERIDOS
        )
        spinnerStatus.adapter = statusAdapter

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = Book.ESTADOS_SUGERIDOS[position]
                actualizarEstadoCalificacion(selectedStatus)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun actualizarEstadoCalificacion(status: String) {
        if (status == "Terminado") {
            // Habilitar calificación obligatoria
            cardRatingContainer.alpha = 1.0f
            ratingBarForm.setIsIndicator(false)
            ratingBarForm.isEnabled = true
            tvRatingHelper.text = "Calificación obligatoria (1 a 5 estrellas)"

            // Si las páginas leídas están vacías o son menores al total, sugerir igualarlas al total
            val totalStr = etTotalPages.text.toString().trim()
            val totalInt = totalStr.toIntOrNull()
            if (totalInt != null && totalInt > 0) {
                val readInt = etReadPages.text.toString().trim().toIntOrNull() ?: 0
                if (readInt < totalInt) {
                    etReadPages.setText(totalInt.toString())
                }
            }
        } else {
            // Deshabilitar calificación
            cardRatingContainer.alpha = 0.5f
            ratingBarForm.rating = 0f
            ratingBarForm.setIsIndicator(true)
            ratingBarForm.isEnabled = false
            tvRatingHelper.text = getString(R.string.rating_disabled_info)
        }
    }

    private fun checkIntentAndPopulate() {
        editingBookId = intent.getLongExtra(EXTRA_BOOK_ID, -1L)

        if (editingBookId != -1L) {
            // Modo Edición
            toolbar.title = getString(R.string.edit_book_title)
            btnSaveBook.text = "Actualizar Libro"
            btnDeleteBook.visibility = View.VISIBLE

            currentBook = dbHelper.getBookById(editingBookId, currentUserId)
            currentBook?.let { book ->
                etTitle.setText(book.titulo)
                etAuthor.setText(book.autor)

                val genreIndex = Book.GENEROS_OBLIGATORIOS.indexOf(book.genero)
                if (genreIndex >= 0) spinnerGenre.setSelection(genreIndex)

                etTotalPages.setText(book.totalPaginas.toString())
                etReadPages.setText(book.paginasLeidas.toString())

                val statusIndex = Book.ESTADOS_SUGERIDOS.indexOf(book.estado)
                if (statusIndex >= 0) spinnerStatus.setSelection(statusIndex)

                if (book.estado == "Terminado") {
                    ratingBarForm.rating = book.calificacion.toFloat()
                }
            }
        } else {
            // Modo Registro
            toolbar.title = getString(R.string.add_book_title)
            btnSaveBook.text = getString(R.string.btn_save_book)
            btnDeleteBook.visibility = View.GONE
            // Estado inicial por defecto "Por leer"
            actualizarEstadoCalificacion("Por leer")
        }
    }

    private fun setupListeners() {
        btnSaveBook.setOnClickListener {
            guardarLibro()
        }

        btnDeleteBook.setOnClickListener {
            confirmarEliminacion()
        }
    }

    private fun guardarLibro() {
        tilTitle.error = null
        tilAuthor.error = null
        tilTotalPages.error = null
        tilReadPages.error = null

        val titulo = etTitle.text.toString().trim()
        val autor = etAuthor.text.toString().trim()
        val genero = spinnerGenre.selectedItem.toString()
        val totalPagesStr = etTotalPages.text.toString().trim()
        val readPagesStr = etReadPages.text.toString().trim()
        val estado = spinnerStatus.selectedItem.toString()
        val calificacion = ratingBarForm.rating.toInt()

        // 1. Validar Título (2 - 60 caracteres)
        if (titulo.length < 2 || titulo.length > 60) {
            tilTitle.error = getString(R.string.err_title_invalid)
            Toast.makeText(this, "Título inválido: debe tener entre 2 y 60 caracteres", Toast.LENGTH_SHORT).show()
            etTitle.requestFocus()
            return
        }

        // 2. Validar Autor (2 - 60 caracteres)
        if (autor.length < 2 || autor.length > 60) {
            tilAuthor.error = getString(R.string.err_author_invalid)
            Toast.makeText(this, "Autor inválido: debe tener entre 2 y 60 caracteres", Toast.LENGTH_SHORT).show()
            etAuthor.requestFocus()
            return
        }

        // 3. Validar Total Páginas (entero > 0)
        val totalPaginas = totalPagesStr.toIntOrNull()
        if (totalPaginas == null || totalPaginas <= 0) {
            tilTotalPages.error = getString(R.string.err_total_pages_invalid)
            Toast.makeText(this, "Total de páginas inválido: debe ser un número entero mayor que cero", Toast.LENGTH_SHORT).show()
            etTotalPages.requestFocus()
            return
        }

        // 4. Validar Páginas Leídas (entero entre 0 y totalPaginas)
        val paginasLeidas = readPagesStr.toIntOrNull()
        if (paginasLeidas == null || paginasLeidas < 0 || paginasLeidas > totalPaginas) {
            tilReadPages.error = "Las páginas leídas deben ser entre 0 y $totalPaginas"
            Toast.makeText(this, "Páginas leídas inválidas: debe ser entre 0 y $totalPaginas", Toast.LENGTH_SHORT).show()
            etReadPages.requestFocus()
            return
        }

        // 5. Validar Calificación si Terminado (1 a 5 estrellas)
        if (estado == "Terminado" && (calificacion < 1 || calificacion > 5)) {
            Toast.makeText(this, getString(R.string.err_rating_required), Toast.LENGTH_LONG).show()
            return
        }

        // Preparar fecha de terminado si corresponde
        var fechaTerminado = ""
        var timestampTerminado = 0L
        var anioTerminado = 0

        if (estado == "Terminado") {
            if (currentBook != null && currentBook?.estado == "Terminado" && currentBook?.fechaTerminado?.isNotBlank() == true) {
                // Conservar fecha previa si ya estaba terminado
                fechaTerminado = currentBook!!.fechaTerminado
                timestampTerminado = currentBook!!.fechaTerminadoTimestamp
                anioTerminado = currentBook!!.anioTerminado
            } else {
                // Registrar nueva fecha de finalización
                val now = Date()
                fechaTerminado = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(now)
                timestampTerminado = System.currentTimeMillis()
                anioTerminado = Calendar.getInstance().get(Calendar.YEAR)
            }
        }

        val bookToSave = Book(
            id = if (editingBookId != -1L) editingBookId else 0,
            userId = currentUserId,
            titulo = titulo,
            autor = autor,
            genero = genero,
            totalPaginas = totalPaginas,
            paginasLeidas = paginasLeidas,
            estado = estado,
            calificacion = if (estado == "Terminado") calificacion else 0,
            fechaTerminado = fechaTerminado,
            fechaTerminadoTimestamp = timestampTerminado,
            anioTerminado = anioTerminado
        )

        if (editingBookId != -1L) {
            val updatedRows = dbHelper.updateBook(bookToSave)
            if (updatedRows > 0) {
                Toast.makeText(this, "Libro actualizado exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al actualizar libro", Toast.LENGTH_SHORT).show()
            }
        } else {
            val insertedId = dbHelper.insertBook(bookToSave)
            if (insertedId != -1L) {
                Toast.makeText(this, "Libro registrado exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al registrar libro", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarEliminacion() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar Libro")
            .setMessage("¿Estás seguro de que deseas eliminar este libro de tu biblioteca?")
            .setPositiveButton("Eliminar") { _, _ ->
                val deleted = dbHelper.deleteBook(editingBookId, currentUserId)
                if (deleted > 0) {
                    Toast.makeText(this, "Libro eliminado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "No se pudo eliminar el libro", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
