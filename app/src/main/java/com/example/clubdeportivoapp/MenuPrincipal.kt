package com.example.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Importación necesaria para las referencias de los botones
import android.widget.Toast // Aunque no se usa directamente en este código, se mantiene por si acaso
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.example.clubdeportivoapp.ui.clientes.ClientesActivity
import com.example.clubdeportivoapp.ui.actividades.ActividadesActivity
import com.example.clubdeportivoapp.ui.pagos.PagosActivity
import com.example.clubdeportivoapp.ui.reportes.ListadosActivity

/**
 * Actividad principal que sirve como menú de navegación.
 * * Responsable de:
 * 1. Mostrar el saludo personalizado con el nombre de usuario del Login.
 * 2. Manejar la navegación hacia los cuatro módulos principales del sistema.
 */
class MenuPrincipal : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Se establece el layout activity_menu_principal (el diseño vertical con fondo)
        setContentView(R.layout.activity_menu_principal)

        val welcomeText = findViewById<TextView>(R.id.tv_welcome_message)

        // 1. Recuperar el nombre de usuario pasado desde MainActivity. Por defecto es "Personal".
        val userName = intent.getStringExtra("USER_NAME") ?: "Personal"
        welcomeText.text = "Hola, $userName. ¡Bienvenido/a al CludDeportivoApp!"

        // 2. Referencias a los Botones (Corregido: Ahora son tipo Button, no CardView)
        val cardClientes = findViewById<Button>(R.id.card_clientes)
        val cardPagos = findViewById<Button>(R.id.card_pagos)
        val cardActividades = findViewById<Button>(R.id.card_actividades)
        val cardListados = findViewById<Button>(R.id.card_listados)

        // 3. Asignación de Listeners para la Navegación

        // Módulo Clientes: Abre la actividad de listado y gestión de clientes.
        cardClientes.setOnClickListener {
            val intent = Intent(this, ClientesActivity::class.java)
            startActivity(intent)
        }

        // Módulo Actividades: Abre la actividad de listado y gestión de actividades.
        cardActividades.setOnClickListener {
            val intent = Intent(this, ActividadesActivity::class.java)
            startActivity(intent)
        }

        // Módulo Pagos: Abre la actividad de gestión y registro de cuotas/pagos.
        cardPagos.setOnClickListener {
            val intent = Intent(this, PagosActivity::class.java)
            startActivity(intent)
        }

        // Módulo Reportes: Abre la actividad de listados (ej. clientes morosos).
        cardListados.setOnClickListener {
            val intent = Intent(this, ListadosActivity::class.java)
            startActivity(intent)
        }
    }
}