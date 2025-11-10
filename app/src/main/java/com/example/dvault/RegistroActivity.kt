package com.example.dvault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import models.SQLiteHelper

class RegistroActivity : AppCompatActivity() {

    private lateinit var inputNombre: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputPais: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var tvYaTienesCuenta: TextView

    private lateinit var dbHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asegúrate de usar el layout que tiene el campo 'inputPais'
        setContentView(R.layout.activity_registro)

        dbHelper = SQLiteHelper(this)

        // Conectar vistas
        inputNombre = findViewById(R.id.inputNombre)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        inputPais = findViewById(R.id.inputPais)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        tvYaTienesCuenta = findViewById(R.id.tvYaTienesCuenta)

        // --- LÓGICA DE REGISTRO ACTUALIZADA ---
        btnRegistrar.setOnClickListener {
            // 1. Obtener los textos
            val nombre = inputNombre.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString() // No usamos .trim() aquí
            val pais = inputPais.text.toString().trim()

            // 2. Limpiar errores previos
            inputNombre.error = null
            inputEmail.error = null
            inputPassword.error = null
            inputPais.error = null

            // --- 3. INICIO DE VALIDACIONES ---

            // Validación de campos vacíos (Req: nombre y contraseña no nulos)
            if (nombre.isEmpty()) {
                inputNombre.error = "El nombre no puede estar vacío"
                inputNombre.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                inputEmail.error = "El email no puede estar vacío"
                inputEmail.requestFocus()
                return@setOnClickListener
            }
            if (pais.isEmpty()) {
                inputPais.error = "El país no puede estar vacío"
                inputPais.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                inputPassword.error = "La contraseña no puede estar vacía"
                inputPassword.requestFocus()
                return@setOnClickListener
            }

            // Validación de Contraseña (Req: 8 caracteres)
            if (password.length < 8) {
                inputPassword.error = "Debe tener al menos 8 caracteres"
                inputPassword.requestFocus()
                return@setOnClickListener
            }

            // Validación de Contraseña (Req: 1 Mayúscula)
            // '.any { it.isUpperCase() }' significa "contiene al menos un carácter que es mayúscula"
            if (!password.any { it.isUpperCase() }) {
                inputPassword.error = "Debe incluir al menos una mayúscula"
                inputPassword.requestFocus()
                return@setOnClickListener
            }

            // Validación de Contraseña (Req: 1 Símbolo)
            // '.any { !it.isLetterOrDigit() }' significa "contiene al menos un carácter que NO es letra NI número"
            if (!password.any { !it.isLetterOrDigit() }) {
                inputPassword.error = "Debe incluir al menos un símbolo (ej: @, #, !)"
                inputPassword.requestFocus()
                return@setOnClickListener
            }

            // --- 4. FIN DE VALIDACIONES ---

            // Si todo está bien, insertar en la BD
            val exito = dbHelper.insertarUsuario(nombre, email, password, pais)

            if (exito != -1L) {
                Toast.makeText(this, "Registro exitoso. Inicia sesión.", Toast.LENGTH_LONG).show()
                finish() // Cierra la pantalla de registro
            } else {
                inputEmail.error = "El correo ya está registrado"
                inputEmail.requestFocus()
            }
        }

        // Ir al Login
        tvYaTienesCuenta.setOnClickListener {
            finish() // Cierra esta actividad y vuelve al Login
        }
    }

    override fun onDestroy() {
        dbHelper.close() // Cierra la conexión a la BD cuando la actividad se destruye
        super.onDestroy()
    }
}