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
 * [Clase de Nivel Intermedio]
 * Actividad para mostrar reportes y listados.
 */
class ListadosActivity : AppCompatActivity() {

    private lateinit var clienteDao: ClienteDao
    private lateinit var rvResultado: RecyclerView
    private lateinit var cardReporteMorosos: CardView
    private lateinit var tvReporteTitle: TextView
    private lateinit var clientesAdapter: ClientesAdapter
    private lateinit var tvEmptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listados)
        supportActionBar?.title = "Reportes"

        // Inicializar el DAO
        clienteDao = ClienteDao(this)

        initializeViews()
        setupListeners()
        setupRecyclerView()
    }

    private fun initializeViews() {
        rvResultado = findViewById(R.id.rv_reporte_resultado)
        cardReporteMorosos = findViewById(R.id.card_reporte_morosos)
        tvReporteTitle = findViewById(R.id.tv_reporte_title)
        // [Nota]: El TextView tvEmptyState no se inicializa aquí ya que no está en el XML proporcionado.
    }

    private fun setupRecyclerView() {
        // ✅ CORRECCIÓN CLAVE: Se pasa el clienteDao al constructor del adaptador,
        // y se elimina la línea duplicada que causaba el error de sintaxis.
        clientesAdapter = ClientesAdapter(
            clientes = emptyList(),
            onItemClicked = { cliente ->
                Toast.makeText(this, "Cliente seleccionado: ${cliente.nombre}", Toast.LENGTH_SHORT).show()
            },
            clienteDao = clienteDao // Dependencia necesaria para verificar morosidad en el Adapter
        )
        rvResultado.layoutManager = LinearLayoutManager(this)
        rvResultado.adapter = clientesAdapter
    }

    private fun setupListeners() {
        cardReporteMorosos.setOnClickListener {
            loadReporteMorosos()
        }
    }

    /**
     * Carga y muestra el listado de Clientes Morosos.
     */
    private fun loadReporteMorosos() {
        try {
            // Llama a la función de reporte del DAO (getClientesMorosos)
            val morosos: List<Cliente> = clienteDao.getClientesMorosos()

            tvReporteTitle.text = "Clientes Morosos (${morosos.size} encontrados)"
            clientesAdapter.updateClientes(morosos)

            if (morosos.isEmpty()) {
                Toast.makeText(this, "¡Felicidades! No hay clientes morosos.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al generar reporte: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}