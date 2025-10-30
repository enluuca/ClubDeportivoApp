package com.example.clubdeportivoapp.ui.clientes

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.clubdeportivoapp.R
import com.example.clubdeportivoapp.data.dao.ClienteDao
import com.example.clubdeportivoapp.data.model.Cliente
import com.example.clubdeportivoapp.data.model.NoSocio
import com.example.clubdeportivoapp.data.model.Socio
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Actividad para Añadir o Editar los datos de un Cliente.
 * Incluye la funcionalidad de generar, previsualizar y compartir una credencial.
 */
class DetalleClienteActivity : AppCompatActivity() {

    private val STORAGE_PERMISSION_CODE = 101

    private lateinit var clienteDao: ClienteDao
    private var clienteId: Int = -1
    private var loadedCliente: Cliente? = null

    // Referencias UI
    private lateinit var tvHeader: TextView
    private lateinit var etDni: EditText
    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etFechaNacimiento: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etTelefono: EditText
    private lateinit var cbAptoFisico: CheckBox
    private lateinit var rgMembresia: RadioGroup
    private lateinit var rbSocio: RadioButton
    private lateinit var rbNoSocio: RadioButton
    private lateinit var layoutSocioDetails: LinearLayout
    private lateinit var etVencimientoCuota: EditText
    private lateinit var cbCarnetEntregado: CheckBox
    private lateinit var btnGuardar: Button
    private lateinit var btnGenerarCredencial: Button
    private lateinit var temporaryCredencialLayout: View // Contenedor para inflar activity_credencial.xml

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_cliente)

        clienteDao = ClienteDao(this)
        clienteId = intent.getIntExtra(ClientesActivity.EXTRA_CLIENTE_ID, -1)

        // Inicializar la vista de la credencial (activity_credencial.xml)
        temporaryCredencialLayout = layoutInflater.inflate(R.layout.activity_credencial, null)

        initializeViews()
        setupListeners()

        if (clienteId != -1) {
            loadClienteData(clienteId)
        } else {
            tvHeader.text = "Añadir Nuevo Cliente"
            btnGenerarCredencial.visibility = View.GONE
        }
    }

    private fun initializeViews() {
        // Inicializaciones de vistas omitidas por brevedad, asumiendo IDs de activity_detalle_cliente.xml
        tvHeader = findViewById(R.id.tv_detalle_header)
        etDni = findViewById(R.id.et_dni)
        etNombre = findViewById(R.id.et_nombre)
        etApellido = findViewById(R.id.et_apellido)
        etFechaNacimiento = findViewById(R.id.et_fecha_nacimiento)
        etDireccion = findViewById(R.id.et_direccion)
        etTelefono = findViewById(R.id.et_telefono)
        cbAptoFisico = findViewById(R.id.cb_apto_fisico)
        rgMembresia = findViewById(R.id.rg_membresia)
        rbSocio = findViewById(R.id.rb_socio)
        rbNoSocio = findViewById(R.id.rb_no_socio)
        layoutSocioDetails = findViewById(R.id.layout_socio_details)
        etVencimientoCuota = findViewById(R.id.et_vencimiento_cuota)
        cbCarnetEntregado = findViewById(R.id.cb_carnet_entregado)
        btnGuardar = findViewById(R.id.btn_guardar_cliente)
        btnGenerarCredencial = findViewById(R.id.btn_generar_credencial)
    }

    private fun setupListeners() {
        // Setup Listeners omitidos por brevedad
        // ...
        btnGuardar.setOnClickListener { saveOrUpdateCliente() }
        btnGenerarCredencial.setOnClickListener { showCredentialPreview() }
        // ...
    }

    private fun loadClienteData(id: Int) {
        val cliente = clienteDao.getClienteById(id)
        if (cliente != null) {
            loadedCliente = cliente
            tvHeader.text = "Editar Cliente: ${cliente.nombre} ${cliente.apellido}"
            btnGuardar.text = "Actualizar Cliente"
            btnGenerarCredencial.visibility = View.VISIBLE
            fillForm(cliente)

            val socio = clienteDao.getSocioByClienteId(cliente.id)
            if (socio != null) {
                etVencimientoCuota.setText(socio.fechaVencimientoCuota)
                cbCarnetEntregado.isChecked = socio.carnetEntregado
            }
        } else {
            Toast.makeText(this, "Error: Cliente no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fillForm(cliente: Cliente) {
        // Lógica de llenado de formulario omitida por brevedad
        etDni.setText(cliente.dni.toString())
        etNombre.setText(cliente.nombre)
        etApellido.setText(cliente.apellido)
        // ...
        if (cliente.asociarse) rbSocio.isChecked = true else rbNoSocio.isChecked = true
    }

    private fun saveOrUpdateCliente() {
        if (!validateFields()) return

        val nuevoClienteBase = createClienteFromForm()
        val socioData = if (rbSocio.isChecked) createSocioFromForm(0) else null
        val noSocioData = if (rbNoSocio.isChecked) createNoSocioFromForm(0) else null

        val success: Boolean
        if (clienteId == -1) {
            val nuevoId = clienteDao.insertCliente(cliente = nuevoClienteBase, socioData = socioData, noSocioData = noSocioData)
            success = nuevoId > 0
        } else {
            val rowsAffected = clienteDao.updateCliente(nuevoClienteBase.copy(id = clienteId))
            success = rowsAffected > 0
        }

        if (success) {
            Toast.makeText(this, "Cliente guardado/actualizado con éxito.", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Error al guardar/actualizar el cliente. Verifique el DNI.", Toast.LENGTH_LONG).show()
        }
    }

    // --- LÓGICA DE PREVISUALIZACIÓN Y COMPARTIR ---

    private fun showCredentialPreview() {
        val cliente = loadedCliente ?: return Toast.makeText(this, "Debe cargar el cliente.", Toast.LENGTH_SHORT).show()

        // 1. Rellenar la vista de la credencial con datos
        val credencialView = fillCredentialView(cliente, temporaryCredencialLayout)

        // 2. Inflar el layout del diálogo
        val dialogView = layoutInflater.inflate(R.layout.dialog_credencial_preview, null)
        val container = dialogView.findViewById<FrameLayout>(R.id.frame_credencial_container)
        val btnShare = dialogView.findViewById<Button>(R.id.btn_share_from_preview)

        // 3. Añadir la credencial rellenada al contenedor del diálogo
        (credencialView.parent as? ViewGroup)?.removeView(credencialView)
        container.addView(credencialView)

        // 4. Crear y mostrar el AlertDialog
        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(true).create()
        dialog.show()

        // 5. Configurar el Listener para Compartir
        btnShare.setOnClickListener {
            // Llamamos a la lógica de compartir, pasando el diálogo para cerrarlo
            checkStoragePermission(dialog)
        }
    }

    private fun fillCredentialView(cliente: Cliente, credencialView: View): View {
        val socioDetails = if (cliente.asociarse) clienteDao.getSocioByClienteId(cliente.id) else null

        credencialView.findViewById<TextView>(R.id.tv_credencial_name).text = "${cliente.nombre} ${cliente.apellido}"
        credencialView.findViewById<TextView>(R.id.tv_credencial_dni).text = "DNI: ${cliente.dni}"
        val tvStatusCred = credencialView.findViewById<TextView>(R.id.tv_credencial_status)

        if (cliente.asociarse && socioDetails != null) {
            val isMoroso = isCuotaVencida(socioDetails.fechaVencimientoCuota)
            tvStatusCred.text = if (isMoroso) "SOCIO MOROSO" else "SOCIO ACTIVO"
        } else {
            tvStatusCred.text = "NO SOCIO"
        }
        return credencialView
    }

    private fun checkStoragePermission(dialog: AlertDialog) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Si no hay permiso, lo solicitamos. La respuesta llama a onRequestPermissionsResult.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        } else {
            // Si hay permiso, compartimos
            shareCredencial(dialog)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso se concede, compartimos (el diálogo no está disponible aquí, usaríamos una bandera)
                Toast.makeText(this, "Permiso concedido. Vuelva a presionar Compartir.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso de almacenamiento denegado. No se puede compartir.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Captura el Bitmap del currentCredencialView y lo comparte.
     */
    private fun shareCredencial(dialog: AlertDialog?) {
        val cliente = loadedCliente ?: return

        // 1. Generar el Bitmap (Captura la vista que está en el contenedor del diálogo)
        val bitmap = createBitmapFromView(temporaryCredencialLayout, 600, 350)

        // 2. Guardar el Bitmap y obtener la URI
        val imageUri = saveBitmapToFile(bitmap, "${cliente.apellido}_${cliente.dni}_credencial.png")

        if (imageUri != null) {
            shareImage(imageUri, cliente.nombre)
            dialog?.dismiss() // Cerrar el diálogo después de iniciar el Intent de compartir
        } else {
            Toast.makeText(this, "Fallo al generar el archivo de credencial.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createBitmapFromView(view: View, width: Int, height: Int): Bitmap {
        // Forzamos la medición para la captura precisa
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight())

        val bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToFile(bitmap: Bitmap, filename: String): Uri? {
        val imagesFolder = File(filesDir, "credenciales")
        if (!imagesFolder.exists()) imagesFolder.mkdirs()

        val file = File(imagesFolder, filename)
        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()

            return FileProvider.getUriForFile(this, applicationContext.packageName + ".fileprovider", file)
        } catch (e: Exception) {
            Log.e("DetalleClienteActivity", "Error saving bitmap: ${e.message}", e)
            return null
        }
    }

    private fun shareImage(uri: Uri, clientName: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Credencial de Membresía Club")
            putExtra(Intent.EXTRA_TEXT, "Hola, ${clientName}. Aquí tienes tu credencial digital del club.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Compartir credencial vía"))
    }

    private fun isCuotaVencida(fechaVencimiento: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val vencimiento = dateFormat.parse(fechaVencimiento)
            vencimiento != null && vencimiento.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    private fun createClienteFromForm(): Cliente {
        val dni = etDni.text.toString().toInt()
        val fechaNacimiento = etFechaNacimiento.text.toString()
        val fechaAlta = getTodayDate()

        return Cliente(
            id = clienteId,
            dni = dni,
            nombre = etNombre.text.toString(),
            apellido = etApellido.text.toString(),
            fechaNacimiento = fechaNacimiento,
            direccion = etDireccion.text.toString(),
            telefono = etTelefono.text.toString(),
            aptoFisico = cbAptoFisico.isChecked,
            asociarse = rbSocio.isChecked,
            fechaAlta = fechaAlta
        )
    }

    private fun createSocioFromForm(idCliente: Int): Socio {
        val fechaInscripcion = getTodayDate()
        val vencimientoCuota = etVencimientoCuota.text.toString()

        return Socio(
            id = idCliente,
            fechaInscripcion = fechaInscripcion,
            fechaVencimientoCuota = vencimientoCuota,
            numeroCarnet = 0,
            carnetEntregado = cbCarnetEntregado.isChecked,
            fechaBaja = null
        )
    }

    private fun createNoSocioFromForm(idCliente: Int): NoSocio {
        return NoSocio(
            id = idCliente,
            fechaBaja = null
        )
    }

    private fun validateFields(): Boolean {
        if (etNombre.text.isNullOrEmpty() || etApellido.text.isNullOrEmpty() || etDni.text.isNullOrEmpty()) {
            Toast.makeText(this, "Nombre, Apellido y DNI son obligatorios.", Toast.LENGTH_SHORT).show()
            return false
        }
        val dni = etDni.text.toString().toIntOrNull()
        if (dni == null || dni <= 0) {
            Toast.makeText(this, "El DNI debe ser un número válido.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (rbSocio.isChecked && etVencimientoCuota.text.isNullOrEmpty()) {
            Toast.makeText(this, "La fecha de vencimiento de la cuota es obligatoria para socios.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showDatePickerDialog(editText: EditText) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, y, m, d ->
            val date = String.format(Locale.US, "%d-%02d-%02d", y, m + 1, d)
            editText.setText(date)
        }, year, month, day)

        dpd.show()
    }

    private fun getTodayDate(): String {
        val c = Calendar.getInstance()
        return String.format(Locale.US, "%d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}