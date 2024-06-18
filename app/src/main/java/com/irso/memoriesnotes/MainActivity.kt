package com.irso.memoriesnotes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var correoEditText: EditText
    private lateinit var contrasenaEditText: EditText
    private lateinit var iniciarSesionButton: Button
    private lateinit var dbUsuarios: DbUsuarios
    private var intentosFallidos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Inicializo vistas
        correoEditText = findViewById(R.id.correoEditText)
        contrasenaEditText = findViewById(R.id.contrasenaEditText)
        iniciarSesionButton = findViewById(R.id.iniciarSesionButton)

        dbUsuarios = DbUsuarios(this)

        // Valido credenciales
        iniciarSesionButton.setOnClickListener {
            val email = correoEditText.text.toString().trim()
            val password = contrasenaEditText.text.toString().trim()
            val usuarioId = validateLogin(email, password)

            if (usuarioId != -1) {
                saveUserIdToPreferences(usuarioId)  // Guardar el ID del usuario en las preferencias compartidas
                goToInicioNotes()
            } else {
                intentosFallidos++
                if (intentosFallidos >= 3) {
                    Toast.makeText(this, "Has excedido el límite de intentos fallidos. Cerrando la aplicación.", Toast.LENGTH_SHORT).show()
                    finishAffinity()
                    // Limpio los campos
                    correoEditText.setText("")
                    contrasenaEditText.setText("")
                } else {
                    Toast.makeText(this, "Correo o contraseña incorrectos. Intento $intentosFallidos/3", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Pantalla de registro
        val tvGoRegister = findViewById<TextView>(R.id.tv_go_to_register)
        tvGoRegister.setOnClickListener {
            goToRegister()
        }
    }

    private fun validateLogin(email: String, password: String): Int {
        // Valido correo electrónico
        if (!email.isValidEmail()) {
            correoEditText.error = "Ingresa un correo electrónico válido"
            return -1
        }
        // Valido contraseña
        if (password.length < 5) {
            contrasenaEditText.error = "Debe tener al menos 5 caracteres"
            return -1
        }
        // Consulto la base de datos si el usuario está registrado
        return dbUsuarios.validarUsuario(email, password)
    }

    // Valido el correo con los caracteres que se usan
    private fun String.isValidEmail(): Boolean {
        val regex = Regex("[^@ ]+@[^@ ]+\\.[^@ ]+")
        return regex.matches(this)
    }

    private fun goToRegister() {
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun saveUserIdToPreferences(usuarioId: Int) {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("usuario_id", usuarioId)
            apply()
        }
    }

    private fun goToInicioNotes() {
        val intent = Intent(this, InicioNotesActivity::class.java)
        startActivity(intent)
        finish()
    }
}
