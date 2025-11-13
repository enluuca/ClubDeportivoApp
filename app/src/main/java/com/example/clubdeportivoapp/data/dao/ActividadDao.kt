package com.example.clubdeportivoapp.data.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.clubdeportivoapp.data.db.DatabaseHelper
import com.example.clubdeportivoapp.data.model.Actividad

/**
 * [Clase de Nivel Intermedio]
 * Data Access Object (DAO) para la entidad Actividad.
 * * Responsable de: Implementar las operaciones CRUD para la tabla 'Actividad'.
 */
class ActividadDao(context: Context) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context)

    // --- Métodos de Conversión ---

    /**
     * Convierte una fila de la base de datos (Cursor) en un objeto Actividad.
     */
    private fun cursorToActividad(cursor: Cursor): Actividad {
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
        // Retorna el ID de la fila insertada.
        return db.insert(DatabaseHelper.TABLE_ACTIVIDAD, null, values)
    }

    /**
     * Obtiene una Actividad por su ID.
     */
    fun getActividadById(id: Int): Actividad? {
        val db = dbHelper.readableDatabase
        // Uso de 'use' para asegurar que el Cursor se cierre automáticamente (Mejor práctica).
        val cursor = db.query(
            DatabaseHelper.TABLE_ACTIVIDAD,
            null,
            "${DatabaseHelper.ACTIVIDAD_COL_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        // Refactorización: Uso de 'use' para cerrar el cursor.
        return cursor.use {
            if (it.moveToFirst()) {
                cursorToActividad(it)
            } else {
                null
            }
        }
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
            "${DatabaseHelper.ACTIVIDAD_COL_NOMBRE} ASC" // Ordena por nombre
        )

        // Uso de 'use' para garantizar que el Cursor se cierre correctamente
        cursor.use {
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