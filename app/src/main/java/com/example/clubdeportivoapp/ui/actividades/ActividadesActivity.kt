package com.example.clubdeportivoapp.ui.actividades

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
import com.example.clubdeportivoapp.data.dao.ActividadDao
import com.example.clubdeportivoapp.data.model.Actividad
import java.util.Locale

/**
 * [Clase de Nivel Intermedio]
 * Actividad principal para la Gestión y Listado de Actividades.
 * * Responsable de: Mostrar el listado, manejar la búsqueda y la navegación al detalle.
 */
class ActividadesActivity : AppCompatActivity() {

    // Constante para identificar el ID de la actividad al navegar
    companion object {
        const val EXTRA_ACTIVIDAD_ID = "ACTIVIDAD_ID"
    }

    private lateinit var actividadDao: ActividadDao
    private lateinit var actividadesAdapter: ActividadesAdapter
    // Referencias a elementos de la UI
    private lateinit var rvActividades: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var etSearchActividad: EditText
    private lateinit var btnAddActividad: Button
    // Lista completa de actividades cargadas de la DB
    private var allActividades: List<Actividad> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividades) // Layout: activity_actividades.xml

        // Inicialización del DAO (dependencia principal de datos)
        actividadDao = ActividadDao(this)

        // Inicialización de la UI
        initializeViews()
        setupRecyclerView()
        setupSearchListener()
        setupAddButton()

        supportActionBar?.title = "Gestión de Actividades"
    }

    /**
     * Inicializa las referencias a las vistas mediante findViewById.
     */
    private fun initializeViews() {
        rvActividades = findViewById(R.id.rv_actividades)
        tvEmptyState = findViewById(R.id.tv_actividades_empty_state)
        etSearchActividad = findViewById(R.id.et_search_actividad)
        btnAddActividad = findViewById(R.id.btn_add_actividad)
    }

    override fun onResume() {
        super.onResume()
        // Cargar los datos cada vez que la actividad se hace visible (al volver de edición/adición)
        // Esto asegura que la lista esté siempre actualizada.
        loadActividades()
    }

    /**
     * Configura el RecyclerView, el LayoutManager y el Adapter.
     */
    private fun setupRecyclerView() {
        // Inicializamos el Adapter con el callback de clic
        actividadesAdapter = ActividadesAdapter(allActividades) { actividad ->
            navigateToDetalleActividad(actividad.id)
        }
        rvActividades.layoutManager = LinearLayoutManager(this)
        rvActividades.adapter = actividadesAdapter
    }

    /**
     * Configura el listener de búsqueda para filtrar la lista al escribir.
     */
    private fun setupSearchListener() {
        etSearchActividad.doAfterTextChanged { text ->
            filterActividades(text.toString())
        }
    }

    /**
     * Configura el botón Añadir para navegar a la actividad de detalle en modo inserción.
     */
    private fun setupAddButton() {
        btnAddActividad.setOnClickListener {
            navigateToDetalleActividad(actividadId = -1) // -1 indica nuevo registro
        }
    }

    /**
     * Carga todas las actividades de la base de datos y actualiza la lista del adaptador.
     */
    private fun loadActividades() {
        // Nota: La llamada a la DB se realiza en el hilo principal (simple)
        allActividades = actividadDao.getAllActividades()
        actividadesAdapter.updateActividades(allActividades)
        updateEmptyState()
        etSearchActividad.setText("") // Limpia el campo de búsqueda después de recargar
    }

    /**
     * Filtra la lista completa (allActividades) basándose en el texto de búsqueda.
     */
    private fun filterActividades(query: String) {
        val queryLower = query.lowercase(Locale.getDefault())

        val filteredList = allActividades.filter { actividad ->
            // Si la consulta está vacía, muestra todos. Si no, compara el nombre en minúsculas.
            query.isEmpty() ||
                    actividad.nombre.lowercase(Locale.getDefault()).contains(queryLower)
        }

        actividadesAdapter.updateActividades(filteredList)
        updateEmptyState(filteredList.isEmpty())
    }

    /**
     * Actualiza la visibilidad del mensaje de lista vacía según el estado actual de la lista.
     */
    private fun updateEmptyState(isFilteredEmpty: Boolean = allActividades.isEmpty()) {
        tvEmptyState.visibility = if (isFilteredEmpty) View.VISIBLE else View.GONE
    }

    /**
     * Navega a la actividad de detalle/edición, pasando el ID de la actividad.
     */
    private fun navigateToDetalleActividad(actividadId: Int) {
        val intent = Intent(this, DetalleActividadActivity::class.java).apply {
            putExtra(EXTRA_ACTIVIDAD_ID, actividadId)
        }
        startActivity(intent)
    }
}