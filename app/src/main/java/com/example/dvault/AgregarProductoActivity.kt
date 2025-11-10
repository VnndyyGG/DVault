package com.example.dvault

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import models.SQLiteHelper

class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var inputNombre: EditText
    private lateinit var inputMarca: EditText
    private lateinit var inputDesc: EditText
    private lateinit var inputPrecio: EditText
    private lateinit var inputStock: EditText
    private lateinit var btnPublicar: Button
    private lateinit var btnBack: ImageButton

    private lateinit var dbHelper: SQLiteHelper
    private var vendedorId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_producto)

        dbHelper = SQLiteHelper(this)

        val sharedPref = getSharedPreferences("DVaultPrefs", MODE_PRIVATE)
        vendedorId = sharedPref.getInt("USUARIO_ID", -1)

        if (vendedorId == -1) {
            Toast.makeText(this, "Error: No se pudo identificar al vendedor", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        inputNombre = findViewById(R.id.inputNombreProducto)
        inputMarca = findViewById(R.id.inputMarca)
        inputDesc = findViewById(R.id.inputDescripcion)
        inputPrecio = findViewById(R.id.inputPrecio)
        inputStock = findViewById(R.id.inputStock)
        btnPublicar = findViewById(R.id.btnPublicar)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        btnPublicar.setOnClickListener {
            publicarProducto()
        }
    }

    private fun publicarProducto() {
        val nombre = inputNombre.text.toString().trim()
        val marca = inputMarca.text.toString().trim()
        val desc = inputDesc.text.toString().trim()
        val precioStr = inputPrecio.text.toString().trim()
        val stockStr = inputStock.text.toString().trim()

        if (nombre.isEmpty() || marca.isEmpty() || desc.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val precio = precioStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()

        if (precio == null || precio <= 0) {
            inputPrecio.error = "Ingresa un precio válido"
            return
        }

        if (stock == null || stock <= 0) {
            inputStock.error = "Ingresa un stock válido"
            return
        }

        // --- LLAMADA CORREGIDA ---
        // Orden requerido por SQLiteHelper: (nombre, marca, precio, descripcion, imagen, vendedorId)
        val success = dbHelper.insertarProducto(
            nombre,
            marca,
            precio,     // 3. Double
            desc,       // 4. String
            "",         // 5. String (Imagen, requerido por la BD)
            vendedorId
        )
        // -------------------------

        if (success != -1L) {
            Toast.makeText(this, "Producto publicado con éxito", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Error al publicar el producto", Toast.LENGTH_SHORT).show()
        }
    }
}