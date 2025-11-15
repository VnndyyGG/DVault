package com.example.dvault

import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import models.SQLiteHelper
import java.text.SimpleDateFormat
import java.util.*

class ProductoDetalleActivity : AppCompatActivity() {

    private lateinit var dbHelper: SQLiteHelper
    private var usuarioId: Int = -1
    private var productoId: Int = -1
    private var vendedorId: Int = -1
    private var esVendedor: Boolean = false

    // Vistas
    private lateinit var btnBack: ImageButton
    private lateinit var btnFavorito: ImageButton
    private lateinit var viewPagerImagenes: ViewPager2
    private lateinit var tvIndicadorPagina: TextView
    private lateinit var tvBadgeOferta: TextView
    private lateinit var tvMarca: TextView
    private lateinit var tvNombre: TextView
    private lateinit var tvPrecio: TextView
    private lateinit var layoutPrecioOferta: LinearLayout
    private lateinit var tvPrecioOriginal: TextView
    private lateinit var tvPrecioOferta: TextView
    private lateinit var tvTiempoRestante: TextView
    private lateinit var tvVendedor: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var layoutBotonesComprador: LinearLayout
    private lateinit var layoutBotonesVendedor: LinearLayout
    private lateinit var btnAgregarFavoritoImagen: ImageButton
    private lateinit var btnAgregarCarrito: Button
    private lateinit var btnEditar: Button
    private lateinit var btnOferta: Button

    private var precioNormal: Double = 0.0
    private var precioOfertaActual: Double? = null
    private var fechaFinOferta: Long? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_producto_detalle)

        // Ajustar padding para barra de estado
        val rootLayout = findViewById<ConstraintLayout>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }

        dbHelper = SQLiteHelper(this)

        // Obtener datos del intent
        productoId = intent.getIntExtra("PRODUCTO_ID", -1)
        if (productoId == -1) {
            Toast.makeText(this, "Error: Producto no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Obtener ID del usuario actual
        val sharedPref = getSharedPreferences("DVaultPrefs", MODE_PRIVATE)
        usuarioId = sharedPref.getInt("USUARIO_ID", -1)

        inicializarVistas()
        cargarProducto()
        configurarBotones()
    }

    private fun inicializarVistas() {
        btnBack = findViewById(R.id.btnBack)
        btnFavorito = findViewById(R.id.btnFavorito)
        viewPagerImagenes = findViewById(R.id.viewPagerImagenes)
        tvIndicadorPagina = findViewById(R.id.tvIndicadorPagina)
        tvBadgeOferta = findViewById(R.id.tvBadgeOferta)
        tvMarca = findViewById(R.id.tvMarca)
        tvNombre = findViewById(R.id.tvNombre)
        tvPrecio = findViewById(R.id.tvPrecio)
        layoutPrecioOferta = findViewById(R.id.layoutPrecioOferta)
        tvPrecioOriginal = findViewById(R.id.tvPrecioOriginal)
        tvPrecioOferta = findViewById(R.id.tvPrecioOferta)
        tvTiempoRestante = findViewById(R.id.tvTiempoRestante)
        tvVendedor = findViewById(R.id.tvVendedor)
        tvDescripcion = findViewById(R.id.tvDescripcion)
        layoutBotonesComprador = findViewById(R.id.layoutBotonesComprador)
        layoutBotonesVendedor = findViewById(R.id.layoutBotonesVendedor)
        btnAgregarFavoritoImagen = findViewById(R.id.btnAgregarFavoritoImagen)
        btnAgregarCarrito = findViewById(R.id.btnAgregarCarrito)
        btnEditar = findViewById(R.id.btnEditar)
        btnOferta = findViewById(R.id.btnOferta)
    }

    private fun cargarProducto() {
        val cursor = dbHelper.obtenerProductoPorId(productoId)

        if (cursor != null && cursor.moveToFirst()) {
            val marca = cursor.getString(cursor.getColumnIndexOrThrow("producto_marca"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("producto_nombre"))
            val precio = cursor.getDouble(cursor.getColumnIndexOrThrow("producto_precio"))
            val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("producto_descripcion"))
            val imagenes = cursor.getString(cursor.getColumnIndexOrThrow("producto_imagen"))
            vendedorId = cursor.getInt(cursor.getColumnIndexOrThrow("vendedor_id"))

            // Campos de oferta
            val columnIndexOferta = cursor.getColumnIndex("precio_oferta")
            val columnIndexFechaFin = cursor.getColumnIndex("fecha_fin_oferta")

            precioOfertaActual = if (columnIndexOferta >= 0 && !cursor.isNull(columnIndexOferta)) {
                cursor.getDouble(columnIndexOferta)
            } else null

            fechaFinOferta = if (columnIndexFechaFin >= 0 && !cursor.isNull(columnIndexFechaFin)) {
                cursor.getString(columnIndexFechaFin).toLongOrNull()
            } else null

            cursor.close()

            // Obtener nombre del vendedor
            val cursorVendedor = dbHelper.obtenerUsuarioPorId(vendedorId)
            var nombreVendedor = "Desconocido"
            if (cursorVendedor != null && cursorVendedor.moveToFirst()) {
                nombreVendedor = cursorVendedor.getString(cursorVendedor.getColumnIndexOrThrow("nombre"))
                cursorVendedor.close()
            }

            // Determinar si el usuario actual es el vendedor
            esVendedor = (usuarioId == vendedorId)

            // Mostrar datos
            tvMarca.text = marca.uppercase()
            tvNombre.text = nombre
            tvVendedor.text = nombreVendedor
            tvDescripcion.text = descripcion
            precioNormal = precio

            // Cargar imágenes
            cargarImagenes(imagenes)

            // Mostrar precio (con o sin oferta)
            mostrarPrecio()

            // Configurar interfaz según rol
            configurarInterfazSegunRol()

            // Configurar favorito
            actualizarBotonFavorito()
        }

        dbHelper.close()
    }

    private fun cargarImagenes(imagenesString: String) {
        val rutas = imagenesString.split(",").filter { it.isNotEmpty() }

        if (rutas.isEmpty()) {
            tvIndicadorPagina.text = "0/0"
            return
        }

        val adapter = ImagenProductoAdapter(rutas)
        viewPagerImagenes.adapter = adapter

        tvIndicadorPagina.text = "1/${rutas.size}"

        viewPagerImagenes.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tvIndicadorPagina.text = "${position + 1}/${rutas.size}"
            }
        })
    }

    private fun mostrarPrecio() {
        // Verificar si hay oferta activa
        if (precioOfertaActual != null && fechaFinOferta != null) {
            val ahora = System.currentTimeMillis()
            if (fechaFinOferta!! > ahora) {
                // Oferta activa
                tvPrecio.visibility = View.GONE
                layoutPrecioOferta.visibility = View.VISIBLE
                tvBadgeOferta.visibility = View.VISIBLE

                tvPrecioOriginal.text = formatoMoneda(precioNormal)
                tvPrecioOriginal.paintFlags = tvPrecioOriginal.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvPrecioOferta.text = formatoMoneda(precioOfertaActual!!)

                // Iniciar contador de tiempo
                iniciarContadorTiempo()
                return
            } else {
                // Oferta expirada, eliminarla
                dbHelper.eliminarOferta(productoId)
            }
        }

        // Sin oferta o oferta expirada
        tvPrecio.visibility = View.VISIBLE
        tvPrecio.text = formatoMoneda(precioNormal)
        layoutPrecioOferta.visibility = View.GONE
        tvBadgeOferta.visibility = View.GONE
    }

    private fun iniciarContadorTiempo() {
        handler.post(object : Runnable {
            override fun run() {
                if (fechaFinOferta != null) {
                    val tiempoRestante = fechaFinOferta!! - System.currentTimeMillis()
                    if (tiempoRestante > 0) {
                        tvTiempoRestante.text = "Termina en ${formatearTiempo(tiempoRestante)}"
                        handler.postDelayed(this, 1000) // Actualizar cada segundo
                    } else {
                        // Oferta expirada
                        tvTiempoRestante.text = "Oferta expirada"
                        handler.postDelayed({
                            recreate() // Recargar activity
                        }, 2000)
                    }
                }
            }
        })
    }

    private fun formatearTiempo(millis: Long): String {
        val segundos = millis / 1000
        val horas = segundos / 3600
        val minutos = (segundos % 3600) / 60
        val segs = segundos % 60

        return when {
            horas > 0 -> "${horas}h ${minutos}m"
            minutos > 0 -> "${minutos}m ${segs}s"
            else -> "${segs}s"
        }
    }

    private fun configurarInterfazSegunRol() {
        if (esVendedor) {
            // Vista de vendedor
            btnFavorito.visibility = View.GONE
            layoutBotonesComprador.visibility = View.GONE
            layoutBotonesVendedor.visibility = View.VISIBLE

            // Cambiar texto del botón de oferta si ya existe una
            if (precioOfertaActual != null && fechaFinOferta != null && fechaFinOferta!! > System.currentTimeMillis()) {
                btnOferta.text = "CANCELAR OFERTA"
            } else {
                btnOferta.text = "CREAR OFERTA"
            }
        } else {
            // Vista de comprador
            btnFavorito.visibility = View.VISIBLE
            layoutBotonesComprador.visibility = View.VISIBLE
            layoutBotonesVendedor.visibility = View.GONE
        }
    }

    private fun actualizarBotonFavorito() {
        if (!esVendedor) {
            val esFav = dbHelper.esFavorito(usuarioId, productoId)
            if (esFav) {
                btnAgregarFavoritoImagen.setImageResource(R.drawable.ic_heart_filled)
                btnAgregarFavoritoImagen.setColorFilter(
                    resources.getColor(android.R.color.holo_red_light, null)
                )
            } else {
                btnAgregarFavoritoImagen.setImageResource(R.drawable.ic_heart)
                btnAgregarFavoritoImagen.setColorFilter(
                    resources.getColor(android.R.color.white, null)
                )
            }
        }
    }

    private fun configurarBotones() {
        btnBack.setOnClickListener { finish() }

        btnFavorito.setOnClickListener {
            toggleFavorito()
        }

        btnAgregarFavoritoImagen.setOnClickListener {
            toggleFavorito()
        }

        btnAgregarCarrito.setOnClickListener {
            agregarAlCarrito()
        }

        btnEditar.setOnClickListener {
            mostrarDialogoEditar()
        }

        btnOferta.setOnClickListener {
            if (precioOfertaActual != null && fechaFinOferta != null && fechaFinOferta!! > System.currentTimeMillis()) {
                cancelarOferta()
            } else {
                mostrarDialogoCrearOferta()
            }
        }
    }

    private fun toggleFavorito() {
        val db = SQLiteHelper(this)
        if (db.esFavorito(usuarioId, productoId)) {
            db.eliminarDeFavoritos(usuarioId, productoId)
            Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
        } else {
            db.agregarAFavoritos(usuarioId, productoId)
            Toast.makeText(this, "Agregado a favoritos ❤️", Toast.LENGTH_SHORT).show()
        }
        db.close()
        actualizarBotonFavorito()
    }

    private fun agregarAlCarrito() {
        val db = SQLiteHelper(this)
        val exito = db.agregarAlCarrito(usuarioId, productoId, 1)
        db.close()

        if (exito != -1L) {
            Toast.makeText(this, "Producto agregado al carrito", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al agregar al carrito", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoEditar() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialogo_editar_producto)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val etMarca = dialog.findViewById<EditText>(R.id.etMarca)
        val etNombre = dialog.findViewById<EditText>(R.id.etNombre)
        val etPrecio = dialog.findViewById<EditText>(R.id.etPrecio)
        val etDescripcion = dialog.findViewById<EditText>(R.id.etDescripcion)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelar)
        val btnGuardar = dialog.findViewById<Button>(R.id.btnGuardar)

        // Prellenar con datos actuales
        etMarca.setText(tvMarca.text.toString())
        etNombre.setText(tvNombre.text.toString())
        etPrecio.setText(precioNormal.toInt().toString())
        etDescripcion.setText(tvDescripcion.text.toString())

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val nuevaMarca = etMarca.text.toString().trim()
            val nuevoNombre = etNombre.text.toString().trim()
            val nuevoPrecioStr = etPrecio.text.toString().trim()
            val nuevaDescripcion = etDescripcion.text.toString().trim()

            if (nuevaMarca.isEmpty() || nuevoNombre.isEmpty() || nuevoPrecioStr.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nuevoPrecio = nuevoPrecioStr.toDoubleOrNull()
            if (nuevoPrecio == null || nuevoPrecio <= 0) {
                Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener la imagen actual
            val cursor = dbHelper.obtenerProductoPorId(productoId)
            var imagenActual = ""
            if (cursor != null && cursor.moveToFirst()) {
                imagenActual = cursor.getString(cursor.getColumnIndexOrThrow("producto_imagen"))
                cursor.close()
            }

            val resultado = dbHelper.actualizarProducto(
                productoId,
                nuevoNombre,
                nuevaMarca,
                nuevoPrecio,
                nuevaDescripcion,
                imagenActual
            )

            if (resultado > 0) {
                Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                recreate() // Recargar la actividad
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun mostrarDialogoCrearOferta() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_crear_oferta)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val tvPrecioActual = dialog.findViewById<TextView>(R.id.tvPrecioActual)
        val etPrecioOferta = dialog.findViewById<EditText>(R.id.etPrecioOferta)
        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroupDuracion)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelar)
        val btnCrear = dialog.findViewById<Button>(R.id.btnCrearOferta)

        tvPrecioActual.text = formatoMoneda(precioNormal)

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnCrear.setOnClickListener {
            val precioOfertaStr = etPrecioOferta.text.toString().trim()
            if (precioOfertaStr.isEmpty()) {
                Toast.makeText(this, "Ingresa el precio de oferta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val precioOferta = precioOfertaStr.toDoubleOrNull()
            if (precioOferta == null || precioOferta <= 0) {
                Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (precioOferta >= precioNormal) {
                Toast.makeText(this, "El precio de oferta debe ser menor al precio normal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener duración seleccionada
            val duracionHoras = when (radioGroup.checkedRadioButtonId) {
                R.id.radio1Hora -> 1
                R.id.radio3Horas -> 3
                R.id.radio6Horas -> 6
                R.id.radio12Horas -> 12
                R.id.radio24Horas -> 24
                else -> 1
            }

            val fechaFin = System.currentTimeMillis() + (duracionHoras * 3600 * 1000)
            val resultado = dbHelper.crearOferta(productoId, precioOferta, fechaFin.toString())

            if (resultado > 0) {
                Toast.makeText(this, "¡Oferta creada por $duracionHoras hora(s)!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                recreate() // Recargar la actividad
            } else {
                Toast.makeText(this, "Error al crear oferta", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun cancelarOferta() {
        val resultado = dbHelper.eliminarOferta(productoId)
        if (resultado > 0) {
            Toast.makeText(this, "Oferta cancelada", Toast.LENGTH_SHORT).show()
            recreate() // Recargar la actividad
        } else {
            Toast.makeText(this, "Error al cancelar oferta", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatoMoneda(valor: Double): String {
        val sharedPref = getSharedPreferences("DVaultPrefs", MODE_PRIVATE)
        val pais = sharedPref.getString("USUARIO_PAIS", "Chile") ?: "Chile"
        val valorRedondeado = Math.round(valor)
        return if (pais.equals("Chile", ignoreCase = true)) {
            "$ ${String.format("%,d", valorRedondeado).replace(",", ".")}"
        } else {
            "$${String.format("%,d", valorRedondeado)}"
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        dbHelper.close()
        super.onDestroy()
    }
}