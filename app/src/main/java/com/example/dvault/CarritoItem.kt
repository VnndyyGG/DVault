package com.example.dvault // Asegúrate que este sea tu paquete

/**
 * Modelo de datos (Data Class) para un item en el carrito.
 * Es un "molde" que dice qué datos tiene cada item.
 * 'var quantity' puede cambiar (es variable), por eso es 'var'.
 */
data class CarritoItem(
    val id: String,
    val titulo: String,
    val precioUnitario: Double,
    var cantidad: Int,
    val imageUrl: String
)