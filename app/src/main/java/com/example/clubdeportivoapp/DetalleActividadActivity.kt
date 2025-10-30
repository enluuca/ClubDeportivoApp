package com.example.clubdeportivoapp.ui.actividades

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.clubdeportivoapp.R
import com.example.clubdeportivoapp.data.dao.ActividadDao
import com.example.clubdeportivoapp.data.model.Actividad

/**
 * Actividad para Añadir, Ver o Editar los datos de una Actividad.
 * Gestiona la entidad Actividad.
 */
class DetalleActividadActivity : AppCompatActivity() {

    private lateinit var actividadDao: ActividadDao
    private var actividadId: Int = -1 // -1 indica Añadir nueva actividad

    // Referencias UI
    private lateinit var tvHeader: TextView
    private lateinit var etNombre: EditText
    private lateinit var etCosto: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_actividad)

        actividadDao = ActividadDao(this)
        actividadId = intent.getIntExtra(ActividadesActivity.EXTRA_ACTIVIDAD_ID, -1)

        initializeViews()
        setupListeners()

        if (actividadId != -1) {
            loadActividadData(actividadId)
        } else {
            // Modo Añadir
            tvHeader.text = "Añadir Nueva Actividad"
            btnEliminar.visibility = View.GONE // Oculta eliminar en modo añadir
        }
    }

    private fun initializeViews() {
        tvHeader = findViewById(R.id.tv_detalle_actividad_header)
        etNombre = findViewById(R.id.et_actividad_nombre)
        etCosto = findViewById(R.id.et_actividad_costo)
        btnGuardar = findViewById(R.id.btn_guardar_actividad)
        btnEliminar = findViewById(R.id.btn_eliminar_actividad)
    }

    private fun setupListeners() {
        btnGuardar.setOnClickListener { saveOrUpdateActividad() }
        btnEliminar.setOnClickListener { deleteActividad() }
    }

    private fun loadActividadData(id: Int) {
        // Carga los datos de la actividad para edición
        val actividad = actividadDao.getActividadById(id)
        if (actividad != null) {
            tvHeader.text = "Editar Actividad: ${actividad.nombre}"
            btnGuardar.text = "ACTUALIZAR ACTIVIDAD"
            btnEliminar.visibility = View.VISIBLE
            fillForm(actividad)
        } else {
            Toast.makeText(this, "Error: Actividad no encontrada.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fillForm(actividad: Actividad) {
        etNombre.setText(actividad.nombre)
        // Convierte Double a String para mostrar, usa formato sin localismo para edición
        etCosto.setText(actividad.costo.toString())
    }

    private fun saveOrUpdateActividad() {
        if (!validateFields()) return

        val nombre = etNombre.text.toString().trim()
        val costo = etCosto.text.toString().toDoubleOrNull() ?: 0.0

        val actividad = Actividad(
            id = if (actividadId != -1) actividadId else 0,
            nombre = nombre,
            costo = costo
        )

        val success: Boolean
        if (actividadId == -1) {
            // MODO AÑADIR
            val newId = actividadDao.insertActividad(actividad)
            success = newId > 0
            if (success) Toast.makeText(this, "Actividad guardada con ID: $newId", Toast.LENGTH_SHORT).show()
        } else {
            // MODO EDITAR
            val rowsAffected = actividadDao.updateActividad(actividad)
            success = rowsAffected > 0
            if (success) Toast.makeText(this, "Actividad actualizada.", Toast.LENGTH_SHORT).show()
        }

        if (success) {
            finish() // Cierra y regresa a la lista
        } else {
            Toast.makeText(this, "Error al guardar/actualizar la actividad.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteActividad() {
        if (actividadId != -1) {
            val rowsAffected = actividadDao.deleteActividad(actividadId)
            if (rowsAffected > 0) {
                Toast.makeText(this, "Actividad eliminada con éxito.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al eliminar la actividad.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateFields(): Boolean {
        if (etNombre.text.isNullOrEmpty()) {
            Toast.makeText(this, "El nombre de la actividad es obligatorio.", Toast.LENGTH_SHORT).show()
            return false
        }
        val costoStr = etCosto.text.toString()
        val costo = costoStr.toDoubleOrNull()
        if (costoStr.isNullOrEmpty() || costo == null || costo < 0) {
            Toast.makeText(this, "El costo debe ser un número válido y positivo.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
