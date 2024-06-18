package com.irso.memoriesnotes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class InicioNotesActivity : AppCompatActivity() {

    private lateinit var microphoneButton: ImageButton
    private lateinit var searchButton: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var notasDBHelper: DbNotas
    private lateinit var dbUsuarios: DbUsuarios
    private lateinit var logoutButton: Button
    private lateinit var notesListView: ListView
    private lateinit var adapter: NotaAdaptador
    private lateinit var welcomeTextView: TextView
    private val notasList: MutableList<Nota> = mutableListOf()
    private var usuarioId: Int = 0

    private val startVoiceInputLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
                buscarNota(spokenText)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicionotes)

        notesListView = findViewById(R.id.notesListView)
        notasDBHelper = DbNotas(this)
        dbUsuarios = DbUsuarios(this)
        welcomeTextView = findViewById(R.id.welcomeTextView)

        // Obtener el ID del usuario desde las preferencias compartidas
        usuarioId = getUserIdFromPreferences()

        // Verificar si el usuario es válido
        if (usuarioId == -1) {
            goToMainActivity()
            return
        }

        // Obtener el nombre del usuario y mostrar el mensaje de bienvenida
        val nombreUsuario = dbUsuarios.obtenerNombreUsuario(usuarioId)
        welcomeTextView.text = "¡Bienvenido $nombreUsuario a tu lista de notas!"

        // Cargo las notas desde la base de datos
        cargarNotas()

        // Eventos
        val agregarnotaButton = findViewById<FloatingActionButton>(R.id.agregarnotaButton)
        agregarnotaButton.setOnClickListener {
            GoNuevaNota()
        }
        microphoneButton = findViewById(R.id.microphoneButton)
        microphoneButton.setOnClickListener {
            startVoiceInput()
        }
        searchButton = findViewById(R.id.searchButton)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            buscarNota(query)
        }
        logoutButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            cerrarSesion()
        }

        notesListView.setOnItemClickListener { _, _, position, _ ->
            val nota = notasList[position]
            abrirNota(nota.id)
        }
    }

    private fun getUserIdFromPreferences(): Int {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("usuario_id", -1)
    }

    private fun cargarNotas() {
        val notas = notasDBHelper.obtenerNotas(usuarioId)
        adapter = NotaAdaptador(this, notas)
        notesListView.adapter = adapter
        notasList.clear()
        notasList.addAll(notas)
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...")
        startVoiceInputLauncher.launch(intent)
    }

    private fun buscarNota(query: String) {
        adapter.setQuery(query)
    }

    private fun cerrarSesion() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        goToMainActivity()
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun GoNuevaNota() {
        val intent = Intent(this, NuevanotaActivity::class.java)
        intent.putExtra("usuario_id", usuarioId)
        startActivity(intent)
    }

    private fun abrirNota(id: Long) {
        val intent = Intent(this, NuevanotaActivity::class.java)
        intent.putExtra("nota_id", id)
        intent.putExtra("usuario_id", usuarioId)
        startActivity(intent)
    }
}
