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
 * Adapter para el RecyclerView que muestra la lista de Actividades.
 * Gestiona el layout 'actividad_list_item.xml'.
 *
 * @param actividades Lista de objetos Actividad a mostrar.
 * @param onItemClicked Callback que se ejecuta al hacer clic en una actividad.
 */
class ActividadesAdapter(
    private var actividades: List<Actividad>,
    private val onItemClicked: (Actividad) -> Unit
) : RecyclerView.Adapter<ActividadesAdapter.ActividadViewHolder>() {

    /**
     * ViewHolder que mantiene las referencias a las vistas de 'actividad_list_item.xml'.
     */
    class ActividadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Aseguramos que los IDs coincidan con el layout
        val tvNombre: TextView = view.findViewById(R.id.tv_actividad_nombre)
        val tvCosto: TextView = view.findViewById(R.id.tv_actividad_costo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        // Aseguramos que el nombre del layout sea correcto
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.actividad_list_item, parent, false)
        return ActividadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        val actividad = actividades[position]

        // Formatear el costo como moneda local
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val costoFormateado = format.format(actividad.costo)

        holder.tvNombre.text = actividad.nombre
        holder.tvCosto.text = "Costo: ${costoFormateado}"

        // Listener para el clic (navegar al detalle/edición)
        holder.itemView.setOnClickListener {
            onItemClicked(actividad)
        }
    }

    override fun getItemCount() = actividades.size

    /**
     * Permite actualizar la lista de actividades (útil para la función de búsqueda/filtrado).
     */
    fun updateActividades(newActividades: List<Actividad>) {
        actividades = newActividades
        notifyDataSetChanged()
    }
}