package com.example.dvault

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import models.Producto
import models.SQLiteHelper

// Esta pantalla también usa el AdaptadorProducto
class ListarActivity : AppCompatActivity(), AdaptadorProducto.AlPulsarProductoListener {

    private lateinit var listaVisualMisArticulos: RecyclerView
    private lateinit var adaptadorProductos: AdaptadorProducto
    private val listaMisProductos = mutableListOf<Producto>()

    private lateinit var ayudanteBD: SQLiteHelper
    private var idVendedor: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar) // Usa tu 'activity_listar.xml'

        // ✅ Ajustar padding para la barra de estado
        val rootLayout = findViewById<ConstraintLayout>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }

        ayudanteBD = SQLiteHelper(this)

        // Obtener ID del usuario (que es el vendedor)
        val preferencias = getSharedPreferences("DVaultPrefs", MODE_PRIVATE)
        idVendedor = preferencias.getInt("USUARIO_ID", -1)

        if (idVendedor == -1) {
            Toast.makeText(this, "Error de sesión de usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        inicializarVistas()
        configurarRecyclerView()
        cargarMisProductos()
    }

    private fun inicializarVistas() {
        val btnVolver = findViewById<ImageButton>(R.id.btnBack)
        listaVisualMisArticulos = findViewById(R.id.recyclerViewMisArticulos)

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun configurarRecyclerView() {
        listaVisualMisArticulos.layoutManager = GridLayoutManager(this, 2)
        // Reutilizamos el adaptador
        adaptadorProductos = AdaptadorProducto(this, listaMisProductos, this)
        listaVisualMisArticulos.adapter = adaptadorProductos
    }

    private fun cargarMisProductos() {
        listaMisProductos.clear()

        // --- LÓGICA CORREGIDA ---
        // Usamos la función para obtener productos por el ID del vendedor
        val cursor = ayudanteBD.obtenerProductosPorVendedor(idVendedor)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val producto = Producto(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("producto_id")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("producto_nombre")),
                    marca = cursor.getString(cursor.getColumnIndexOrThrow("producto_marca")),
                    precio = cursor.getDouble(cursor.getColumnIndexOrThrow("producto_precio")),
                    descripcion = cursor.getString(cursor.getColumnIndexOrThrow("producto_descripcion")),
                    imagen = cursor.getString(cursor.getColumnIndexOrThrow("producto_imagen")),
                    vendedorId = cursor.getInt(cursor.getColumnIndexOrThrow("vendedor_id"))
                )
                listaMisProductos.add(producto)
            } while (cursor.moveToNext())

            cursor.close()
        }

        ayudanteBD.close()

        if (listaMisProductos.isEmpty()) {
            Toast.makeText(this, "Aún no has publicado productos", Toast.LENGTH_SHORT).show()
        }

        adaptadorProductos.notifyDataSetChanged()
    }

    // --- Implementación de la Interfaz ---

    override fun alPulsarAgregarAlCarrito(producto: Producto) {
        // Como son TUS propios productos, no puedes agregarlos a TU carrito.
        // (En el futuro, aquí podríamos abrir una pantalla para "Editar" o "Eliminar" el producto)
        Toast.makeText(this, "No puedes comprar tus propios productos", Toast.LENGTH_SHORT).show()
    }

    // (alPulsarTarjetaProducto se maneja dentro del adaptador y abre el detalle)
}