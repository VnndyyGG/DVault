package com.example.dvault

import android.content.Intent
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

class ComprarActivity : AppCompatActivity(), AdaptadorProducto.AlPulsarProductoListener {

    // --- Variables ---
    private lateinit var ayudanteBD: SQLiteHelper
    private lateinit var listaVisual: RecyclerView
    private lateinit var adaptadorProductos: AdaptadorProducto
    private val listaProductos = mutableListOf<Producto>()

    private var idUsuario: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comprar)

        // ✅ SOLUCIÓN 3: Ajustar padding para la barra de estado
        val rootLayout = findViewById<ConstraintLayout>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }

        ayudanteBD = SQLiteHelper(this)

        // Obtener el ID del usuario de SharedPreferences
        val preferencias = getSharedPreferences("DVaultPrefs", MODE_PRIVATE)
        idUsuario = preferencias.getInt("USUARIO_ID", -1)

        if (idUsuario == -1) {
            Toast.makeText(this, "Error de sesión de usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configurar botón de volver
        val btnVolver: ImageButton = findViewById(R.id.btnBack)
        btnVolver.setOnClickListener {
            finish() // Cierra esta pantalla y vuelve al Menú
        }

        // Configurar botón del carrito
        val btnCarrito: ImageButton = findViewById(R.id.btnCarrito)
        btnCarrito.setOnClickListener {
            val intent = Intent(this, CarritoActivity::class.java)
            startActivity(intent)
        }

        // Configurar la lista visual (RecyclerView)
        listaVisual = findViewById(R.id.recyclerViewProductos)

        // Usamos una cuadrícula de 2 columnas
        listaVisual.layoutManager = GridLayoutManager(this, 2)

        // Al crear el adaptador, le pasamos "this" (esta Actividad) como el "oyente"
        adaptadorProductos = AdaptadorProducto(this, listaProductos, this)
        listaVisual.adapter = adaptadorProductos

        // Agregar productos de prueba (SOLO LA PRIMERA VEZ, luego comenta esta línea)
        // agregarProductosDePrueba()

        // Cargar los productos de la BD
        cargarProductos()
    }

    private fun cargarProductos() {
        // Limpiar la lista antes de cargar
        listaProductos.clear()

        // Usar la función de nuestro ayudante de BD
        val cursor = ayudanteBD.obtenerTodosLosProductos()

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Leer datos del cursor usando los nombres de TUS columnas
                val producto = Producto(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("producto_id")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("producto_nombre")),
                    marca = cursor.getString(cursor.getColumnIndexOrThrow("producto_marca")),
                    precio = cursor.getDouble(cursor.getColumnIndexOrThrow("producto_precio")),
                    descripcion = cursor.getString(cursor.getColumnIndexOrThrow("producto_descripcion")),
                    imagen = cursor.getString(cursor.getColumnIndexOrThrow("producto_imagen")),
                    vendedorId = cursor.getInt(cursor.getColumnIndexOrThrow("vendedor_id"))
                )
                listaProductos.add(producto)
            } while (cursor.moveToNext())

            cursor.close()
        }

        ayudanteBD.close()

        // Notificar al adaptador que los datos han cambiado
        adaptadorProductos.notifyDataSetChanged()
    }

    // Función para agregar productos de prueba (USAR SOLO UNA VEZ)
    private fun agregarProductosDePrueba() {
        val db = SQLiteHelper(this)

        // Primero necesitas tener un usuario vendedor registrado
        // Asume que el usuario con ID 1 es vendedor
        db.insertarProducto(
            "Riñonera de Cuero",
            "Gucci",
            341.0,
            "Riñonera elegante de cuero genuino",
            "",
            1 // ID del vendedor
        )

        db.insertarProducto(
            "Mochila Shark",
            "Bape",
            98.0,
            "Mochila icónica con diseño de tiburón",
            "",
            1
        )

        db.insertarProducto(
            "Gorra Monogram",
            "Louis Vuitton",
            450.0,
            "Gorra premium con monograma LV",
            "",
            1
        )

        db.insertarProducto(
            "Sudadera Box Logo",
            "Supreme",
            320.0,
            "Sudadera clásica con logo",
            "",
            1
        )

        db.close()
        Toast.makeText(this, "Productos de prueba agregados", Toast.LENGTH_SHORT).show()
    }

    override fun alPulsarAgregarAlCarrito(producto: Producto) {
        // Abrimos una nueva instancia del ayudante para esta acción
        val db = SQLiteHelper(this)
        val exito = db.agregarAlCarrito(idUsuario, producto.id, 1)
        db.close()

        if (exito != -1L) {
            Toast.makeText(this, "${producto.nombre} añadido al carrito", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al añadir al carrito", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargamos los productos cada vez que volvemos a esta pantalla
        // (por si un vendedor agregó uno nuevo)
        cargarProductos()
    }
}