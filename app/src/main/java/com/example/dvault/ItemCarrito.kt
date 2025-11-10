package models

// Este objeto representa un item en el carrito,
// incluyendo los datos del vendedor
data class ItemCarrito(
    val carritoId: Int,
    val cantidad: Int,
    val productoId: Int,
    val nombreProducto: String,
    val marcaProducto: String,
    val precioProducto: Double,
    val imagenProducto: String,
    val vendedorId: Int,
    val nombreVendedor: String
)