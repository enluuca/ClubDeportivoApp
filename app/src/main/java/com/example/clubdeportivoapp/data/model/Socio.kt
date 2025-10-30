package com.example.clubdeportivoapp.data.model

/**
 * Entidad complementaria de Cliente para miembros que son Socios.
 * Corresponde a la tabla Socio (FK id = Cliente.id).
 */
data class Socio(
    val id: Int = 0, // Hereda el ID del Cliente (clave foránea y primaria a la vez)
    val fechaInscripcion: String, // Usar formato YYYY-MM-DD
    val fechaVencimientoCuota: String, // Usar formato YYYY-MM-DD
    val numeroCarnet: Int,
    val carnetEntregado: Boolean, // true o false
    val fechaBaja: String? = null // Puede ser nulo si el socio está activo
)
