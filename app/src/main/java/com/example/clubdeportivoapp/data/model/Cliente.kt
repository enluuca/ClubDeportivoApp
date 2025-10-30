package com.example.clubdeportivoapp.data.model

/**
 * Modelo de datos para la tabla 'Cliente'.
 * Contiene los datos personales básicos de cualquier persona registrada en el club.
 *
 * @property id ID del cliente. En SQLite, será autoincremental (Long)
 * @property dni Número de Documento de Identidad (UNIQUE).
 * @property nombre Nombre del cliente.
 * @property apellido Apellido del cliente.
 * @property fechaNacimiento Fecha de nacimiento (Usamos String para formato ISO-8601 YYYY-MM-DD).
 * @property direccion Dirección de residencia.
 * @property telefono Número de teléfono.
 * @property aptoFisico Indica si tiene el certificado físico válido (Boolean).
 * @property asociarse Indica si la persona desea asociarse (Boolean).
 * @property fechaAlta Fecha de alta en el sistema (String).
 */
data class Cliente(
    val id: Int = 0,
    val dni: Int, // CORREGIDO: El DNI es un número (INTEGER en DB)
    val nombre: String,
    val apellido: String, // CORREGIDO: El Apellido es un String
    val fechaNacimiento: String,
    val direccion: String,
    val telefono: String,
    val aptoFisico: Boolean,
    val asociarse: Boolean,
    val fechaAlta: String
)
