package com.example.clubdeportivoapp.data.model

/**
 * Modelo de datos para la entidad RegistroActividad.
 * Corresponde a un pago único realizado por un Cliente No Socio para una Actividad.
 */
data class RegistroActividad(
    val id: Int = 0, // Clave primaria (AUTOINCREMENT)
    val idCliente: Int, // Clave foránea a Cliente.id (Cliente NO Socio)
    val idActividad: Int, // Clave foránea a Actividad.id
    val fechaPago: String, // Formato YYYY-MM-DD
    val montoPagado: Double, // Costo de la actividad al momento del pago
    val medioPago: String,
    val fechaExpiracion: String // Formato YYYY-MM-DD (Puede ser la fecha del día si es pago único)
)