package com.example.clubdeportivoapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.example.clubdeportivoapp.data.db.DatabaseHelper

/**
 * Actividad Principal del ClubDeportivoApp.
 * Funciona como la pantalla de Login, utilizando el layout activity_main.xml
 * y la lógica de DB implementada en DatabaseHelper.java.
 */
class MainActivity : AppCompatActivity() {

    // Instancia de la clase Java que gestiona la base de datos SQLite
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inicializar el DatabaseHelper (clase Java)
        dbHelper = DatabaseHelper(this)

        // 2. Referencias a los elementos de la UI definidos en activity_main.xml
        val etUsername = findViewById<TextInputEditText>(R.id.et_main_username)
        val etPassword = findViewById<TextInputEditText>(R.id.et_main_password)
        val btnSend = findViewById<Button>(R.id.btn_main_send)

        // 3. Listener para el botón de Ingresar
        btnSend.setOnClickListener {
            handleLogin(etUsername.text.toString().trim(), etPassword.text.toString().trim())
        }
    }

    /**
     * Maneja la lógica de autenticación.
     * @param username El nombre de usuario ingresado.
     * @param password La contraseña ingresada.
     */
    private fun handleLogin(username: String, password: String) {
        if (username.isEmpty() || password.isEmpty()) {
            showToast("Por favor, ingrese usuario y contraseña.")
            return
        }

        // Llamada al método de verificación implementado en la clase Java (DatabaseHelper)
        val isUserValid = dbHelper.checkUser(username, password)

        if (isUserValid) {
            // Autenticación exitosa
            Log.d("MainActivity", "Login exitoso para usuario: $username")
            showToast("¡Acceso concedido! Bienvenido/a al sistema.")

            // Implementar la navegación al MenuPrincipalActivity
            val intent = Intent(this, MenuPrincipal::class.java).apply {
                // Se pasa el nombre de usuario al menú principal para el saludo
                putExtra("USER_NAME", username)
            }
            startActivity(intent)
            finish() // Cierra MainActivity para evitar volver atrás con el botón 'back'

        } else {
            // Autenticación fallida
            Log.w("MainActivity", "Intento de login fallido para usuario: $username")
            showToast("Credenciales incorrectas. Verifique sus datos.")
        }
    }

    /**
     * Función utilitaria para mostrar mensajes Toast.
     */
    private fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}