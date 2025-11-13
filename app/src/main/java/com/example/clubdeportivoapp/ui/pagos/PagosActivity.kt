package com.example.clubdeportivoapp.ui.pagos

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.clubdeportivoapp.R
import com.example.clubdeportivoapp.data.dao.ActividadDao
import com.example.clubdeportivoapp.data.dao.ClienteDao
import com.example.clubdeportivoapp.data.dao.PagoDao
import com.example.clubdeportivoapp.data.model.Actividad
import com.example.clubdeportivoapp.data.model.Cliente
import com.example.clubdeportivoapp.data.model.Cuota
import com.example.clubdeportivoapp.data.model.RegistroActividad
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Actividad para la Gestión y Registro de Pagos (Cuotas de Socio o Registros de Actividad).
 */
class PagosActivity : AppCompatActivity() {

    // DAOs
    private lateinit var clienteDao: ClienteDao
    private lateinit var actividadDao: ActividadDao
    private lateinit var pagoDao: PagoDao

    // ESTADO y FECHAS (Usando SimpleDateFormat para compatibilidad con API 24)
    private var currentCliente: Cliente? = null
    private var allActividades: List<Actividad> = emptyList()
    private var selectedActividad: Actividad? = null
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US) // Formato compatible

    // Referencias UI (Inicialización y setupListeners omitidos por ser largos)
    private lateinit var etDniSearch: EditText
    private lateinit var btnBuscarCliente: Button
    private lateinit var tvClienteInfo: TextView
    private lateinit var cardOpcionesPago: View
    private lateinit var layoutPagoCuota: LinearLayout
    private lateinit var layoutRegistroActividad: LinearLayout

    // Cuota UI
    private lateinit var tvVencimientoAnterior: TextView
    private lateinit var etMedioCuota: EditText
    private lateinit var btnRegistrarCuota: Button

    // Registro Actividad UI
    private lateinit var spinnerActividad: Spinner
    private lateinit var tvCostoActividad: TextView
    private lateinit var etMedioActividad: EditText
    private lateinit var btnRegistrarActividadPago: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagos)

        clienteDao = ClienteDao(this)
        actividadDao = ActividadDao(this)
        pagoDao = PagoDao(this)

        initializeViews()
        setupListeners()
    }

    // (Funciones initializeViews() y setupListeners() omitidas por brevedad)

    private fun initializeViews() {
        etDniSearch = findViewById(R.id.et_pago_dni_search)
        btnBuscarCliente = findViewById(R.id.btn_pago_buscar_cliente)
        tvClienteInfo = findViewById(R.id.tv_pago_cliente_info)
        cardOpcionesPago = findViewById(R.id.card_opciones_pago)
        layoutPagoCuota = findViewById(R.id.layout_pago_cuota)
        layoutRegistroActividad = findViewById(R.id.layout_registro_actividad)
        tvVencimientoAnterior = findViewById(R.id.tv_pago_cuota_vencimiento_anterior)
        etMedioCuota = findViewById(R.id.et_pago_medio_cuota)
        btnRegistrarCuota = findViewById(R.id.btn_registrar_cuota)
        spinnerActividad = findViewById(R.id.spinner_actividad_select)
        tvCostoActividad = findViewById(R.id.tv_pago_costo_actividad)
        etMedioActividad = findViewById(R.id.et_pago_medio_actividad)
        btnRegistrarActividadPago = findViewById(R.id.btn_registrar_actividad_pago)
    }

    private fun setupListeners() {
        btnBuscarCliente.setOnClickListener { searchCliente() }
        btnRegistrarCuota.setOnClickListener { registrarCuota() }
        btnRegistrarActividadPago.setOnClickListener { registrarRegistroActividad() }
    }

    // (Lógica de Búsqueda y Setup Socio/NoSocio omitida por brevedad)
    private fun searchCliente() {
        val dni = etDniSearch.text.toString().trim()
        if (dni.isEmpty()) {
            Toast.makeText(this, "Ingrese un DNI para buscar.", Toast.LENGTH_SHORT).show()
            return
        }

        val dniInt = dni.toIntOrNull()
        if (dniInt == null) {
            Toast.makeText(this, "DNI inválido.", Toast.LENGTH_SHORT).show()
            return
        }

        currentCliente = clienteDao.getClienteByDni(dniInt)

        if (currentCliente != null) {
            handleClientFound(currentCliente!!)
        } else {
            Toast.makeText(this, "Cliente no encontrado con DNI: $dni", Toast.LENGTH_LONG).show()
            resetUI()
        }
    }

    private fun handleClientFound(cliente: Cliente) {
        val tipo = if (cliente.asociarse) "Socio Activo" else "No Socio"
        tvClienteInfo.text = "Cliente: ${cliente.nombre} ${cliente.apellido} ($tipo)"
        tvClienteInfo.visibility = View.VISIBLE
        cardOpcionesPago.visibility = View.VISIBLE

        if (cliente.asociarse) {
            setupSocioPayment(cliente.id)
        } else {
            setupNoSocioPayment(cliente.id)
        }
    }

    private fun resetUI() {
        currentCliente = null
        tvClienteInfo.visibility = View.GONE
        cardOpcionesPago.visibility = View.GONE
        layoutPagoCuota.visibility = View.GONE
        layoutRegistroActividad.visibility = View.GONE
    }

    private fun setupSocioPayment(socioId: Int) {
        layoutRegistroActividad.visibility = View.GONE
        layoutPagoCuota.visibility = View.VISIBLE

        val socioDetails = clienteDao.getSocioByClienteId(socioId)

        if (socioDetails != null) {
            val vencimientoStr = socioDetails.fechaVencimientoCuota
            val hoy = Calendar.getInstance().time

            var estado = "ACTIVO"
            var colorResId = R.color.color_socio

            try {
                val vencimientoDate = dateFormatter.parse(vencimientoStr)

                if (vencimientoDate != null && vencimientoDate.before(hoy)) {
                    estado = "MOROSO"
                    colorResId = R.color.color_moroso
                }
            } catch (e: Exception) {
                estado = "Fecha inválida"
                colorResId = R.color.black
            }

            tvVencimientoAnterior.text = "Cuota Vence: $vencimientoStr ($estado)"
            tvVencimientoAnterior.setTextColor(ContextCompat.getColor(this, colorResId))

        } else {
            tvVencimientoAnterior.text = "No hay datos de cuotas asociadas al socio."
            tvVencimientoAnterior.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
        etMedioCuota.setText("")
    }

    /**
     * ✅ CORRECCIÓN CLAVE: Calcula la fecha de hoy y el vencimiento a 30 días.
     */
    private fun getTodayAndNextVencimiento(): Pair<String, String> {
        val today = Calendar.getInstance()
        val todayStr = dateFormatter.format(today.time)

        val nextVencimiento = Calendar.getInstance()
        // 🔥 CAMBIO CRÍTICO: Sumar exactamente 30 días a la fecha actual
        nextVencimiento.add(Calendar.DAY_OF_YEAR, 30)

        val nextVencimientoStr = dateFormatter.format(nextVencimiento.time)
        return Pair(todayStr, nextVencimientoStr)
    }

    private fun registrarCuota() {
        val cliente = currentCliente ?: return
        val medioPago = etMedioCuota.text.toString().trim()

        if (medioPago.isEmpty()) {
            Toast.makeText(this, "Debe ingresar el medio de pago.", Toast.LENGTH_SHORT).show()
            return
        }

        // Usamos la función corregida
        val (fechaPago, fechaVencimiento) = getTodayAndNextVencimiento()

        // Monto y cálculo simplificado (asumimos un monto fijo por defecto, Ej: 5000)
        val montoBase = 5000.00
        val montoTotal = montoBase

        val nuevaCuota = Cuota(
            idSocio = cliente.id,
            fechaPago = fechaPago,
            monto = montoBase,
            medioPago = medioPago,
            cantidadCuotas = 1,
            montoTotal = montoTotal,
            fechaVencimiento = fechaVencimiento
        )

        val newId = pagoDao.insertCuota(nuevaCuota)
        if (newId > 0) {
            Toast.makeText(this, "Cuota registrada con éxito. Nueva fecha de vencimiento: $fechaVencimiento", Toast.LENGTH_LONG).show()

            // Actualiza la tabla Socio con la nueva fecha de vencimiento
            clienteDao.updateSocioVencimiento(cliente.id, fechaVencimiento)

            resetUI()
            etDniSearch.setText("")
        } else {
            Toast.makeText(this, "Error al registrar la cuota.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNoSocioPayment(clienteId: Int) {
        layoutPagoCuota.visibility = View.GONE
        layoutRegistroActividad.visibility = View.VISIBLE

        allActividades = actividadDao.getAllActividades()
        loadActivitiesSpinner()
        etMedioActividad.setText("")
    }

    private fun loadActivitiesSpinner() {
        val activityNames = allActividades.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerActividad.adapter = adapter

        spinnerActividad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedActividad = allActividades[position]
                displayActivityCost(selectedActividad!!.costo)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedActividad = null
                tvCostoActividad.text = "Seleccione una actividad"
            }
        }
    }

    private fun displayActivityCost(costo: Double) {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        tvCostoActividad.text = "Costo: ${format.format(costo)}"
    }

    private fun registrarRegistroActividad() {
        val cliente = currentCliente ?: return
        val actividad = selectedActividad
        val medioPago = etMedioActividad.text.toString().trim()

        if (actividad == null || medioPago.isEmpty()) {
            Toast.makeText(this, "Seleccione una actividad e ingrese el medio de pago.", Toast.LENGTH_SHORT).show()
            return
        }

        val (fechaPago, _) = getTodayAndNextVencimiento()

        val registro = RegistroActividad(
            idCliente = cliente.id,
            idActividad = actividad.id,
            fechaPago = fechaPago,
            montoPagado = actividad.costo,
            medioPago = medioPago,
            fechaExpiracion = fechaPago
        )

        val newId = pagoDao.insertRegistroActividad(registro)
        if (newId > 0) {
            Toast.makeText(this, "Registro de actividad (${actividad.nombre}) exitoso.", Toast.LENGTH_LONG).show()
            resetUI()
            etDniSearch.setText("")
        } else {
            Toast.makeText(this, "Error al registrar la actividad.", Toast.LENGTH_SHORT).show()
        }
    }
}