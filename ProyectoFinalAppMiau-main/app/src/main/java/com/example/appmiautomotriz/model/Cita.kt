package com.example.appmiautomotriz.model

data class Cita(
    val idCita: Int = 0,
    val idCliente: Int,
    val patente: String,
    val fechaCita: String,
    val horaCita: String,
    val tipoCita: String, // "Diagnóstico", "Mantenimiento"
    val estadoCita: String // "Confirmada", "Pendiente", "Cancelada"
)
