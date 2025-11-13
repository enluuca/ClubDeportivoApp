package com.example.clubdeportivoapp.data.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.clubdeportivoapp.data.db.DatabaseHelper
import com.example.clubdeportivoapp.data.model.Cliente
import com.example.clubdeportivoapp.data.model.NoSocio
import com.example.clubdeportivoapp.data.model.Socio
import android.util.Log // Asegúrate de tener esta importación para Log
import java.text.SimpleDateFormat // Importación requerida para manejo de fechas
import java.util.Date // Importación requerida
import java.util.Locale // Importación requerida

/**
 * [Clase de Nivel Intermedio/Avanzado]
 * Clase de Acceso a Datos (DAO) para Cliente, Socio y NoSocio.
 * * Responsable de: Centralizar la lógica de persistencia y las consultas complejas.
 */
class ClienteDao(context: Context) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context)

    // Función auxiliar: Devuelve la base de datos de solo lectura (ReadableDatabase)
    private val db: SQLiteDatabase
        get() = dbHelper.readableDatabase

    // --- Conversión de Cursor a Modelo ---

    /**
     * Convierte una fila del Cursor en un objeto Cliente.
     * Asegura que todos los 10 campos se mapeen correctamente.
     */
    private fun cursorToCliente(cursor: Cursor): Cliente {
        return Cliente(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_ID)),
            dni = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_DNI)),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_NOMBRE)),
            apellido = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_APELLIDO)),
            fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_FECHA_NACIMIENTO)),
            direccion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_DIRECCION)),
            telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_TELEFONO)),
            // Convierte INTEGER (1/0) a Boolean
            aptoFisico = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_APTO_FISICO)) > 0,
            asociarse = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_ASOCIARSE)) > 0,
            fechaAlta = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_FECHA_ALTA))
        )
    }

    /**
     * Convierte una fila del Cursor en un objeto Socio.
     */
    private fun cursorToSocio(cursor: Cursor): Socio {
        return Socio(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SOCIO_COL_CLIENTE_ID)),
            fechaInscripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SOCIO_COL_FECHA_INSCRIPCION)),
            fechaVencimientoCuota = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SOCIO_COL_FECHA_VENCIMIENTO_CUOTA)),
            numeroCarnet = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SOCIO_COL_NUMERO_CARNET)),
            carnetEntregado = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.SOCIO_COL_CARNET_ENTREGADO)) > 0,
            fechaBaja = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.SOCIO_COL_FECHA_BAJA))
        )
    }

    // --- Métodos de Consulta ---

    /** Obtiene todos los clientes ordenados por apellido. */
    fun getAllClientes(): List<Cliente> {
        val clientes = mutableListOf<Cliente>()
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_CLIENTE} ORDER BY ${DatabaseHelper.CLIENTE_COL_APELLIDO} ASC", null)

        with(cursor) {
            while (moveToNext()) {
                clientes.add(cursorToCliente(this))
            }
        }
        cursor.close()
        return clientes
    }

    /** Obtiene un cliente por su ID. */
    fun getClienteById(id: Int): Cliente? {
        val cursor = db.query(
            DatabaseHelper.TABLE_CLIENTE,
            null,
            "${DatabaseHelper.CLIENTE_COL_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        val cliente = if (cursor.moveToFirst()) {
            cursorToCliente(cursor)
        } else {
            null
        }
        cursor.close()
        return cliente
    }

    /** Obtiene un cliente por su DNI. */
    fun getClienteByDni(dni: Int): Cliente? {
        val cursor = db.query(
            DatabaseHelper.TABLE_CLIENTE,
            null,
            "${DatabaseHelper.CLIENTE_COL_DNI} = ?",
            arrayOf(dni.toString()),
            null, null, null
        )

        val cliente = if (cursor.moveToFirst()) {
            cursorToCliente(cursor)
        } else {
            null
        }
        cursor.close()
        return cliente
    }

    /** Obtiene los detalles de membresía de un Socio por su ID de Cliente. */
    fun getSocioByClienteId(id: Int): Socio? {
        val cursor = db.query(
            DatabaseHelper.TABLE_SOCIO,
            null,
            "${DatabaseHelper.SOCIO_COL_CLIENTE_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        val socio = if (cursor.moveToFirst()) {
            cursorToSocio(cursor)
        } else {
            null
        }
        cursor.close()
        return socio
    }

    // ====================================================================
    // --- MÉTODOS DE MODIFICACIÓN (TRANSACCIONALES) ---
    // ====================================================================

    /**
     * Inserta un nuevo Cliente y, si corresponde, sus datos de membresía (Socio/NoSocio)
     * en una transacción atómica.
     * @return El ID de la nueva fila de Cliente, o -1 si falla la transacción.
     */
    fun insertCliente(cliente: Cliente, socioData: Socio?, noSocioData: NoSocio?): Long {
        val writableDb = dbHelper.writableDatabase
        writableDb.beginTransaction() // Inicia la transacción

        try {
            // 1. Insertar en Cliente
            val values = ContentValues().apply {
                put(DatabaseHelper.CLIENTE_COL_DNI, cliente.dni)
                put(DatabaseHelper.CLIENTE_COL_NOMBRE, cliente.nombre)
                put(DatabaseHelper.CLIENTE_COL_APELLIDO, cliente.apellido)
                // [Resto de los campos]
                put(DatabaseHelper.CLIENTE_COL_FECHA_NACIMIENTO, cliente.fechaNacimiento)
                put(DatabaseHelper.CLIENTE_COL_DIRECCION, cliente.direccion)
                put(DatabaseHelper.CLIENTE_COL_TELEFONO, cliente.telefono)
                put(DatabaseHelper.CLIENTE_COL_APTO_FISICO, if (cliente.aptoFisico) 1 else 0)
                put(DatabaseHelper.CLIENTE_COL_ASOCIARSE, if (cliente.asociarse) 1 else 0)
                put(DatabaseHelper.CLIENTE_COL_FECHA_ALTA, cliente.fechaAlta)
            }

            val clienteId = writableDb.insert(DatabaseHelper.TABLE_CLIENTE, null, values)

            if (clienteId > 0) {
                // 2. Insertar datos de Membresía usando el ID de Cliente
                if (socioData != null) {
                    insertSocio(socioData.copy(id = clienteId.toInt()), writableDb)
                } else if (noSocioData != null) {
                    insertNoSocio(noSocioData.copy(id = clienteId.toInt()), writableDb)
                }
                writableDb.setTransactionSuccessful() // Confirma la transacción
                return clienteId
            }
            return -1L
        } catch (e: Exception) {
            Log.e("ClienteDao", "Error insertando cliente y membresía: ${e.message}")
            return -1L // Retorna error
        } finally {
            writableDb.endTransaction() // Finaliza la transacción
        }
    }

    /**
     * Actualiza los datos de la tabla Cliente (solo los campos base).
     */
    fun updateCliente(cliente: Cliente): Int {
        val writableDb = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.CLIENTE_COL_DNI, cliente.dni)
            put(DatabaseHelper.CLIENTE_COL_NOMBRE, cliente.nombre)
            put(DatabaseHelper.CLIENTE_COL_APELLIDO, cliente.apellido)
            put(DatabaseHelper.CLIENTE_COL_FECHA_NACIMIENTO, cliente.fechaNacimiento)
            put(DatabaseHelper.CLIENTE_COL_DIRECCION, cliente.direccion)
            put(DatabaseHelper.CLIENTE_COL_TELEFONO, cliente.telefono)
            put(DatabaseHelper.CLIENTE_COL_APTO_FISICO, if (cliente.aptoFisico) 1 else 0)
            put(DatabaseHelper.CLIENTE_COL_ASOCIARSE, if (cliente.asociarse) 1 else 0)
        }
        val rowsAffected = writableDb.update(
            DatabaseHelper.TABLE_CLIENTE,
            values,
            "${DatabaseHelper.CLIENTE_COL_ID} = ?",
            arrayOf(cliente.id.toString())
        )
        writableDb.close()
        return rowsAffected
    }

    /**
     * Actualiza solo la fecha de vencimiento de la cuota en la tabla Socio.
     * Utilizada después de registrar un pago de cuota.
     */
    fun updateSocioVencimiento(idSocio: Int, newDate: String): Int {
        val writableDb = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.SOCIO_COL_FECHA_VENCIMIENTO_CUOTA, newDate)
        }

        val rowsAffected = writableDb.update(
            DatabaseHelper.TABLE_SOCIO,
            values,
            "${DatabaseHelper.SOCIO_COL_CLIENTE_ID} = ?",
            arrayOf(idSocio.toString())
        )
        writableDb.close()
        return rowsAffected
    }

    // --- Funciones auxiliares de inserción ---

    private fun insertSocio(socio: Socio, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(DatabaseHelper.SOCIO_COL_CLIENTE_ID, socio.id)
            put(DatabaseHelper.SOCIO_COL_FECHA_INSCRIPCION, socio.fechaInscripcion)
            put(DatabaseHelper.SOCIO_COL_FECHA_VENCIMIENTO_CUOTA, socio.fechaVencimientoCuota)
            put(DatabaseHelper.SOCIO_COL_NUMERO_CARNET, socio.numeroCarnet)
            put(DatabaseHelper.SOCIO_COL_CARNET_ENTREGADO, if (socio.carnetEntregado) 1 else 0)
        }
        db.insert(DatabaseHelper.TABLE_SOCIO, null, values)
    }

    private fun insertNoSocio(noSocio: NoSocio, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(DatabaseHelper.NO_SOCIO_COL_CLIENTE_ID, noSocio.id)
        }
        db.insert(DatabaseHelper.TABLE_NO_SOCIO, null, values)
    }

    // --- Lógica de Reportes ---

    /**
     * Obtiene una lista de Clientes que son Socios y cuya cuota está vencida (Morosos).
     * Utiliza una consulta SQL con JOIN para eficiencia.
     */
    fun getClientesMorosos(): List<Cliente> {
        val morosos = mutableListOf<Cliente>()
        val db = dbHelper.readableDatabase

        // Obtener la fecha actual en formato YYYY-MM-DD para la comparación
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        // Consulta SQL con JOIN: Clientes (T1) + Socio (T2) donde la fecha de vencimiento es < a hoy.
        val query = """
            SELECT T1.* FROM ${DatabaseHelper.TABLE_CLIENTE} T1 
            INNER JOIN ${DatabaseHelper.TABLE_SOCIO} T2 
            ON T1.${DatabaseHelper.CLIENTE_COL_ID} = T2.${DatabaseHelper.SOCIO_COL_CLIENTE_ID}
            WHERE T2.${DatabaseHelper.SOCIO_COL_FECHA_VENCIMIENTO_CUOTA} < ? 
            AND T2.${DatabaseHelper.SOCIO_COL_FECHA_BAJA} IS NULL
            ORDER BY T1.${DatabaseHelper.CLIENTE_COL_APELLIDO} ASC
        """

        val cursor = db.rawQuery(query, arrayOf(today))

        with(cursor) {
            while (moveToNext()) {
                morosos.add(cursorToCliente(this))
            }
        }
        cursor.close()
        // No cerramos 'db' aquí ya que es de solo lectura y el sistema lo gestiona.
        return morosos
    }
}