package com.example.appmiautomotriz.api

/**
 * Constantes de conexión a la API REST.
 */
object ApiConstants {
    // Configuración de IP del servidor
    private const val BASE_URL = "http://192.168.1.36/proyecto/controllers/API"

    const val URL_LOGIN = "$BASE_URL/login.php"
    const val URL_ORDENES = "$BASE_URL/ordenes_api.php"
    const val URL_CREAR_ORDEN = "$BASE_URL/crear_orden.php"
    const val URL_ACTUALIZAR_ORDEN = "$BASE_URL/actualizar_orden.php"
    const val URL_APROBAR_COTIZACION = "$BASE_URL/aprobar_cotizacion.php"
}