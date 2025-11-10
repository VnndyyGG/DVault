package models

//guardar los datos de la consulta JOIN del carrito
data class ItemCarrito(
    val carritoId: Int,
    val cantidad: Int,
    val productoId: Int,
    val nombreProducto: String,
    val marcaProducto: String,
    val precioProducto: Double,
    val imagenProducto: String,
    val vendedorId: Int,
    val nombreVendedor: String,
    val paisVendedor: String //
)