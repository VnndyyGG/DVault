package com.example.dvault

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import models.SQLiteHelper
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class VenderActivity : AppCompatActivity() {

    // Vistas
    private lateinit var inputMarca: EditText
    private lateinit var inputNombre: EditText
    private lateinit var inputDescripcion: EditText
    private lateinit var inputPrecio: EditText
    private lateinit var btnPublicar: Button
    private lateinit var btnSalir: Button
    private lateinit var containerFotos: LinearLayout
    private lateinit var btnBack: ImageButton
    private lateinit var cardFotosContainer: CardView

    private lateinit var dbHelper: SQLiteHelper
    private var vendedorId: Int = -1
    private val listaRutasImagenes = mutableListOf<String>()
    private val MAX_FOTOS = 6

    // Launcher para seleccionar imágenes
    private val seleccionarImagenes = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            if (uris.size > MAX_FOTOS) {
                Toast.makeText(this, "Solo puedes seleccionar $MAX_FOTOS fotos", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            listaRutasImagenes.clear()
            containerFotos.removeAllViews()

            for (uri in uris) {
                val rutaGuardada = guardarImagenEnAlmacenamientoInterno(uri)
                if (rutaGuardada.isNotEmpty()) {
                    listaRutasImagenes.add(rutaGuardada)
                    agregarMiniatura(Uri.fromFile(File(rutaGuardada)))
                }
            }
            Toast.makeText(this, "${listaRutasImagenes.size} foto(s) seleccionada(s)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vender)

        // Ajustar padding para la barra de estado
        val rootLayout = findViewById<ConstraintLayout>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }

        dbHelper = SQLiteHelper(this)

        val sharedPref = getSharedPreferences("DVaultPrefs", MODE_PRIVATE)
        vendedorId = sharedPref.getInt("USUARIO_ID", -1)

        if (vendedorId == -1) {
            Toast.makeText(this, "Error: No se pudo identificar al vendedor", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        inicializarVistas()
        configurarBotones()
        limpiarCampos()
    }

    private fun inicializarVistas() {
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

    private fun configurarBotones() {
        btnSalir.setOnClickListener {
            finish()
        }
        btnPublicar.setOnClickListener {
            publicarProducto()
        }
        btnBack.setOnClickListener {
            finish()
        }
        cardFotosContainer.setOnClickListener {
            if (listaRutasImagenes.size >= MAX_FOTOS) {
                Toast.makeText(this, "Ya has alcanzado el límite de $MAX_FOTOS fotos", Toast.LENGTH_SHORT).show()
            } else {
                seleccionarImagenes.launch("image/*")
            }
        }
    }

    // Copia la foto al almacenamiento interno
    private fun guardarImagenEnAlmacenamientoInterno(uri: Uri): String {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val nombreArchivo = "img_${UUID.randomUUID()}.jpg"
            val archivoDestino = File(filesDir, nombreArchivo)
            val outputStream = FileOutputStream(archivoDestino)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            return archivoDestino.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
            return ""
        }
    }

    //  Añade una miniatura al ScrollView
    private fun agregarMiniatura(uri: Uri) {
        val imageView = ImageView(this)
        val tamano = (90 * resources.displayMetrics.density).toInt() // 90dp
        val params = LinearLayout.LayoutParams(tamano, tamano)
        params.marginEnd = (8 * resources.displayMetrics.density).toInt() // 8dp
        imageView.layoutParams = params
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageURI(uri)
        containerFotos.addView(imageView)
    }

    // Guarda el producto en la base de datos
    private fun publicarProducto() {
        val marca = inputMarca.text.toString().trim()
        val nombre = inputNombre.text.toString().trim()
        val descripcion = inputDescripcion.text.toString().trim()
        val precioStr = inputPrecio.text.toString().trim()

        // Validaciones
        if (marca.isEmpty()) { inputMarca.error = "Ingresa la marca"; return }
        if (nombre.isEmpty()) { inputNombre.error = "Ingresa el nombre"; return }
        if (descripcion.isEmpty()) { inputDescripcion.error = "Ingresa una descripción"; return }
        if (precioStr.isEmpty()) { inputPrecio.error = "Ingresa el precio"; return }
        val precio = precioStr.toDoubleOrNull()
        if (precio == null || precio <= 0) { inputPrecio.error = "Precio inválido"; return }

        // Advertir si no hay fotos, pero permitir continuar
        if (listaRutasImagenes.isEmpty()) {
            Toast.makeText(this, "⚠️ Producto sin fotos", Toast.LENGTH_SHORT).show()
        }

        val imagenesString = listaRutasImagenes.joinToString(",")

        val resultado = dbHelper.insertarProducto(
            nombre = nombre,
            marca = marca,
            precio = precio,
            descripcion = descripcion,
            imagen = imagenesString,
            vendedorId = vendedorId
        )

        if (resultado != -1L) {
            Toast.makeText(this, "¡Producto publicado con éxito!", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Error al publicar el producto", Toast.LENGTH_SHORT).show()
        }
    }


    private fun limpiarCampos() {
        inputMarca.text.clear()
        inputNombre.text.clear()
        inputDescripcion.text.clear()
        inputPrecio.text.clear()
        listaRutasImagenes.clear()
        containerFotos.removeAllViews()
        val placeholderLayout = LinearLayout(this)
        val tamano = (90 * resources.displayMetrics.density).toInt()
        val params = LinearLayout.LayoutParams(tamano, tamano)
        params.marginEnd = (8 * resources.displayMetrics.density).toInt()
        placeholderLayout.layoutParams = params
        placeholderLayout.setBackgroundColor(Color.parseColor("#1A2A3A"))
        placeholderLayout.gravity = Gravity.CENTER
        placeholderLayout.orientation = LinearLayout.VERTICAL

        val iconoCamara = ImageView(this)
        val tamanoIcono = (40 * resources.displayMetrics.density).toInt()
        iconoCamara.layoutParams = LinearLayout.LayoutParams(tamanoIcono, tamanoIcono)
        iconoCamara.setImageResource(android.R.drawable.ic_menu_camera)
        iconoCamara.setColorFilter(Color.parseColor("#7E98A6"))

        val textoAnadir = TextView(this)
        textoAnadir.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textoAnadir.text = "Añadir"
        textoAnadir.setTextColor(Color.parseColor("#7E98A6"))
        textoAnadir.textSize = 11f
        textoAnadir.setPadding(0, (4 * resources.displayMetrics.density).toInt(), 0, 0)

        placeholderLayout.addView(iconoCamara)
        placeholderLayout.addView(textoAnadir)
        containerFotos.addView(placeholderLayout)
    }
}