package com.example.dvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import models.SQLiteHelper
import kotlin.math.roundToInt

class PerfilActivity : AppCompatActivity() {

    private lateinit var dbHelper: SQLiteHelper
    private var usuarioId: Int = -1

    // Vistas
    private lateinit var tvAvatarInicial: TextView
    private lateinit var tvNombreUsuario: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var tvPais: TextView
    private lateinit var tvPromedioEstrellas: TextView
    private lateinit var tvCantidadCalificaciones: TextView
    private lateinit var containerComentarios: LinearLayout
    private lateinit var tvSinComentarios: TextView
    private lateinit var btnBack: ImageButton

    // Vistas de distribución de estrellas
    private lateinit var barra5Estrellas: View
    private lateinit var barra4Estrellas: View
    private lateinit var barra3Estrellas: View
    private lateinit var barra2Estrellas: View
    private lateinit var barra1Estrella: View
    private lateinit var tvCantidad5: TextView
    private lateinit var tvCantidad4: TextView
    private lateinit var tvCantidad3: TextView
    private lateinit var tvCantidad2: TextView
    private lateinit var tvCantidad1: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Ajustar padding para la barra de estado
        val rootLayout = findViewById<ConstraintLayout>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }

        dbHelper = SQLiteHelper(this)

        // Obtener ID del usuario desde SharedPreferences
        val sharedPref = getSharedPreferences("DVaultPrefs", MODE_PRIVATE)
        usuarioId = sharedPref.getInt("USUARIO_ID", -1)

        if (usuarioId == -1) {
            Toast.makeText(this, "Error: No se pudo identificar al usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        inicializarVistas()
        cargarDatosUsuario()
        cargarCalificaciones()

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun inicializarVistas() {
        btnBack = findViewById(R.id.btnBack)
        tvAvatarInicial = findViewById(R.id.tvAvatarInicial)
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario)
        tvEmail = findViewById(R.id.tvEmail)
        tvTelefono = findViewById(R.id.tvTelefono)
        tvPais = findViewById(R.id.tvPais)
        tvPromedioEstrellas = findViewById(R.id.tvPromedioEstrellas)
        tvCantidadCalificaciones = findViewById(R.id.tvCantidadCalificaciones)
        containerComentarios = findViewById(R.id.containerComentarios)
        tvSinComentarios = findViewById(R.id.tvSinComentarios)

        // Barras de distribución
        barra5Estrellas = findViewById(R.id.barra5Estrellas)
        barra4Estrellas = findViewById(R.id.barra4Estrellas)
        barra3Estrellas = findViewById(R.id.barra3Estrellas)
        barra2Estrellas = findViewById(R.id.barra2Estrellas)
        barra1Estrella = findViewById(R.id.barra1Estrella)

        // Cantidades
        tvCantidad5 = findViewById(R.id.tvCantidad5)
        tvCantidad4 = findViewById(R.id.tvCantidad4)
        tvCantidad3 = findViewById(R.id.tvCantidad3)
        tvCantidad2 = findViewById(R.id.tvCantidad2)
        tvCantidad1 = findViewById(R.id.tvCantidad1)
    }

    private fun cargarDatosUsuario() {
        val cursor = dbHelper.obtenerUsuarioPorId(usuarioId)

        if (cursor != null && cursor.moveToFirst()) {
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono"))
            val pais = cursor.getString(cursor.getColumnIndexOrThrow("pais"))

            // Mostrar datos
            tvNombreUsuario.text = nombre
            tvEmail.text = email
            tvTelefono.text = telefono
            tvPais.text = pais

            // Mostrar inicial del nombre en el avatar
            if (nombre.isNotEmpty()) {
                tvAvatarInicial.text = nombre.first().uppercase()
            }

            cursor.close()
        }
    }

    private fun cargarCalificaciones() {
        // Obtener promedio y cantidad de calificaciones
        val promedio = dbHelper.obtenerPromedioCalificaciones(usuarioId)
        val cantidadTotal = dbHelper.obtenerCantidadCalificaciones(usuarioId)

        // Mostrar promedio
        if (cantidadTotal > 0) {
            tvPromedioEstrellas.text = String.format("%.1f", promedio)
            tvCantidadCalificaciones.text = "($cantidadTotal ${if (cantidadTotal == 1) "calificación" else "calificaciones"})"
        } else {
            tvPromedioEstrellas.text = "0.0"
            tvCantidadCalificaciones.text = "(0 calificaciones)"
        }

        // Cargar distribución de estrellas
        cargarDistribucionEstrellas()

        // Cargar comentarios recientes
        cargarComentariosRecientes()
    }

    private fun cargarDistribucionEstrellas() {
        val cursor = dbHelper.obtenerCalificacionesVendedor(usuarioId)
        val distribucion = mutableMapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val estrellas = cursor.getInt(cursor.getColumnIndexOrThrow("estrellas"))
                distribucion[estrellas] = distribucion[estrellas]!! + 1
            } while (cursor.moveToNext())
            cursor.close()
        }

        val total = distribucion.values.sum()

        // Actualizar cantidades
        tvCantidad5.text = distribucion[5].toString()
        tvCantidad4.text = distribucion[4].toString()
        tvCantidad3.text = distribucion[3].toString()
        tvCantidad2.text = distribucion[2].toString()
        tvCantidad1.text = distribucion[1].toString()

        // Actualizar barras de progreso (ancho proporcional)
        if (total > 0) {
            actualizarBarraProgreso(barra5Estrellas, distribucion[5]!!, total)
            actualizarBarraProgreso(barra4Estrellas, distribucion[4]!!, total)
            actualizarBarraProgreso(barra3Estrellas, distribucion[3]!!, total)
            actualizarBarraProgreso(barra2Estrellas, distribucion[2]!!, total)
            actualizarBarraProgreso(barra1Estrella, distribucion[1]!!, total)
        }
    }

    private fun actualizarBarraProgreso(barra: View, cantidad: Int, total: Int) {
        val layoutParams = barra.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = if (cantidad > 0) (cantidad.toFloat() / total.toFloat()) else 0f
        barra.layoutParams = layoutParams

        // Cambiar color si tiene calificaciones
        if (cantidad > 0) {
            barra.setBackgroundColor(resources.getColor(android.R.color.holo_orange_light, null))
        }
    }

    private fun cargarComentariosRecientes() {
        val cursor = dbHelper.obtenerCalificacionesVendedor(usuarioId)

        if (cursor != null && cursor.moveToFirst()) {
            tvSinComentarios.visibility = View.GONE
            var contador = 0

            do {
                if (contador >= 3) break // Mostrar solo los 3 comentarios más recientes

                val estrellas = cursor.getInt(cursor.getColumnIndexOrThrow("estrellas"))
                val comentario = cursor.getString(cursor.getColumnIndexOrThrow("comentario"))
                val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
                val nombreComprador = cursor.getString(cursor.getColumnIndexOrThrow("nombre_comprador"))

                agregarComentario(nombreComprador, estrellas, comentario, fecha)
                contador++
            } while (cursor.moveToNext())

            cursor.close()
        } else {
            tvSinComentarios.visibility = View.VISIBLE
        }
    }

    private fun agregarComentario(nombreComprador: String, estrellas: Int, comentario: String, fecha: String) {
        val inflater = LayoutInflater.from(this)
        val comentarioView = inflater.inflate(R.layout.item_comentario, containerComentarios, false)

        val tvNombreComprador = comentarioView.findViewById<TextView>(R.id.tvNombreComprador)
        val tvEstrellas = comentarioView.findViewById<TextView>(R.id.tvEstrellas)
        val tvComentario = comentarioView.findViewById<TextView>(R.id.tvComentario)
        val tvFecha = comentarioView.findViewById<TextView>(R.id.tvFecha)

        tvNombreComprador.text = nombreComprador
        tvEstrellas.text = "★".repeat(estrellas) + "☆".repeat(5 - estrellas)
        tvComentario.text = comentario.ifEmpty { "Sin comentario" }
        tvFecha.text = fecha

        containerComentarios.addView(comentarioView)
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}