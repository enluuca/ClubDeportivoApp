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
import java.text.SimpleDateFormat // Importación requerida
import java.util.Date // Importación requerida
import java.util.Locale // Importación requerida

/**
 * Clase de Acceso a Datos (DAO) para Cliente, Socio y NoSocio.
 * CONTIENE TODAS LAS OPERACIONES CRUD Y LÓGICA DE PERSISTENCIA ATÓMICA.
 */
class ClienteDao(context: Context) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context)

    // Función auxiliar: Devuelve la base de datos de solo lectura
    private val db: SQLiteDatabase
        get() = dbHelper.readableDatabase

    // --- Conversión de Cursor a Modelo ---

    private fun cursorToCliente(cursor: Cursor): Cliente {
        return Cliente(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_ID)),
            dni = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_DNI)),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_NOMBRE)),
            apellido = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_APELLIDO)),
            fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_FECHA_NACIMIENTO)),
            direccion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_DIRECCION)),
            telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_TELEFONO)),
            aptoFisico = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_APTO_FISICO)) > 0,
            asociarse = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_ASOCIARSE)) > 0,
            fechaAlta = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CLIENTE_COL_FECHA_ALTA))
        )
    }

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

    // --- Métodos de Consulta (Ya estaban correctos) ---

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
    // --- MÉTODOS DE MODIFICACIÓN (AÑADIDOS/CORREGIDOS) ---
    // ====================================================================

    /**
     * ✅ FUNCIÓN COMPLETA: Inserta Cliente y sus datos de membresía.
     * Resuelve: Unresolved reference 'insertCliente' en DetalleClienteActivity.kt
     */
    fun insertCliente(cliente: Cliente, socioData: Socio?, noSocioData: NoSocio?): Long {
        val writableDb = dbHelper.writableDatabase
        writableDb.beginTransaction()

        try {
            // 1. Insertar en Cliente
            val values = ContentValues().apply {
                put(DatabaseHelper.CLIENTE_COL_DNI, cliente.dni)
                put(DatabaseHelper.CLIENTE_COL_NOMBRE, cliente.nombre)
                put(DatabaseHelper.CLIENTE_COL_APELLIDO, cliente.apellido)
                put(DatabaseHelper.CLIENTE_COL_FECHA_NACIMIENTO, cliente.fechaNacimiento)
                put(DatabaseHelper.CLIENTE_COL_DIRECCION, cliente.direccion)
                put(DatabaseHelper.CLIENTE_COL_TELEFONO, cliente.telefono)
                put(DatabaseHelper.CLIENTE_COL_APTO_FISICO, if (cliente.aptoFisico) 1 else 0)
                put(DatabaseHelper.CLIENTE_COL_ASOCIARSE, if (cliente.asociarse) 1 else 0)
                put(DatabaseHelper.CLIENTE_COL_FECHA_ALTA, cliente.fechaAlta)
            }

            val clienteId = writableDb.insert(DatabaseHelper.TABLE_CLIENTE, null, values)

            if (clienteId > 0) {
                // 2. Insertar datos de Membresía usando el nuevo ID
                if (socioData != null) {
                    insertSocio(socioData.copy(id = clienteId.toInt()), writableDb)
                } else if (noSocioData != null) {
                    insertNoSocio(noSocioData.copy(id = clienteId.toInt()), writableDb)
                }
                writableDb.setTransactionSuccessful()
                return clienteId
            }
            return -1L
        } catch (e: Exception) {
            Log.e("ClienteDao", "Error insertando cliente y membresía: ${e.message}")
            return -1L
        } finally {
            writableDb.endTransaction()
        }
    }

    /**
     * ✅ FUNCIÓN COMPLETA: Actualiza Cliente.
     * Resuelve: Unresolved reference 'updateCliente' en DetalleClienteActivity.kt
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
     * ✅ FUNCIÓN COMPLETA: Actualiza la fecha de vencimiento de la cuota del socio.
     * Resuelve: Unresolved reference 'updateSocioVencimiento' en PagosActivity.kt
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

    // --- Funciones auxiliares de inserción (necesarias para insertCliente) ---

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

    fun getClientesMorosos(): List<Cliente> {
        val morosos = mutableListOf<Cliente>()
        val db = dbHelper.readableDatabase

        // 1. Obtener la fecha actual en formato YYYY-MM-DD
        // Usamos Locale.US para asegurar el formato estándar YYYY-MM-DD que es comparable en SQLite
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        // 2. Consulta SQL con JOIN:
        // Buscamos Clientes (T1) que tienen un registro en Socio (T2)
        // Y donde la fecha de vencimiento (FECHA_VENCIMIENTO_CUOTA) es anterior a hoy (< ?)
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
                // Reutilizamos la función de conversión a Cliente
                morosos.add(cursorToCliente(this))
            }
        }
        cursor.close()
        // No cerramos 'db' aquí ya que es 'readableDatabase' y se gestiona automáticamente
        return morosos
    }
}