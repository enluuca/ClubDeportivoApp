package com.example.clubdeportivoapp.data.model

/**
 * Modelo de datos para la entidad Actividad del club.
 * Corresponde a la tabla 'Actividad' en la base de datos SQLite.
 */
data class Actividad(
    val id: Int = 0, // Clave primaria, auto-incrementable
    val nombre: String,
    val costo: Double // Usamos Double para el costo, que se mapea a REAL en SQLite
)
