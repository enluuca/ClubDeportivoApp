package com.example.clubdeportivoapp.data.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.clubdeportivoapp.data.db.DatabaseHelper
import com.example.clubdeportivoapp.data.model.Cuota
import com.example.clubdeportivoapp.data.model.RegistroActividad
// import java.util.Locale // Ya no es necesario si no se usa directamente aquí

/**
 * [Clase de Nivel Intermedio]
 * Data Access Object (DAO) para la gestión de Pagos (Cuotas y RegistroActividad).
 * * Responsable de: Implementar el CRUD para las dos tablas de pagos.
 */
class PagoDao(context: Context) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context)

    // Getter auxiliar para la base de datos de solo lectura
    private val dbReadable get() = dbHelper.readableDatabase
    // Getter auxiliar para la base de datos de escritura/modificación
    private val dbWritable get() = dbHelper.writableDatabase

    // ====================================================================
    // A. OPERACIONES CRUD PARA CUOTAS (Pagos de Socios)
    // ====================================================================

    /**
     * Inserta un nuevo registro de Cuota en la base de datos.
     * @return El ID de la nueva fila insertada, o -1 si hubo un error.
     */
    fun insertCuota(cuota: Cuota): Long {
        val values = ContentValues().apply {
            put(DatabaseHelper.CUOTA_COL_ID_SOCIO, cuota.idSocio)
            put(DatabaseHelper.CUOTA_COL_FECHA_PAGO, cuota.fechaPago)
            put(DatabaseHelper.CUOTA_COL_MONTO, cuota.monto)
            put(DatabaseHelper.CUOTA_COL_MEDIO_PAGO, cuota.medioPago)
            put(DatabaseHelper.CUOTA_COL_CANTIDAD_CUOTAS, cuota.cantidadCuotas)
            put(DatabaseHelper.CUOTA_COL_DESCUENTO, cuota.descuento)
            put(DatabaseHelper.CUOTA_COL_MONTO_TOTAL, cuota.montoTotal)
            put(DatabaseHelper.CUOTA_COL_FECHA_VENCIMIENTO, cuota.fechaVencimiento)
            put(DatabaseHelper.CUOTA_COL_COMPROBANTE, cuota.comprobante)
        }
        return dbWritable.insert(DatabaseHelper.TABLE_CUOTA, null, values)
    }

    /**
     * Obtiene el último registro de Cuota pagada por un Socio.
     * * Utilidad: Determinar la fecha de vencimiento de la próxima cuota.
     */
    fun getLastCuotaBySocioId(idSocio: Int): Cuota? {
        val cursor = dbReadable.query(
            DatabaseHelper.TABLE_CUOTA,
            null, // Selecciona todas las columnas
            "${DatabaseHelper.CUOTA_COL_ID_SOCIO} = ?",
            arrayOf(idSocio.toString()),
            null, null,
            "${DatabaseHelper.CUOTA_COL_FECHA_PAGO} DESC", // Ordenado por fecha descendente
            "1" // Limita a 1 resultado
        )

        val cuota: Cuota? = cursor.use {
            if (it.moveToFirst()) {
                cursorToCuota(it) // Usa función auxiliar para mapeo
            } else {
                null
            }
        }
        return cuota
    }

    /**
     * Convierte una fila de la base de datos (Cursor) en un objeto Cuota.
     */
    private fun cursorToCuota(cursor: Cursor): Cuota {
        return Cuota(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CUOTA_COL_ID)),
            idSocio = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CUOTA_COL_ID_SOCIO)),
            fechaPago = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUOTA_COL_FECHA_PAGO)),
            monto = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.CUOTA_COL_MONTO)),
            medioPago = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUOTA_COL_MEDIO_PAGO)),
            cantidadCuotas = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CUOTA_COL_CANTIDAD_CUOTAS)),
            descuento = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.CUOTA_COL_DESCUENTO)),
            montoTotal = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.CUOTA_COL_MONTO_TOTAL)),
            fechaVencimiento = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.CUOTA_COL_FECHA_VENCIMIENTO)),
            comprobante = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.CUOTA_COL_COMPROBANTE))
        )
    }


    // ====================================================================
    // B. OPERACIONES CRUD PARA REGISTRO DE ACTIVIDAD (Pagos de No Socios)
    // ====================================================================

    /**
     * Inserta un nuevo Registro de Actividad (pago de No Socio por actividad).
     * @return El ID de la nueva fila insertada, o -1 si hubo un error.
     */
    fun insertRegistroActividad(registro: RegistroActividad): Long {
        val values = ContentValues().apply {
            put(DatabaseHelper.REG_ACT_COL_ID_NO_SOCIO, registro.idCliente)
            put(DatabaseHelper.REG_ACT_COL_ID_ACTIVIDAD, registro.idActividad)
            put(DatabaseHelper.REG_ACT_COL_FECHA_PAGO, registro.fechaPago)
            put(DatabaseHelper.REG_ACT_COL_MONTO, registro.montoPagado)
            put(DatabaseHelper.REG_ACT_COL_MEDIO_PAGO, registro.medioPago)
            // Simplificación: usamos los campos mínimos
            put(DatabaseHelper.REG_ACT_COL_CANTIDAD_CUOTAS, 1)
            put(DatabaseHelper.REG_ACT_COL_MONTO_TOTAL, registro.montoPagado)
            // CUOTA_COL_DESCUENTO y CUOTA_COL_COMPROBANTE omitidos en el apply por simplificación
        }
        return dbWritable.insert(DatabaseHelper.TABLE_REGISTRO_ACTIVIDAD, null, values)
    }

    /**
     * Obtiene todos los registros de actividad para un Cliente No Socio.
     */
    fun getRegistrosByClienteId(idCliente: Int): List<RegistroActividad> {
        val registros = mutableListOf<RegistroActividad>()
        val cursor = dbReadable.query(
            DatabaseHelper.TABLE_REGISTRO_ACTIVIDAD,
            null,
            "${DatabaseHelper.REG_ACT_COL_ID_NO_SOCIO} = ?",
            arrayOf(idCliente.toString()),
            null, null,
            "${DatabaseHelper.REG_ACT_COL_FECHA_PAGO} DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                registros.add(cursorToRegistroActividad(it))
            }
        }
        return registros
    }

    /**
     * Convierte una fila de la base de datos (Cursor) en un objeto RegistroActividad.
     */
    private fun cursorToRegistroActividad(cursor: Cursor): RegistroActividad {
        return RegistroActividad(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_ID)),
            idCliente = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_ID_NO_SOCIO)),
            idActividad = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_ID_ACTIVIDAD)),
            fechaPago = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_FECHA_PAGO)),
            montoPagado = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_MONTO_TOTAL)),
            medioPago = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_MEDIO_PAGO)),
            // Nota: La fecha de expiración se toma de fechaPago por simplificación
            fechaExpiracion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_FECHA_PAGO))
        )
    }
}