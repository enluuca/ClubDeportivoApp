package com.example.clubdeportivoapp.data.model

/**
 * Modelo de datos para la entidad Cuota.
 * Corresponde a los pagos recurrentes realizados por un Socio.
 */
data class Cuota(
    val id: Int = 0, // Clave primaria (AUTOINCREMENT)
    val idSocio: Int, // Clave foránea a Socio.id
    val fechaPago: String, // Formato YYYY-MM-DD
    val monto: Double, // Monto base de la cuota
    val medioPago: String,
    val cantidadCuotas: Int,
    val descuento: Double = 0.0,
    val montoTotal: Double,
    val fechaVencimiento: String, // Formato YYYY-MM-DD
    val comprobante: Int? = null // Número de comprobante
)