package com.example.clubdeportivoapp.ui.clientes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.clubdeportivoapp.R
import com.example.clubdeportivoapp.data.dao.ClienteDao
import com.example.clubdeportivoapp.data.model.Cliente
import com.example.clubdeportivoapp.data.model.Socio
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * [Clase de Nivel Intermedio/Avanzado]
 * Adapter para el RecyclerView que muestra la lista de Clientes.
 * Incluye la lógica para determinar el estado de "Socio Moroso" directamente en la vista.
 */
class ClientesAdapter(
    private var clientes: List<Cliente>,
    private val onItemClicked: (Cliente) -> Unit,
    // ✅ CORRECCIÓN CLAVE: El DAO debe pasarse desde la Activity para evitar fugas de memoria
    private val clienteDao: ClienteDao
) : RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder>() {

    // El DAO ya no necesita ser 'lateinit' ni inicializarse en onCreateViewHolder.
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * ViewHolder que mantiene las referencias a las vistas de 'activity_cliente_list_item.xml'.
     */
    class ClienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Se asume que estos IDs están correctos en el layout XML.
        val tvNombreCompleto: TextView = view.findViewById(R.id.tv_cliente_name)
        val tvDni: TextView = view.findViewById(R.id.tv_cliente_dni)
        val tvSocioStatus: TextView = view.findViewById(R.id.tv_socio_status)
        val statusIndicator: ImageView = view.findViewById(R.id.img_status_indicator)
    }

    // ✅ CORRECCIÓN CLAVE: Se elimina la lógica de inicialización del DAO de aquí.
    // Esto previene un error de rendimiento y de contexto.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_cliente_list_item, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        val context = holder.itemView.context

        // 1. Datos básicos
        holder.tvNombreCompleto.text = "${cliente.apellido}, ${cliente.nombre}"
        holder.tvDni.text = "DNI: ${cliente.dni}"

        // 2. Lógica del Estado (Activo vs. Moroso vs. No Socio)
        if (cliente.asociarse) {
            // Es Socio: Realizamos una consulta a la DB para verificar el estado.
            // ⚠️ ADVERTENCIA: La consulta a la DB en onBindViewHolder (hilo principal)
            // causa un bloqueo de la UI. En producción, el Repositorio debería
            // precargar este estado y pasarlo al Adapter.
            val socioDetails: Socio? = clienteDao.getSocioByClienteId(cliente.id)

            if (socioDetails != null && isCuotaVencida(socioDetails.fechaVencimientoCuota)) {
                // Estado: MOROSO (Cuota vencida)
                holder.tvSocioStatus.text = "Socio MOROSO"
                holder.statusIndicator.setImageResource(R.drawable.ic_status_moroso)
                holder.tvSocioStatus.setTextColor(ContextCompat.getColor(context, R.color.color_moroso))
            } else {
                // Estado: ACTIVO (Cuota al día o error de fecha)
                holder.tvSocioStatus.text = "Socio Activo"
                holder.statusIndicator.setImageResource(R.drawable.ic_status_active)
                holder.tvSocioStatus.setTextColor(ContextCompat.getColor(context, R.color.color_socio))
            }

        } else {
            // Es No Socio
            holder.tvSocioStatus.text = "No Socio / Invitado"
            holder.statusIndicator.setImageResource(R.drawable.ic_status_inactive)
            holder.tvSocioStatus.setTextColor(ContextCompat.getColor(context, R.color.color_no_socio))
        }

        // 3. Listener para el clic (navegar al detalle/edición)
        holder.itemView.setOnClickListener {
            onItemClicked(cliente)
        }
    }

    override fun getItemCount() = clientes.size

    /**
     * Compara la fecha de vencimiento de la cuota con la fecha actual.
     * Utiliza SimpleDateFormat para la compatibilidad de API.
     */
    private fun isCuotaVencida(fechaVencimiento: String): Boolean {
        return try {
            // La fecha actual (Date()) se compara con la fecha parseada de la DB.
            val vencimiento = dateFormat.parse(fechaVencimiento)
            vencimiento != null && vencimiento.before(Date())
        } catch (e: Exception) {
            // Si el formato de fecha es inválido, asumimos que no es moroso para evitar un crash.
            false
        }
    }

    /**
     * Permite actualizar la lista de clientes (útil para la función de búsqueda/filtrado).
     */
    fun updateClientes(newClientes: List<Cliente>) {
        clientes = newClientes
        notifyDataSetChanged()
    }
}