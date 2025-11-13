package com.example.clubdeportivoapp.ui.clientes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clubdeportivoapp.R
import com.example.clubdeportivoapp.data.dao.ClienteDao
import com.example.clubdeportivoapp.data.model.Cliente
import java.util.Locale

/**
 * [Clase de Nivel Intermedio]
 * Actividad para la Gestión y Listado de Clientes (Socios y NoSocios).
 * Carga la lista, maneja la búsqueda y la navegación.
 */
class ClientesActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLIENTE_ID = "CLIENTE_ID"
    }

    private lateinit var clienteDao: ClienteDao
    private lateinit var clientesAdapter: ClientesAdapter
    private lateinit var rvClientes: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var etSearchCliente: EditText
    private lateinit var btnAddCliente: Button
    private var allClientes: List<Cliente> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes)

        // 1. Inicialización del DAO
        clienteDao = ClienteDao(this)

        // 2. Referencias de UI (Corregidas para coincidir con activity_clientes.xml)
        rvClientes = findViewById(R.id.rv_clientes)
        tvEmptyState = findViewById(R.id.tv_empty_state)
        etSearchCliente = findViewById(R.id.et_search_cliente)
        btnAddCliente = findViewById(R.id.btn_add_cliente)

        setupRecyclerView()
        setupSearchListener()
        setupAddButton()
    }

    /**
     * Configura el RecyclerView y el Adapter.
     * ✅ AJUSTE CLAVE: Se pasa el clienteDao al constructor del Adapter.
     */
    private fun setupRecyclerView() {
        clientesAdapter = ClientesAdapter(
            clientes = allClientes,
            onItemClicked = { cliente -> navigateToDetalleCliente(cliente.id) },
            clienteDao = clienteDao // Inyecta la dependencia del DAO
        )
        rvClientes.layoutManager = LinearLayoutManager(this)
        rvClientes.adapter = clientesAdapter
    }

    // (Resto de funciones omitidas por brevedad en la revisión, se mantienen iguales)

    private fun setupSearchListener() {
        etSearchCliente.doAfterTextChanged { text ->
            filterClientes(text.toString())
        }
    }

    private fun setupAddButton() {
        // Navegación a la actividad de Adición (usando el botón regular)
        btnAddCliente.setOnClickListener {
            navigateToDetalleCliente(clienteId = -1) // -1 indica nuevo cliente
        }
    }

    /**
     * Carga todos los clientes de la base de datos y actualiza la lista.
     */
    private fun loadClientes() {
        allClientes = clienteDao.getAllClientes()
        clientesAdapter.updateClientes(allClientes)
        updateEmptyState()
        etSearchCliente.setText("")
    }

    /**
     * Filtra la lista de clientes basándose en el texto de búsqueda.
     */
    private fun filterClientes(query: String) {
        val queryLower = query.lowercase(Locale.getDefault())

        val filteredList = allClientes.filter { cliente ->
            query.isEmpty() ||
                    cliente.nombre.lowercase(Locale.getDefault()).contains(queryLower) ||
                    cliente.apellido.lowercase(Locale.getDefault()).contains(queryLower) ||
                    cliente.dni.toString().contains(query)
        }

        clientesAdapter.updateClientes(filteredList)
        updateEmptyState(filteredList.isEmpty())
    }

    /**
     * Actualiza la visibilidad del mensaje de lista vacía.
     */
    private fun updateEmptyState(isFilteredEmpty: Boolean = allClientes.isEmpty()) {
        tvEmptyState.visibility = if (isFilteredEmpty) View.VISIBLE else View.GONE
    }

    /**
     * Navega a la actividad de detalle/edición/adición de cliente.
     */
    private fun navigateToDetalleCliente(clienteId: Int) {
        val intent = Intent(this, DetalleClienteActivity::class.java).apply {
            putExtra(EXTRA_CLIENTE_ID, clienteId)
        }
        startActivity(intent)
    }

    // Al regresar de DetalleClienteActivity, recarga la lista para ver los cambios
    override fun onResume() {
        super.onResume()
        loadClientes()
    }
}