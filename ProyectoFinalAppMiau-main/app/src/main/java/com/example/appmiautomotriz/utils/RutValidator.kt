package com.example.appmiautomotriz.utils

object RutValidator {

    fun validar(rut: String): Boolean {
        var rutLimpio = rut.replace(".", "").replace("-", "")
        if (rutLimpio.length < 2) return false

        val cuerpo = rutLimpio.substring(0, rutLimpio.length - 1)
        val dv = rutLimpio.substring(rutLimpio.length - 1).uppercase()

        if (!cuerpo.all { it.isDigit() }) return false

        var suma = 0
        var multiplo = 2

        for (c in cuerpo.reversed()) {
            suma += c.digitToInt() * multiplo
            multiplo++
            if (multiplo > 7) multiplo = 2
        }

        val resto = 11 - (suma % 11)
        val dvCalculado = when (resto) {
            11 -> "0"
            10 -> "K"
            else -> resto.toString()
        }

        return dv == dvCalculado
    }

    fun formatear(rut: String): String {
        var rutLimpio = rut.replace(".", "").replace("-", "")
        if (rutLimpio.length < 2) return rut

        val cuerpo = rutLimpio.substring(0, rutLimpio.length - 1)
        val dv = rutLimpio.substring(rutLimpio.length - 1).uppercase()

        return "$cuerpo-$dv"
    }
}
