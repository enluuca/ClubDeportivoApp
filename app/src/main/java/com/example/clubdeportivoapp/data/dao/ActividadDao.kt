package com.example.clubdeportivoapp.data.dao

import android.content.ContentValues
import android.content.Context
import com.example.clubdeportivoapp.data.db.DatabaseHelper
import com.example.clubdeportivoapp.data.model.Actividad

/**
 * Data Access Object (DAO) para la entidad Actividad.
 * Implementa las operaciones CRUD para la tabla 'Actividad'.
 */
class ActividadDao(context: Context) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context)

    // --- Métodos de Conversión ---

    /**
     * Convierte una fila de la base de datos (Cursor) en un objeto Actividad.
     */
    private fun cursorToActividad(cursor: android.database.Cursor): Actividad {
        return Actividad(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ACTIVIDAD_COL_ID)),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ACTIVIDAD_COL_NOMBRE)),
            costo = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.ACTIVIDAD_COL_COSTO))
        )
    }

    // --- Operaciones CRUD ---

    /**
     * Inserta una nueva Actividad en la base de datos.
     * @return El ID de la nueva fila insertada, o -1 si hubo un error.
     */
    fun insertActividad(actividad: Actividad): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.ACTIVIDAD_COL_NOMBRE, actividad.nombre)
            put(DatabaseHelper.ACTIVIDAD_COL_COSTO, actividad.costo)
        }
        return db.insert(DatabaseHelper.TABLE_ACTIVIDAD, null, values)
    }

    /**
     * Obtiene una Actividad por su ID.
     */
    fun getActividadById(id: Int): Actividad? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ACTIVIDAD,
            null,
            "${DatabaseHelper.ACTIVIDAD_COL_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        val actividad = if (cursor.moveToFirst()) {
            cursorToActividad(cursor)
        } else {
            null
        }
        cursor.close()
        return actividad
    }

    /**
     * Obtiene una lista de todas las Actividades.
     */
    fun getAllActividades(): List<Actividad> {
        val actividades = mutableListOf<Actividad>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ACTIVIDAD,
            null, null, null, null, null,
            "${DatabaseHelper.ACTIVIDAD_COL_NOMBRE} ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                actividades.add(cursorToActividad(it))
            }
        }
        return actividades
    }

    /**
     * Actualiza los datos de una Actividad existente.
     * @return El número de filas afectadas.
     */
    fun updateActividad(actividad: Actividad): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.ACTIVIDAD_COL_NOMBRE, actividad.nombre)
            put(DatabaseHelper.ACTIVIDAD_COL_COSTO, actividad.costo)
        }
        return db.update(
            DatabaseHelper.TABLE_ACTIVIDAD,
            values,
            "${DatabaseHelper.ACTIVIDAD_COL_ID} = ?",
            arrayOf(actividad.id.toString())
        )
    }

    /**
     * Elimina una Actividad por su ID.
     * @return El número de filas eliminadas.
     */
    fun deleteActividad(id: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseHelper.TABLE_ACTIVIDAD,
            "${DatabaseHelper.ACTIVIDAD_COL_ID} = ?",
            arrayOf(id.toString())
        )
    }
}