package com.example.clubdeportivoapp.ui.actividades

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clubdeportivoapp.R
import com.example.clubdeportivoapp.data.model.Actividad
import java.text.NumberFormat
import java.util.Locale

/**
 * [Clase de Nivel Intermedio]
 * Adapter para el RecyclerView que muestra la lista de Actividades.
 * * Responsable de: Mapear los datos del modelo Actividad al layout de la fila.
 *
 * @param actividades Lista de objetos Actividad a mostrar (inmutable por el constructor).
 * @param onItemClicked Callback que se ejecuta al hacer clic en una actividad.
 */
class ActividadesAdapter(
    // Lista de datos que el adaptador maneja
    private var actividades: List<Actividad>,
    // Lambda para manejar el evento de clic en la Activity
    private val onItemClicked: (Actividad) -> Unit
) : RecyclerView.Adapter<ActividadesAdapter.ActividadViewHolder>() {

    /**
     * ViewHolder: Contenedor que mantiene las referencias a las vistas de 'actividad_list_item.xml'.
     * Esto evita buscar los IDs repetidamente, mejorando el rendimiento.
     */
    class ActividadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // IDs que deben coincidir con el layout de la fila
        val tvNombre: TextView = view.findViewById(R.id.tv_actividad_nombre)
        val tvCosto: TextView = view.findViewById(R.id.tv_actividad_costo)
    }

    /**
     * Se llama cuando el RecyclerView necesita un nuevo ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        // 1. Obtiene el LayoutInflater del contexto padre (RecyclerView)
        val inflater = LayoutInflater.from(parent.context)
        // 2. Infla el layout de la fila
        val view = inflater.inflate(R.layout.actividad_list_item, parent, false)
        return ActividadViewHolder(view)
    }

    /**
     * Asigna los datos a las vistas dentro del ViewHolder.
     */
    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        val actividad = actividades[position]
        // val context = holder.itemView.context // No es necesario si solo usamos Locale.getDefault()

        // 1. Formatear el costo como moneda local (ej: $1.500,00)
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val costoFormateado = format.format(actividad.costo)

        // 2. Asignación de datos
        holder.tvNombre.text = actividad.nombre
        holder.tvCosto.text = "Costo: ${costoFormateado}"

        // 3. Listener para el clic (navegar al detalle/edición)
        // Llama al callback lambda definido en el constructor.
        holder.itemView.setOnClickListener {
            onItemClicked(actividad)
        }
    }

    /**
     * Devuelve el número total de ítems en la lista.
     */
    override fun getItemCount() = actividades.size

    /**
     * Método público para actualizar la lista de datos del adaptador.
     * Utilizado principalmente después de una búsqueda/filtrado o al recargar los datos de la DB.
     * ⚠️ notifyDataSetChanged() es eficiente para listas pequeñas.
     */
    fun updateActividades(newActividades: List<Actividad>) {
        actividades = newActividades
        notifyDataSetChanged()
    }
}