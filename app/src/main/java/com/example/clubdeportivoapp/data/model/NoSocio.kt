package com.example.clubdeportivoapp.data.model

/**
 * Entidad complementaria de Cliente para miembros que son NoSocios.
 * Corresponde a la tabla NoSocio (FK id = Cliente.id).
 */
data class NoSocio(
    val id: Int = 0, // Hereda el ID del Cliente
    val fechaBaja: String? = null // Puede ser nulo
)