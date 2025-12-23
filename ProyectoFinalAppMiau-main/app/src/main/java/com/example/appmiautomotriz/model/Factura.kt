package com.example.appmiautomotriz.model

data class Factura(
    val idFactura: Int = 0,
    val codigoFactura: String,
    val idCliente: Int,
    val idOrden: Int,
    val fechaFactura: String,
    val montoTotal: Int,
    val estadoFactura: String // "Pagada", "Pendiente"
)
