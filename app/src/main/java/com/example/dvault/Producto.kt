package models

data class Producto(
    val id: Int,
    val nombre: String,
    val marca: String,
    val precio: Double,
    val descripcion: String,
    val imagen: String,
    val vendedorId: Int
)