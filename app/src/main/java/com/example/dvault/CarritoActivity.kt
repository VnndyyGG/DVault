package com.example.dvault

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import models.ItemCarrito // <-- Asegúrate de tener este 'data class'
import models.SQLiteHelper
import java.io.File
import java.util.Locale

class CarritoActivity : AppCompatActivity() {

    private lateinit var ayudanteBD: SQLiteHelper
    private var idUsuario: Int = -1
    private var paisUsuario: String = "Chile"

    // Vistas del XML
    private lateinit var tvCantidadArticulos: TextView
    private lateinit var containerProductos: LinearLayout
    private lateinit var tvSubtotal: TextView
    private lateinit var tvEnvio: TextView
    private lateinit var tvImpuestos: TextView
    private lateinit var tvTotalEstimado: TextView
    private lateinit var tvEnvioInfo: TextView
    private lateinit var btnCerrar: ImageButton
    private lateinit var btnEditar: TextView
    private lateinit var btnSiguiente: Button

    // Costos
    private val PAIS_LOCAL = "Chile"
    private val COSTO_ENVIO_LOCAL = 5000.0
    private val COSTO_ENVIO_INTERNACIONAL = 25.0
    private val TASA_IMPUESTOS = 0.19

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        ayudanteBD = SQLiteHelper(this)

        val sharedPref = getSharedPreferences("DVaultPrefs", MODE_PRIVATE)
        idUsuario = sharedPref.getInt("USUARIO_ID", -1)
        paisUsuario = sharedPref.getString("USUARIO_PAIS", PAIS_LOCAL) ?: PAIS_LOCAL

        if (idUsuario == -1) {
            Toast.makeText(this, "Error de sesión de usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        conectarVistas()
        configurarBotones()
        cargarDatosDelCarrito()
    }

    private fun conectarVistas() {
        tvCantidadArticulos = findViewById(R.id.tvCantidadArticulos)
        containerProductos = findViewById(R.id.containerProductos)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvEnvio = findViewById(R.id.tvEnvio)
        tvImpuestos = findViewById(R.id.tvImpuestos)
        tvTotalEstimado = findViewById(R.id.tvTotalEstimado)
        btnCerrar = findViewById(R.id.btnCerrar)
        btnEditar = findViewById(R.id.btnEditar)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        tvEnvioInfo = findViewById(R.id.tvEnvioInfo)
    }

    private fun configurarBotones() {
        btnCerrar.setOnClickListener { finish() }
        btnEditar.setOnClickListener { Toast.makeText(this, "Modo edición", Toast.LENGTH_SHORT).show() }
        btnSiguiente.setOnClickListener { Toast.makeText(this, "Iniciando pago...", Toast.LENGTH_SHORT).show() }
    }

    private fun cargarDatosDelCarrito() {
        val itemsDelCarrito = mutableListOf<ItemCarrito>()
        containerProductos.removeAllViews()

        val cursor = ayudanteBD.obtenerProductosCarrito(idUsuario)
        var subtotal = 0.0
        var cantidadTotalItems = 0
        var paisVendedor = PAIS_LOCAL

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val item = ItemCarrito(
                    carritoId = cursor.getInt(cursor.getColumnIndexOrThrow("carrito_id")),
                    cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad")),
                    productoId = cursor.getInt(cursor.getColumnIndexOrThrow("producto_id")),
                    nombreProducto = cursor.getString(cursor.getColumnIndexOrThrow("producto_nombre")),
                    marcaProducto = cursor.getString(cursor.getColumnIndexOrThrow("producto_marca")),
                    precioProducto = cursor.getDouble(cursor.getColumnIndexOrThrow("producto_precio")),
                    imagenProducto = cursor.getString(cursor.getColumnIndexOrThrow("producto_imagen")),
                    vendedorId = cursor.getInt(cursor.getColumnIndexOrThrow("vendedor_id")),
                    nombreVendedor = cursor.getString(cursor.getColumnIndexOrThrow("nombre_vendedor")),
                    paisVendedor = cursor.getString(cursor.getColumnIndexOrThrow("pais_vendedor"))
                )
                itemsDelCarrito.add(item)
                subtotal += (item.precioProducto * item.cantidad)
                cantidadTotalItems += item.cantidad
                paisVendedor = item.paisVendedor
            } while (cursor.moveToNext())
            cursor.close()
        }
        ayudanteBD.close()

        if (itemsDelCarrito.isEmpty()) {
            tvCantidadArticulos.text = "0 artículos"
            actualizarResumenCostos(0.0, PAIS_LOCAL)
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            return
        }

        // Agrupar productos por vendedor
        val productosPorVendedor = itemsDelCarrito.groupBy { it.nombreVendedor }

        // Mostrar productos agrupados
        for ((vendedor, productos) in productosPorVendedor) {
            agregarSeccionVendedor(vendedor, productos)
        }

        tvCantidadArticulos.text = if (cantidadTotalItems == 1) "$cantidadTotalItems artículo" else "$cantidadTotalItems artículos"
        actualizarResumenCostos(subtotal, paisVendedor)
    }

    // Esta es tu función que tenía el error
    private fun agregarSeccionVendedor(vendedor: String, productos: List<ItemCarrito>) {
        val inflater = LayoutInflater.from(this)

        // Agregar card del vendedor (AHORA SÍ ENCUENTRA EL LAYOUT)
        val cardVendedor = inflater.inflate(R.layout.item_vendedor_carrito, containerProductos, false)
        val tvNombreVendedor = cardVendedor.findViewById<TextView>(R.id.tvNombreVendedor)
        tvNombreVendedor.text = "Vendido por: $vendedor"
        containerProductos.addView(cardVendedor)

        // Agregar cada producto del vendedor
        for (producto in productos) {
            agregarProductoCard(producto)
        }
    }

    // Esta es tu otra función que tenía el error
    private fun agregarProductoCard(item: ItemCarrito) {
        val inflater = LayoutInflater.from(this)
        // (AHORA SÍ ENCUENTRA EL LAYOUT)
        val cardProducto = inflater.inflate(R.layout.item_producto_carrito, containerProductos, false)

        // Conectar vistas del 'item_producto_carrito.xml'
        val ivImagen = cardProducto.findViewById<ImageView>(R.id.ivProductoImagen)
        val tvMarca = cardProducto.findViewById<TextView>(R.id.tvMarcaProducto)
        val tvNombre = cardProducto.findViewById<TextView>(R.id.tvNombreProducto)
        val tvPrecio = cardProducto.findViewById<TextView>(R.id.tvPrecioProducto)
        val tvCantidad = cardProducto.findViewById<TextView>(R.id.tvCantidad)
        val tvVendedor = cardProducto.findViewById<TextView>(R.id.tvVendedorProducto)
        val btnEliminar = cardProducto.findViewById<ImageButton>(R.id.btnEliminar)

        // Llenar datos
        tvMarca.text = item.marcaProducto.uppercase()
        tvNombre.text = item.nombreProducto
        tvPrecio.text = formatoMoneda(item.precioProducto)
        tvCantidad.text = "Cantidad: ${item.cantidad}"
        tvVendedor.visibility = View.GONE // Ocultamos el vendedor (ya está en el título)

        // Cargar imagen
        val rutasImagenes = item.imagenProducto.split(",")
        if (rutasImagenes.isNotEmpty() && rutasImagenes[0].isNotEmpty()) {
            val archivoImagen = File(rutasImagenes[0])
            if (archivoImagen.exists()) {
                ivImagen.setImageURI(Uri.fromFile(archivoImagen))
            }
        }

        btnEliminar.setOnClickListener {
            eliminarDelCarrito(item.carritoId)
        }

        containerProductos.addView(cardProducto)
    }

    private fun eliminarDelCarrito(carritoId: Int) {
        val db = SQLiteHelper(this)
        val resultado = db.eliminarDelCarrito(carritoId)
        db.close()

        if (resultado > 0) {
            Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
            cargarDatosDelCarrito() // Recargar carrito
        } else {
            Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarResumenCostos(subtotal: Double, paisVendedor: String) {
        var costoEnvio: Double
        var impuestos: Double

        if (paisVendedor.equals(PAIS_LOCAL, ignoreCase = true)) {
            costoEnvio = COSTO_ENVIO_LOCAL
            impuestos = 0.0
            tvEnvioInfo.text = "$ ${formatoMoneda(costoEnvio)} (Nacional)"
            tvImpuestos.text = "$ 0 (exento)"
        } else {
            val tipoCambio = 900.0
            costoEnvio = COSTO_ENVIO_INTERNACIONAL * tipoCambio
            impuestos = subtotal * TASA_IMPUESTOS
            tvEnvioInfo.text = "$ ${formatoMoneda(costoEnvio)} (Internacional)"
            tvImpuestos.text = formatoMoneda(impuestos)
        }

        val total = subtotal + costoEnvio + impuestos
        tvSubtotal.text = formatoMoneda(subtotal)
        tvEnvio.text = formatoMoneda(costoEnvio)
        tvTotalEstimado.text = formatoMoneda(total)
    }

    private fun formatoMoneda(valor: Double): String {
        val valorRedondeado = Math.round(valor)
        return if (paisUsuario.equals("Chile", ignoreCase = true)) {
            "$ ${String.format("%,d", valorRedondeado).replace(",", ".")}"
        } else {
            "$${String.format("%,d", valorRedondeado)}"
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatosDelCarrito() // Recargar cuando volvemos
    }
}