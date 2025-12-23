package com.example.appmiautomotriz.model

// para guardar los datos de una orden.
/**
 * Modelo de datos que representa una Orden de Trabajo.
 * Coincide con la estructura de la tabla en la base de datos y la respuesta JSON de la API.
 */
data class OrdenDeTrabajo(
    val numeroOrdenDeTrabajo: Int,
    val fechaOrdenDeTrabajo: String,
    val estado: String,
    val observaciones: String?, // '?' significa que puede ser nulo
    val patente: String,
    val idSeguro: Int? = null,
    val idCliente: Int? = null,
    val firestoreId: String? = null, // ID del documento en Firestore (String)
    val nombreCliente: String? = null // Nombre del cliente (transitorio, no guardado en API ordenes directo)
)

