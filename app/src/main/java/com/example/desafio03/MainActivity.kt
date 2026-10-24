package com.example.desafio03

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.desafio03.adapter.BookAdapter
import com.example.desafio03.db.DatabaseHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var bookAdapter: BookAdapter

    // Views
    private lateinit var tvGreeting: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var tvGoalTitle: TextView
    private lateinit var btnEditGoal: MaterialButton
    private lateinit var tvGoalTarget: TextView
    private lateinit var tvGoalCompleted: TextView
    private lateinit var tvGoalRemaining: TextView
    private lateinit var progressGoal: LinearProgressIndicator
    private lateinit var tvGoalStatusText: TextView

    private lateinit var btnNavAddBook: MaterialCardView
    private lateinit var btnNavLibrary: MaterialCardView
    private lateinit var btnNavStats: MaterialCardView

    private lateinit var tvCountReading: TextView
    private lateinit var cardEmptyReading: MaterialCardView
    private lateinit var rvCurrentlyReading: RecyclerView

    private var currentYear: Int = 0
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            goToLogin()
            return
        }

        currentUserId = currentUser.uid
        currentYear = Calendar.getInstance().get(Calendar.YEAR)
        dbHelper = DatabaseHelper(this)

        initViews()
        setupListeners()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser == null) {
            goToLogin()
            return
        }
        cargarDatosMeta()
        cargarLibrosLeyendo()
    }

    private fun initViews() {
        tvGreeting = findViewById(R.id.tvGreeting)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        btnLogout = findViewById(R.id.btnLogout)
        tvGoalTitle = findViewById(R.id.tvGoalTitle)
        btnEditGoal = findViewById(R.id.btnEditGoal)
        tvGoalTarget = findViewById(R.id.tvGoalTarget)
        tvGoalCompleted = findViewById(R.id.tvGoalCompleted)
        tvGoalRemaining = findViewById(R.id.tvGoalRemaining)
        progressGoal = findViewById(R.id.progressGoal)
        tvGoalStatusText = findViewById(R.id.tvGoalStatusText)

        btnNavAddBook = findViewById(R.id.btnNavAddBook)
        btnNavLibrary = findViewById(R.id.btnNavLibrary)
        btnNavStats = findViewById(R.id.btnNavStats)

        tvCountReading = findViewById(R.id.tvCountReading)
        cardEmptyReading = findViewById(R.id.cardEmptyReading)
        rvCurrentlyReading = findViewById(R.id.rvCurrentlyReading)

        val email = auth.currentUser?.email ?: "Usuario"
        val username = email.substringBefore('@')
        tvGreeting.text = "¡Hola, $username!"
        tvUserEmail.text = email
        tvGoalTitle.text = "Meta Anual de Lectura ($currentYear)"
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            confirmarCierreSesion()
        }

        btnEditGoal.setOnClickListener {
            mostrarDialogoMeta()
        }

        btnNavAddBook.setOnClickListener {
            startActivity(Intent(this, BookFormActivity::class.java))
        }

        btnNavLibrary.setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }

        btnNavStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(this, emptyList()) { book ->
            val intent = Intent(this, BookFormActivity::class.java).apply {
                putExtra(BookFormActivity.EXTRA_BOOK_ID, book.id)
            }
            startActivity(intent)
        }
        rvCurrentlyReading.layoutManager = LinearLayoutManager(this)
        rvCurrentlyReading.adapter = bookAdapter
    }

    private fun cargarDatosMeta() {
        val meta = dbHelper.getAnnualGoal(currentUserId, currentYear)
        val terminados = dbHelper.getFinishedBooksCountInYear(currentUserId, currentYear)

        tvGoalCompleted.text = terminados.toString()

        if (meta > 0) {
            tvGoalTarget.text = meta.toString()
            val faltan = maxOf(0, meta - terminados)
            tvGoalRemaining.text = faltan.toString()

            val pct = ((terminados.toDouble() / meta.toDouble()) * 100).toInt().coerceIn(0, 100)
            progressGoal.progress = pct

            if (terminados >= meta) {
                tvGoalStatusText.text = getString(R.string.goal_achieved)
            } else {
                tvGoalStatusText.text = "Progreso: $pct% ($terminados de $meta libros)"
            }
        } else {
            tvGoalTarget.text = "-"
            tvGoalRemaining.text = "-"
            progressGoal.progress = 0
            tvGoalStatusText.text = "Aún no has definido tu meta para $currentYear. ¡Presiona Modificar!"
        }
    }

    private fun cargarLibrosLeyendo() {
        val readingBooks = dbHelper.getCurrentlyReadingBooks(currentUserId)
        val count = readingBooks.size
        tvCountReading.text = "$count ${if (count == 1) "libro" else "libros"}"

        if (readingBooks.isEmpty()) {
            cardEmptyReading.visibility = View.VISIBLE
            rvCurrentlyReading.visibility = View.GONE
        } else {
            cardEmptyReading.visibility = View.GONE
            rvCurrentlyReading.visibility = View.VISIBLE
            bookAdapter.updateBooks(readingBooks)
        }
    }

    private fun mostrarDialogoMeta() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_goal, null)
        val tilGoal = dialogView.findViewById<TextInputLayout>(R.id.tilDialogGoal)
        val etGoal = dialogView.findViewById<TextInputEditText>(R.id.etDialogGoal)

        val metaActual = dbHelper.getAnnualGoal(currentUserId, currentYear)
        if (metaActual > 0) {
            etGoal.setText(metaActual.toString())
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Guardar", null) // asignamos después para validar sin cerrar si hay error
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val btnGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnGuardar.setOnClickListener {
                val inputStr = etGoal.text.toString().trim()
                val metaInt = inputStr.toIntOrNull()

                if (metaInt == null || metaInt <= 0) {
                    tilGoal.error = getString(R.string.err_goal_invalid)
                } else {
                    tilGoal.error = null
                    dbHelper.setAnnualGoal(currentUserId, currentYear, metaInt)
                    Toast.makeText(this, "Meta anual actualizada a $metaInt libros", Toast.LENGTH_SHORT).show()
                    cargarDatosMeta()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun confirmarCierreSesion() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout)
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Cerrar Sesión") { _, _ ->
                auth.signOut()
                goToLogin()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
