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
 * [Clase de Nivel Intermedio]
 * Actividad para Añadir, Ver o Editar los datos de una Actividad.
 * Gestiona la entidad Actividad y su persistencia mediante ActividadDao.
 */
class DetalleActividadActivity : AppCompatActivity() {

    private lateinit var actividadDao: ActividadDao
    // Almacena el ID de la actividad. -1 indica el modo de inserción (Añadir).
    private var actividadId: Int = -1

    // Referencias UI
    private lateinit var tvHeader: TextView
    private lateinit var etNombre: EditText
    private lateinit var etCosto: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_actividad) // Se carga la interfaz del formulario

        // Inicializar el DAO
        actividadDao = ActividadDao(this)
        // Recuperar el ID pasado desde ActividadesActivity.kt
        actividadId = intent.getIntExtra(ActividadesActivity.EXTRA_ACTIVIDAD_ID, -1)

        initializeViews()
        setupListeners()

        // Determinar el modo de la actividad (Añadir o Editar)
        if (actividadId != -1) {
            loadActividadData(actividadId) // Modo Edición: Cargar datos
        } else {
            // Modo Añadir: Configurar la UI para una nueva entrada
            tvHeader.text = "Añadir Nueva Actividad"
            btnEliminar.visibility = View.GONE
        }
    }

    /**
     * Inicializa las referencias a los elementos de la interfaz de usuario.
     */
    private fun initializeViews() {
        tvHeader = findViewById(R.id.tv_detalle_actividad_header)
        etNombre = findViewById(R.id.et_actividad_nombre)
        etCosto = findViewById(R.id.et_actividad_costo)
        btnGuardar = findViewById(R.id.btn_guardar_actividad)
        btnEliminar = findViewById(R.id.btn_eliminar_actividad)
    }

    /**
     * Configura los listeners para los botones de acción.
     */
    private fun setupListeners() {
        btnGuardar.setOnClickListener { saveOrUpdateActividad() }
        btnEliminar.setOnClickListener { deleteActividad() }
    }

    /**
     * Carga los datos de una actividad existente en los campos del formulario.
     */
    private fun loadActividadData(id: Int) {
        // Nota: Idealmente, esta llamada a la DB debería ser asíncrona.
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

    /**
     * Rellena los campos de texto con los datos del objeto Actividad.
     */
    private fun fillForm(actividad: Actividad) {
        etNombre.setText(actividad.nombre)
        // Convertir el Double a String para mostrarlo en el EditText
        etCosto.setText(actividad.costo.toString())
    }

    /**
     * Valida los campos y ejecuta la inserción o actualización de la actividad.
     */
    private fun saveOrUpdateActividad() {
        if (!validateFields()) return // Si la validación falla, sale de la función

        // 1. Recolección y conversión de datos.
        val nombre = etNombre.text.toString().trim()
        // Uso de toDoubleOrNull para manejar la entrada del usuario de forma segura.
        val costo = etCosto.text.toString().toDoubleOrNull() ?: 0.0

        // 2. Creación del objeto modelo (Actividad).
        val actividad = Actividad(
            // El ID es 0 si es nuevo, o el ID existente si es edición.
            id = if (actividadId != -1) actividadId else 0,
            nombre = nombre,
            costo = costo
        )

        val success: Boolean
        if (actividadId == -1) {
            // MODO AÑADIR: Llama a la función de inserción del DAO.
            val newId = actividadDao.insertActividad(actividad)
            success = newId > 0
            if (success) Toast.makeText(this, "Actividad guardada con ID: $newId", Toast.LENGTH_SHORT).show()
        } else {
            // MODO EDITAR: Llama a la función de actualización del DAO.
            val rowsAffected = actividadDao.updateActividad(actividad)
            success = rowsAffected > 0
            if (success) Toast.makeText(this, "Actividad actualizada.", Toast.LENGTH_SHORT).show()
        }

        if (success) {
            finish() // Cierra la actividad para volver a la lista (ActividadesActivity)
        } else {
            Toast.makeText(this, "Error al guardar/actualizar la actividad.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Ejecuta la lógica para eliminar la actividad de la base de datos.
     */
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

    /**
     * Valida los campos del formulario.
     * Revisa que el nombre no esté vacío y que el costo sea un número válido y positivo.
     */
    private fun validateFields(): Boolean {
        if (etNombre.text.isNullOrEmpty()) {
            Toast.makeText(this, "El nombre de la actividad es obligatorio.", Toast.LENGTH_SHORT).show()
            return false
        }
        val costoStr = etCosto.text.toString()
        val costo = costoStr.toDoubleOrNull()
        // Validación combinada: El costo no debe estar vacío Y debe ser convertible a un número válido (>= 0).
        if (costoStr.isNullOrEmpty() || costo == null || costo < 0) {
            Toast.makeText(this, "El costo debe ser un número válido y positivo.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}