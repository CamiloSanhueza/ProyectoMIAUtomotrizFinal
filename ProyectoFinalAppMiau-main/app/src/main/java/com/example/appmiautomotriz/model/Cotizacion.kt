package com.example.appmiautomotriz.model

data class Cotizacion(
    val idCotizacion: Int = 0,
    val codigoCotizacion: String,
    val idCliente: Int,
    val patente: String,
    val fechaCotizacion: String,
    val montoCotizacion: Int,
    val estadoCotizacion: String // "Aprobada", "Rechazada", "Pendiente"
)
