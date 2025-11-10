package models

// Este es el "molde" que define qué es un Producto.
// Los nombres coinciden con tu SQLiteHelper.
data class Producto(
    val id: Int,
    val nombre: String,
    val marca: String,
    val precio: Double,
    val descripcion: String,
    val imagen: String, // String de rutas separadas por ","
    val vendedorId: Int
)