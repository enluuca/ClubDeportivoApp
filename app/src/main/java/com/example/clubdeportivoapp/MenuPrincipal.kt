package com.example.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button // ✅ Importación de Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// Eliminada la importación de CardView si ya no se usa, o si se usa, no se aplica a los botones
import android.widget.TextView
import com.example.clubdeportivoapp.ui.clientes.ClientesActivity
import com.example.clubdeportivoapp.ui.actividades.ActividadesActivity
import com.example.clubdeportivoapp.ui.pagos.PagosActivity
import com.example.clubdeportivoapp.ui.reportes.ListadosActivity

/**
 * Actividad que sirve como Menu Principal
 * después de la autenticación exitosa.
 */
class MenuPrincipal : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)

        val welcomeText = findViewById<TextView>(R.id.tv_welcome_message)
        val userName = intent.getStringExtra("USER_NAME") ?: "Personal"
        welcomeText.text = "Hola, $userName. ¡Bienvenido/a al CludDeportivoApp!"

        // ❌ CAMBIO CLAVE: Reemplazar CardView por Button
        val cardClientes = findViewById<Button>(R.id.card_clientes)
        val cardPagos = findViewById<Button>(R.id.card_pagos)
        val cardActividades = findViewById<Button>(R.id.card_actividades)
        val cardListados = findViewById<Button>(R.id.card_listados)

        // Asignar Listeners
        cardClientes.setOnClickListener {
            val intent = Intent(this, ClientesActivity::class.java)
            startActivity(intent)
        }

        cardActividades.setOnClickListener {
            val intent = Intent(this, ActividadesActivity::class.java)
            startActivity(intent)
        }

        cardPagos.setOnClickListener {
            val intent = Intent(this, PagosActivity::class.java)
            startActivity(intent)
        }

        cardListados.setOnClickListener {
            val intent = Intent(this, ListadosActivity::class.java)
            startActivity(intent)
        }
    }
}
