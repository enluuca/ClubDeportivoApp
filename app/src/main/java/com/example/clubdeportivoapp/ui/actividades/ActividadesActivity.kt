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
 * Actividad para la Gestión y Listado de Actividades.
 * Responde al botón "Actividades" del MenuPrincipalActivity.
 */
class ActividadesActivity : AppCompatActivity() {

    // Constante para el ID de actividad que se pasa entre actividades
    companion object {
        const val EXTRA_ACTIVIDAD_ID = "ACTIVIDAD_ID"
    }

    private lateinit var actividadDao: ActividadDao
    private lateinit var actividadesAdapter: ActividadesAdapter
    private lateinit var rvActividades: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var etSearchActividad: EditText
    private lateinit var btnAddActividad: Button
    private var allActividades: List<Actividad> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividades) // Layout: activity_actividades.xml

        // Inicialización de DAO y referencias de UI
        actividadDao = ActividadDao(this)
        rvActividades = findViewById(R.id.rv_actividades)
        tvEmptyState = findViewById(R.id.tv_actividades_empty_state)
        etSearchActividad = findViewById(R.id.et_search_actividad)
        btnAddActividad = findViewById(R.id.btn_add_actividad)

        setupRecyclerView()
        setupSearchListener()
        setupAddButton()

        // El ActionBar/Toolbar podría configurarse aquí
        supportActionBar?.title = "Gestión de Actividades"
    }

    override fun onResume() {
        super.onResume()
        // Cargar los datos cada vez que la actividad se hace visible (al volver de edición/adición)
        loadActividades()
    }

    private fun setupRecyclerView() {
        // Callback para el clic en un ítem (navegación al detalle/edición)
        actividadesAdapter = ActividadesAdapter(allActividades) { actividad ->
            navigateToDetalleActividad(actividad.id)
        }
        rvActividades.layoutManager = LinearLayoutManager(this)
        rvActividades.adapter = actividadesAdapter
    }

    private fun setupSearchListener() {
        etSearchActividad.doAfterTextChanged { text ->
            filterActividades(text.toString())
        }
    }

    private fun setupAddButton() {
        // Navegación a la actividad de Adición (sin pasar ID)
        btnAddActividad.setOnClickListener {
            navigateToDetalleActividad(actividadId = -1)
        }
    }

    /**
     * Carga todas las actividades de la base de datos y actualiza la lista.
     */
    private fun loadActividades() {
        // Nota: Las operaciones de DB se mantienen en el hilo principal para simplicidad.
        allActividades = actividadDao.getAllActividades()
        actividadesAdapter.updateActividades(allActividades)
        updateEmptyState()
        etSearchActividad.setText("") // Limpia la búsqueda
    }

    /**
     * Filtra la lista de actividades basándose en el texto de búsqueda (por nombre).
     */
    private fun filterActividades(query: String) {
        val queryLower = query.lowercase(Locale.getDefault())

        val filteredList = allActividades.filter { actividad ->
            query.isEmpty() ||
                    actividad.nombre.lowercase(Locale.getDefault()).contains(queryLower)
        }

        actividadesAdapter.updateActividades(filteredList)
        updateEmptyState(filteredList.isEmpty())
    }

    /**
     * Actualiza la visibilidad del mensaje de lista vacía.
     */
    private fun updateEmptyState(isFilteredEmpty: Boolean = allActividades.isEmpty()) {
        tvEmptyState.visibility = if (isFilteredEmpty) View.VISIBLE else View.GONE
    }

    /**
     * Navega a la actividad de detalle/edición/adición de actividad.
     * @param actividadId El ID de la actividad. Usa -1 para indicar una nueva.
     */
    private fun navigateToDetalleActividad(actividadId: Int) {
        // La clase DetalleActividadActivity debe crearse en un bloque posterior
        val intent = Intent(this, DetalleActividadActivity::class.java).apply {
            putExtra(EXTRA_ACTIVIDAD_ID, actividadId)
        }
        startActivity(intent)
    }
}

