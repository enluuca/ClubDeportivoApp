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
import com.example.clubdeportivoapp.MenuPrincipal // Se recomienda importar la clase completa para claridad

/**
 * Actividad Principal del ClubDeportivoApp. (Pantalla de Login)
 * * Responsable de:
 * 1. Inicializar la interfaz de usuario (activity_main.xml).
 * 2. Manejar la entrada de credenciales.
 * 3. Utilizar DatabaseHelper.java para la verificación de la base de datos local.
 * 4. Navegar a MenuPrincipal en caso de éxito.
 */
class MainActivity : AppCompatActivity() {

    // Instancia de la clase Java que gestiona la base de datos SQLite.
    // Usamos 'lateinit' porque la inicialización ocurre en onCreate().
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización de la capa de acceso a datos.
        dbHelper = DatabaseHelper(this)

        // 1. Referencias a los elementos de la UI definidos en activity_main.xml.
        // Se asume que estos IDs están correctamente definidos en el XML.
        val etUsername = findViewById<TextInputEditText>(R.id.et_main_username)
        val etPassword = findViewById<TextInputEditText>(R.id.et_main_password)
        val btnSend = findViewById<Button>(R.id.btn_main_send)

        // 2. Configuración del Listener para el botón de Ingresar.
        btnSend.setOnClickListener {
            // Recolectar y limpiar (trim) las credenciales antes de la verificación.
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            handleLogin(username, password)
        }
    }

    /**
     * Procesa la solicitud de autenticación de usuario.
     * * @param username El nombre de usuario ingresado.
     * @param password La contraseña ingresada.
     */
    private fun handleLogin(username: String, password: String) {
        // Validación básica de campos vacíos antes de la consulta a la DB.
        if (username.isEmpty() || password.isEmpty()) {
            showToast("Por favor, ingrese usuario y contraseña.")
            return
        }

        // Se llama al método de verificación de la base de datos.
        val isUserValid = dbHelper.checkUser(username, password)

        if (isUserValid) {
            // --- Caso de Éxito ---
            Log.d("MainActivity", "Login exitoso para usuario: $username")
            showToast("¡Acceso concedido! Bienvenido/a al sistema.")

            // Navegación al menú principal.
            val intent = Intent(this, MenuPrincipal::class.java).apply {
                // Se pasa el nombre de usuario para personalizar el saludo en el menú.
                putExtra("USER_NAME", username)
            }
            startActivity(intent)

            // Finalizar esta actividad para que el usuario no pueda volver al Login con el botón "Atrás".
            finish()

        } else {
            // --- Caso de Falla ---
            Log.w("MainActivity", "Intento de login fallido para usuario: $username")
            showToast("Credenciales incorrectas. Verifique sus datos.")
        }
    }

    /**
     * Extensión de función para simplificar la visualización de mensajes Toast.
     * Utiliza 'this' como contexto.
     */
    private fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}