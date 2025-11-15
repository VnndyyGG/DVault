package com.example.dvault

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import models.Producto
import models.SQLiteHelper

class FavoritosActivity : AppCompatActivity(), AdaptadorProducto.AlPulsarProductoListener {

    private lateinit var dbHelper: SQLiteHelper
    private var usuarioId: Int = -1

    private lateinit var btnBack: ImageButton
    private lateinit var tvCantidadFavoritos: TextView
    private lateinit var recyclerViewFavoritos: RecyclerView
    private lateinit var layoutSinFavoritos: LinearLayout
    private lateinit var btnIrAComprar: Button

    private val listaFavoritos = mutableListOf<Producto>()
    private lateinit var adaptador: AdaptadorProducto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoritos)

        // Ajustar padding para barra de estado
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

        if (usuarioId == -1) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        inicializarVistas()
        configurarRecyclerView()
        cargarFavoritos()

        btnBack.setOnClickListener { finish() }
        btnIrAComprar.setOnClickListener {
            val intent = Intent(this, ComprarActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun inicializarVistas() {
        btnBack = findViewById(R.id.btnBack)
        tvCantidadFavoritos = findViewById(R.id.tvCantidadFavoritos)
        recyclerViewFavoritos = findViewById(R.id.recyclerViewFavoritos)
        layoutSinFavoritos = findViewById(R.id.layoutSinFavoritos)
        btnIrAComprar = findViewById(R.id.btnIrAComprar)
    }

    private fun configurarRecyclerView() {
        recyclerViewFavoritos.layoutManager = GridLayoutManager(this, 2)
        adaptador = AdaptadorProducto(this, listaFavoritos, this)
        recyclerViewFavoritos.adapter = adaptador
    }

    private fun cargarFavoritos() {
        listaFavoritos.clear()

        val cursor = dbHelper.obtenerFavoritos(usuarioId)

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
                listaFavoritos.add(producto)
            } while (cursor.moveToNext())
            cursor.close()
        }

        dbHelper.close()

        // Actualizar UI
        actualizarUI()
    }

    private fun actualizarUI() {
        if (listaFavoritos.isEmpty()) {
            // Mostrar mensaje de "sin favoritos"
            recyclerViewFavoritos.visibility = View.GONE
            tvCantidadFavoritos.visibility = View.GONE
            layoutSinFavoritos.visibility = View.VISIBLE
        } else {
            // Mostrar lista de favoritos
            recyclerViewFavoritos.visibility = View.VISIBLE
            tvCantidadFavoritos.visibility = View.VISIBLE
            layoutSinFavoritos.visibility = View.GONE

            val cantidad = listaFavoritos.size
            tvCantidadFavoritos.text = if (cantidad == 1) {
                "1 producto guardado"
            } else {
                "$cantidad productos guardados"
            }

            adaptador.notifyDataSetChanged()
        }
    }

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

    override fun alPulsarFavorito(producto: Producto) {
        val db = SQLiteHelper(this)
        db.eliminarDeFavoritos(usuarioId, producto.id)
        db.close()

        Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()

        // Recargar lista
        cargarFavoritos()
    }

    override fun onResume() {
        super.onResume()
        cargarFavoritos()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}