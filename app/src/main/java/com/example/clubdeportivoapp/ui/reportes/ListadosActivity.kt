package com.example.clubdeportivoapp.ui.reportes

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clubdeportivoapp.R
import com.example.clubdeportivoapp.data.dao.ClienteDao
import com.example.clubdeportivoapp.data.model.Cliente
import com.example.clubdeportivoapp.ui.clientes.ClientesAdapter // Reutilizamos el adaptador de clientes

/**
 * Actividad para mostrar reportes y listados.
 */
class ListadosActivity : AppCompatActivity() {

    private lateinit var clienteDao: ClienteDao
    private lateinit var rvResultado: RecyclerView
    private lateinit var cardReporteMorosos: CardView
    private lateinit var tvReporteTitle: TextView
    private lateinit var clientesAdapter: ClientesAdapter
    private lateinit var tvEmptyState: TextView // Necesitas añadir este TextView al layout si quieres usarlo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listados)
        supportActionBar?.title = "Reportes"

        clienteDao = ClienteDao(this)

        initializeViews()
        setupListeners()
        setupRecyclerView()
    }

    private fun initializeViews() {
        rvResultado = findViewById(R.id.rv_reporte_resultado)
        cardReporteMorosos = findViewById(R.id.card_reporte_morosos)
        tvReporteTitle = findViewById(R.id.tv_reporte_title)
        // Puedes usar el tv_reporte_title como un mensaje de "vacío" o añadir un TextView separado
    }

    private fun setupRecyclerView() {
        // Reutilizamos el adaptador y el item de lista de clientes
        clientesAdapter = ClientesAdapter(emptyList()) { cliente ->
            Toast.makeText(this, "Cliente seleccionado: ${cliente.nombre}", Toast.LENGTH_SHORT).show()
            // Aquí se podría navegar a la edición si fuera necesario
        }
        rvResultado.layoutManager = LinearLayoutManager(this)
        rvResultado.adapter = clientesAdapter
    }

    private fun setupListeners() {
        cardReporteMorosos.setOnClickListener {
            // Ejecutar el reporte de Morosos
            loadReporteMorosos()
        }
    }

    /**
     * Carga y muestra el listado de Clientes Morosos.
     */
    private fun loadReporteMorosos() {
        try {
            // Llama a la función del DAO (añadida en el ClienteDao.kt)
            val morosos: List<Cliente> = clienteDao.getClientesMorosos()

            tvReporteTitle.text = "Clientes Morosos (${morosos.size} encontrados)"
            clientesAdapter.updateClientes(morosos)

            if (morosos.isEmpty()) {
                Toast.makeText(this, "¡Felicidades! No hay clientes morosos.", Toast.LENGTH_LONG).show()
                // Si tienes un tvEmptyState en el layout, podrías mostrarlo aquí.
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al generar reporte: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}