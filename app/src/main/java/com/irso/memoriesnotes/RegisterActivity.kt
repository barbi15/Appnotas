package com.irso.memoriesnotes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var nombreEditText: EditText
    private lateinit var apellidoEditText: EditText
    private lateinit var correoEditText: EditText
    private lateinit var contrasenaEditText: EditText
    private lateinit var repContrasenaEditText: EditText
    private lateinit var telefonoEditText: EditText
    private lateinit var registrarButton: Button
    private lateinit var dbUsuarios: DbUsuarios

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbUsuarios = DbUsuarios(this)
        nombreEditText = findViewById(R.id.nombreEditText)
        apellidoEditText = findViewById(R.id.apellidoEditText)
        telefonoEditText = findViewById(R.id.telefonoEditText)
        correoEditText = findViewById(R.id.correoEditText)
        contrasenaEditText = findViewById(R.id.contrasenaEditText)
        repContrasenaEditText = findViewById(R.id.repContrasenaEditText)
        registrarButton = findViewById(R.id.registrarButton)

        // listener para el botón de registrar
        registrarButton.setOnClickListener {
            // Valido los datos de registro
            registrarUsuario()
        }
        // listener para el botón de "volver al inicio de sesión"
        val tvGoLogin = findViewById<TextView>(R.id.tv_go_to_login)
        tvGoLogin.setOnClickListener {
            goToLogin()
        }
    }

    private fun registrarUsuario() {
        val nombre = nombreEditText.text.toString().trim()
        val apellido = apellidoEditText.text.toString().trim()
        val telefono = telefonoEditText.text.toString().trim()
        val email = correoEditText.text.toString().trim()
        val password = contrasenaEditText.text.toString().trim()
        val repPassword = repContrasenaEditText.text.toString().trim()

        if (email.isEmpty() || !isValidEmail(email)) {
            correoEditText.error = "Ingrese un correo electrónico válido"
            return
        }

        if (password.length < 5) {
            contrasenaEditText.error = "La contraseña debe tener al menos 5 caracteres"
            return
        }

        if (password != repPassword) {
            repContrasenaEditText.error = "Las contraseñas no coinciden"
            return
        }
        if (telefono.length != 10) {
            telefonoEditText.error = "El número de teléfono debe tener 10 dígitos"
            return
        }

        // Verifico si el correo ya está registrado
        if (dbUsuarios.isEmailRegistered(email)) {
            Toast.makeText(this, "El correo electrónico ya está registrado", Toast.LENGTH_SHORT).show()
        } else {
            // Si pasa las validaciones, inserta
            val usuarioId = dbUsuarios.insertarUsuario(nombre, apellido, telefono, email, password)
            Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
            saveUserIdToPreferences(usuarioId)
            goToInicioNotes()
        }
    }

    // Valido si el formato del correo
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun goToLogin() {
        val i = Intent(this, MainActivity::class.java)
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
        val email = correoEditText.text.toString().trim()
        val usuarioId = dbUsuarios.getUserIdByEmail(email)

        val intent = Intent(this, InicioNotesActivity::class.java).apply {
            putExtra("usuario_id", usuarioId)
        }
        startActivity(intent)
    }
}
