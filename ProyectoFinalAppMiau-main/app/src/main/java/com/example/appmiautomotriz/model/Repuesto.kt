package com.example.appmiautomotriz.model

data class Repuesto(
    val idRepuesto: Int = 0,
    val nombreRepuesto: String,
    val descripcionRepuesto: String = "",
    val stock: Int = 0,
    val precio: Int = 0
)
