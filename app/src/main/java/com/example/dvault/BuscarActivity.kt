package com.example.dvault

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import models.Producto
import models.SQLiteHelper

class BuscarActivity : AppCompatActivity(), AdaptadorProducto.AlPulsarProductoListener {

    private lateinit var inputSearch: EditText
    private lateinit var recyclerResultados: RecyclerView
    private lateinit var adaptadorProductos: AdaptadorProducto
    private val listaProductos = mutableListOf<Producto>()
    private val listaProductosCompleta = mutableListOf<Producto>()

    private lateinit var dbHelper: SQLiteHelper
    private var usuarioId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar)

        // ✅ Ajustar padding para la barra de estado
        val rootLayout = findViewById<ConstraintLayout>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }

        dbHelper = SQLiteHelper(this)

        // Obtener ID del usuario
        val sharedPref = getSharedPreferences("DVaultPrefs", MODE_PRIVATE)
        usuarioId = sharedPref.getInt("USUARIO_ID", -1)

        inicializarVistas()
        configurarRecyclerView()
        cargarTodosLosProductos()
        configurarBusqueda()
    }

    private fun inicializarVistas() {
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        inputSearch = findViewById(R.id.inputSearch)
        recyclerResultados = findViewById(R.id.recyclerViewResultados)

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun configurarRecyclerView() {
        // Grid de 2 columnas
        recyclerResultados.layoutManager = GridLayoutManager(this, 2)
        adaptadorProductos = AdaptadorProducto(this, listaProductos, this)
        recyclerResultados.adapter = adaptadorProductos
    }

    private fun cargarTodosLosProductos() {
        listaProductosCompleta.clear()

        val cursor = dbHelper.obtenerTodosLosProductos()

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
                listaProductosCompleta.add(producto)
            } while (cursor.moveToNext())

            cursor.close()
        }

        dbHelper.close()

        // Mostrar todos los productos inicialmente
        listaProductos.clear()
        listaProductos.addAll(listaProductosCompleta)
        adaptadorProductos.notifyDataSetChanged()
    }

    private fun configurarBusqueda() {
        // Búsqueda en tiempo real mientras el usuario escribe
        inputSearch.addTextChangedListener { texto ->
            filtrarProductos(texto.toString())
        }
    }

    private fun filtrarProductos(query: String) {
        listaProductos.clear()

        if (query.isEmpty()) {
            // Si no hay búsqueda, mostrar todos
            listaProductos.addAll(listaProductosCompleta)
        } else {
            // Filtrar por nombre, marca o descripción
            val queryLower = query.lowercase()
            for (producto in listaProductosCompleta) {
                if (producto.nombre.lowercase().contains(queryLower) ||
                    producto.marca.lowercase().contains(queryLower) ||
                    producto.descripcion.lowercase().contains(queryLower)
                ) {
                    listaProductos.add(producto)
                }
            }
        }

        adaptadorProductos.notifyDataSetChanged()

        // Mostrar mensaje si no hay resultados
        if (listaProductos.isEmpty() && query.isNotEmpty()) {
            Toast.makeText(this, "No se encontraron resultados", Toast.LENGTH_SHORT).show()
        }
    }

    // Implementación de la interfaz del adaptador
    override fun alPulsarAgregarAlCarrito(producto: Producto) {
        val db = SQLiteHelper(this)
        val exito = db.agregarAlCarrito(usuarioId, producto.id, 1)
        db.close()

        if (exito != -1L) {
            Toast.makeText(this, "${producto.nombre} añadido al carrito", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al añadir al carrito", Toast.LENGTH_SHORT).show()
        }
    }
}