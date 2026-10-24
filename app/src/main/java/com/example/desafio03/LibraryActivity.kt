package com.example.desafio03

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.desafio03.adapter.BookAdapter
import com.example.desafio03.db.DatabaseHelper
import com.example.desafio03.model.Book
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class LibraryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var bookAdapter: BookAdapter

    private lateinit var toolbar: MaterialToolbar
    private lateinit var chipGroupFilter: ChipGroup
    private lateinit var rvLibrary: RecyclerView
    private lateinit var layoutEmptyLibrary: LinearLayout
    private lateinit var tvEmptyLibraryMsg: TextView
    private lateinit var btnEmptyAddBook: MaterialButton
    private lateinit var fabAddBook: FloatingActionButton

    private var allBooks: List<Book> = emptyList()
    private var currentFilter: String = "Todos"
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            finish()
            return
        }
        currentUserId = currentUser.uid
        dbHelper = DatabaseHelper(this)

        initViews()
        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        cargarLibros()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        chipGroupFilter = findViewById(R.id.chipGroupFilter)
        rvLibrary = findViewById(R.id.rvLibrary)
        layoutEmptyLibrary = findViewById(R.id.layoutEmptyLibrary)
        tvEmptyLibraryMsg = findViewById(R.id.tvEmptyLibraryMsg)
        btnEmptyAddBook = findViewById(R.id.btnEmptyAddBook)
        fabAddBook = findViewById(R.id.fabAddBook)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(this, emptyList()) { book ->
            val intent = Intent(this, BookFormActivity::class.java).apply {
                putExtra(BookFormActivity.EXTRA_BOOK_ID, book.id)
            }
            startActivity(intent)
        }
        rvLibrary.layoutManager = LinearLayoutManager(this)
        rvLibrary.adapter = bookAdapter
    }

    private fun setupListeners() {
        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedId = checkedIds.first()
                currentFilter = when (checkedId) {
                    R.id.chipToRead -> "Por leer"
                    R.id.chipReading -> "Leyendo"
                    R.id.chipCompleted -> "Terminado"
                    else -> "Todos"
                }
                aplicarFiltro()
            }
        }

        val goToAddBook = View.OnClickListener {
            startActivity(Intent(this, BookFormActivity::class.java))
        }

        fabAddBook.setOnClickListener(goToAddBook)
        btnEmptyAddBook.setOnClickListener(goToAddBook)
    }

    private fun cargarLibros() {
        allBooks = dbHelper.getAllBooksByUser(currentUserId)
        aplicarFiltro()
    }

    private fun aplicarFiltro() {
        val filteredList = if (currentFilter == "Todos") {
            allBooks
        } else {
            allBooks.filter { it.estado.equals(currentFilter, ignoreCase = true) }
        }

        if (filteredList.isEmpty()) {
            rvLibrary.visibility = View.GONE
            layoutEmptyLibrary.visibility = View.VISIBLE

            if (allBooks.isEmpty()) {
                tvEmptyLibraryMsg.text = getString(R.string.empty_library_msg)
                btnEmptyAddBook.visibility = View.VISIBLE
            } else {
                tvEmptyLibraryMsg.text = "No tienes ningún libro en estado '$currentFilter'."
                btnEmptyAddBook.visibility = View.GONE
            }
        } else {
            rvLibrary.visibility = View.VISIBLE
            layoutEmptyLibrary.visibility = View.GONE
            bookAdapter.updateBooks(filteredList)
        }
    }
}
