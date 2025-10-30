package com.example.clubdeportivoapp.data.dao

import android.content.ContentValues
import android.content.Context
import com.example.clubdeportivoapp.data.db.DatabaseHelper
import com.example.clubdeportivoapp.data.model.Cuota
import com.example.clubdeportivoapp.data.model.RegistroActividad
import java.util.Locale

/**
 * Data Access Object (DAO) para la gestión de Pagos (Cuotas y RegistroActividad).
 */
class PagoDao(context: Context) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context)

    // ====================================================================
    // A. OPERACIONES CRUD PARA CUOTAS (Pagos de Socios)
    // ====================================================================

    /**
     * Inserta un nuevo registro de Cuota en la base de datos.
     * @return El ID de la nueva fila insertada, o -1 si hubo un error.
     */
    fun insertCuota(cuota: Cuota): Long {
        val db = dbHelper.writableDatabase
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
        return db.insert(DatabaseHelper.TABLE_CUOTA, null, values)
    }

    /**
     * Obtiene el último registro de Cuota pagada por un Socio, ordenado por fecha de pago.
     * Es útil para saber cuándo vence la próxima cuota.
     */
    fun getLastCuotaBySocioId(idSocio: Int): Cuota? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_CUOTA,
            null,
            "${DatabaseHelper.CUOTA_COL_ID_SOCIO} = ?",
            arrayOf(idSocio.toString()),
            null, null,
            "${DatabaseHelper.CUOTA_COL_FECHA_PAGO} DESC", // Ordenado por fecha descendente
            "1" // Limita a 1 resultado
        )

        val cuota: Cuota? = if (cursor.moveToFirst()) {
            Cuota(
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
        } else {
            null
        }
        cursor.close()
        return cuota
    }

    // ====================================================================
    // B. OPERACIONES CRUD PARA REGISTRO DE ACTIVIDAD (Pagos de No Socios)
    // ====================================================================

    /**
     * Inserta un nuevo Registro de Actividad (pago de No Socio por actividad).
     * @return El ID de la nueva fila insertada, o -1 si hubo un error.
     */
    fun insertRegistroActividad(registro: RegistroActividad): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.REG_ACT_COL_ID_NO_SOCIO, registro.idCliente)
            put(DatabaseHelper.REG_ACT_COL_ID_ACTIVIDAD, registro.idActividad)
            put(DatabaseHelper.REG_ACT_COL_FECHA_PAGO, registro.fechaPago)
            put(DatabaseHelper.REG_ACT_COL_MONTO, registro.montoPagado)
            put(DatabaseHelper.REG_ACT_COL_MEDIO_PAGO, registro.medioPago)
            // Nota: Aquí se asume que la cantidadCuotas, descuento, montoTotal y comprobante
            // se simplifican o calculan en la Activity, usando solo los campos necesarios del modelo
            put(DatabaseHelper.REG_ACT_COL_CANTIDAD_CUOTAS, 1) // Asumimos pago único por ahora
            put(DatabaseHelper.REG_ACT_COL_MONTO_TOTAL, registro.montoPagado)
        }
        return db.insert(DatabaseHelper.TABLE_REGISTRO_ACTIVIDAD, null, values)
    }

    /**
     * Obtiene todos los registros de actividad para un Cliente No Socio.
     */
    fun getRegistrosByClienteId(idCliente: Int): List<RegistroActividad> {
        val registros = mutableListOf<RegistroActividad>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_REGISTRO_ACTIVIDAD,
            null,
            "${DatabaseHelper.REG_ACT_COL_ID_NO_SOCIO} = ?",
            arrayOf(idCliente.toString()),
            null, null,
            "${DatabaseHelper.REG_ACT_COL_FECHA_PAGO} DESC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                registros.add(
                    RegistroActividad(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_ID)),
                        idCliente = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_ID_NO_SOCIO)),
                        idActividad = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_ID_ACTIVIDAD)),
                        fechaPago = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_FECHA_PAGO)),
                        montoPagado = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_MONTO_TOTAL)),
                        medioPago = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_MEDIO_PAGO)),
                        // Los siguientes campos no están en el modelo simplificado,
                        // pero se devuelven por compatibilidad con la estructura de la DB:
                        fechaExpiracion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.REG_ACT_COL_FECHA_PAGO))
                    )
                )
            }
        }
        return registros
    }
}