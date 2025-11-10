package com.example.dvault

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton // <-- IMPORT AÑADIDO
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView // <-- IMPORT AÑADIDO
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import models.SQLiteHelper

class VenderActivity : AppCompatActivity() {

    // --- Variables de Vistas ---
    private lateinit var inputMarca: EditText
    private lateinit var inputNombre: EditText
    private lateinit var inputDescripcion: EditText
    private lateinit var inputPrecio: EditText
    // private lateinit var btnSubirFotos: Button // <-- ELIMINADO
    private lateinit var btnPublicar: Button
    private lateinit var btnSalir: Button
    private lateinit var containerFotos: LinearLayout
    private lateinit var btnBack: ImageButton // <-- AÑADIDO
    private lateinit var cardFotosContainer: CardView // <-- AÑADIDO

    // --- Otras Variables ---
    private lateinit var dbHelper: SQLiteHelper
    private var vendedorId: Int = -1
    private val fotosUri = mutableListOf<Uri>()

    // Launcher para seleccionar imágenes
    private val seleccionarImagenes = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            agregarFotos(uris)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vender)

        dbHelper = SQLiteHelper(this)

        // Obtener ID del vendedor
        val sharedPref = getSharedPreferences("DVaultPrefs", MODE_PRIVATE)
        vendedorId = sharedPref.getInt("USUARIO_ID", -1)

        if (vendedorId == -1) {
            Toast.makeText(this, "Error: No se pudo identificar al vendedor", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        inicializarVistas()
        configurarBotones()
    }

    // --- FUNCIÓN ACTUALIZADA ---
    private fun inicializarVistas() {
        // IDs del layout
        btnBack = findViewById(R.id.btnBack)
        inputMarca = findViewById(R.id.inputMarca)
        inputNombre = findViewById(R.id.inputNombre)
        inputDescripcion = findViewById(R.id.inputDescripcion)
        inputPrecio = findViewById(R.id.inputPrecio)
        cardFotosContainer = findViewById(R.id.cardFotosContainer)
        btnPublicar = findViewById(R.id.btnPublicar)
        btnSalir = findViewById(R.id.btnSalir)
        containerFotos = findViewById(R.id.containerFotos)
    }

    // --- FUNCIÓN ACTUALIZADA ---
    private fun configurarBotones() {
        // Botón Salir (del fragmento nuevo)
        btnSalir.setOnClickListener {
            finish()
        }

        // Botón Publicar (del fragmento nuevo)
        btnPublicar.setOnClickListener {
            publicarProducto()
        }

        // Botón Retroceder (del fragmento 'inicializarVistas' nuevo)
        btnBack.setOnClickListener {
            finish()
        }

        // Click en el contenedor de fotos (del fragmento 'inicializarVistas' nuevo)
        cardFotosContainer.setOnClickListener {
            seleccionarImagenes.launch("image/*")
        }

        // El OnClickListener de 'btnSubirFotos' se eliminó
    }

    // --- ESTA FUNCIÓN QUEDA IGUAL ---
    private fun agregarFotos(uris: List<Uri>) {
        fotosUri.clear()
        fotosUri.addAll(uris)

        // Limpiar container
        containerFotos.removeAllViews()

        // Agregar las imágenes seleccionadas
        for (uri in uris) {
            val imageView = ImageView(this)
            val params = LinearLayout.LayoutParams(200, 200)
            params.marginEnd = 16
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageURI(uri)
            imageView.background = ContextCompat.getDrawable(this, R.drawable.circle_outline)
            containerFotos.addView(imageView)
        }

        Toast.makeText(this, "${uris.size} foto(s) seleccionada(s)", Toast.LENGTH_SHORT).show()
    }

    // --- ESTA FUNCIÓN QUEDA IGUAL ---
    private fun publicarProducto() {
        val marca = inputMarca.text.toString().trim()
        val nombre = inputNombre.text.toString().trim()
        val descripcion = inputDescripcion.text.toString().trim()
        val precioStr = inputPrecio.text.toString().trim()

        // Validaciones
        if (marca.isEmpty()) {
            inputMarca.error = "Ingresa la marca"
            inputMarca.requestFocus()
            return
        }
        // ... (resto de tus validaciones) ...
        if (nombre.isEmpty()) {
            inputNombre.error = "Ingresa el nombre del producto"
            inputNombre.requestFocus()
            return
        }
        if (descripcion.isEmpty()) {
            inputDescripcion.error = "Ingresa una descripción"
            inputDescripcion.requestFocus()
            return
        }
        if (precioStr.isEmpty()) {
            inputPrecio.error = "Ingresa el precio"
            inputPrecio.requestFocus()
            return
        }
        val precio = precioStr.toDoubleOrNull()
        if (precio == null || precio <= 0) {
            inputPrecio.error = "Ingresa un precio válido"
            inputPrecio.requestFocus()
            return
        }

        // Convertir URIs de fotos a String (guardar la primera por ahora)
        val imagenUri = if (fotosUri.isNotEmpty()) {
            fotosUri[0].toString() // <-- NOTA: Esto no guarda la ruta del archivo
        } else {
            "" // Sin imagen
        }

        // Insertar en la base de datos
        val resultado = dbHelper.insertarProducto(
            nombre = nombre,
            marca = marca,
            precio = precio,
            descripcion = descripcion,
            imagen = imagenUri, // <-- Se guarda la URI como String
            vendedorId = vendedorId
        )

        if (resultado != -1L) {
            Toast.makeText(this, "¡Producto publicado con éxito!", Toast.LENGTH_LONG).show()
            limpiarCampos()
        } else {
            Toast.makeText(this, "Error al publicar el producto", Toast.LENGTH_SHORT).show()
        }
    }

    // --- ESTA FUNCIÓN QUEDA IGUAL ---
    private fun limpiarCampos() {
        inputMarca.text.clear()
        inputNombre.text.clear()
        inputDescripcion.text.clear()
        inputPrecio.text.clear()
        fotosUri.clear()
        containerFotos.removeAllViews()

        // Volver a agregar el placeholder
        val imgPlaceholder = ImageView(this)
        val params = LinearLayout.LayoutParams(200, 200)
        params.marginEnd = 16
        imgPlaceholder.layoutParams = params
        imgPlaceholder.scaleType = ImageView.ScaleType.CENTER_CROP
        imgPlaceholder.setImageResource(android.R.drawable.ic_menu_gallery)
        imgPlaceholder.background = ContextCompat.getDrawable(this, R.drawable.circle_outline)
        containerFotos.addView(imgPlaceholder)
    }
}