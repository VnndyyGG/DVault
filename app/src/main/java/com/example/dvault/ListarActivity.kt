package com.example.dvault

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import models.Producto
import models.SQLiteHelper

class ListarActivity : AppCompatActivity(), AdaptadorProducto.AlPulsarProductoListener {

    private lateinit var recyclerHistorial: RecyclerView
    private lateinit var adaptadorProductos: AdaptadorProducto
    private val listaProductos = mutableListOf<Producto>()

    private lateinit var dbHelper: SQLiteHelper
    private var usuarioId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar)

        dbHelper = SQLiteHelper(this)

        // Obtener ID del usuario
        val sharedPref = getSharedPreferences("DVaultPrefs", MODE_PRIVATE)
        usuarioId = sharedPref.getInt("USUARIO_ID", -1)

        if (usuarioId == -1) {
            Toast.makeText(this, "Error de sesión de usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        inicializarVistas()
        configurarRecyclerView()
        cargarHistorial()
    }

    private fun inicializarVistas() {
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        recyclerHistorial = findViewById(R.id.recyclerViewMisArticulos) // ← ID correcto del XML

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun configurarRecyclerView() {
        recyclerHistorial.layoutManager = GridLayoutManager(this, 2)
        adaptadorProductos = AdaptadorProducto(this, listaProductos, this)
        recyclerHistorial.adapter = adaptadorProductos
    }

    private fun cargarHistorial() {
        listaProductos.clear()

        // Obtener productos del carrito (historial)
        val cursor = dbHelper.obtenerProductosCarrito(usuarioId)

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

                // Evitar duplicados
                if (!listaProductos.any { it.id == producto.id }) {
                    listaProductos.add(producto)
                }
            } while (cursor.moveToNext())

            cursor.close()
        }

        dbHelper.close()

        if (listaProductos.isEmpty()) {
            Toast.makeText(this, "No tienes productos en tu historial", Toast.LENGTH_SHORT).show()
        }

        adaptadorProductos.notifyDataSetChanged()
    }

    override fun alPulsarAgregarAlCarrito(producto: Producto) {
        // Ya están en el carrito, así que solo mostramos un mensaje
        Toast.makeText(this, "Este producto ya está en tu carrito", Toast.LENGTH_SHORT).show()
    }
}