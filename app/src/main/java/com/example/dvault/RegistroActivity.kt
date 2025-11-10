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
    private lateinit var inputTelefono: EditText // <-- CAMPO AÑADIDO
    private lateinit var btnRegistrar: Button
    private lateinit var tvYaTienesCuenta: TextView

    private lateinit var dbHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        dbHelper = SQLiteHelper(this)

        // Conectar vistas
        inputNombre = findViewById(R.id.inputNombre)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        inputPais = findViewById(R.id.inputPais)
        inputTelefono = findViewById(R.id.inputTelefono) // <-- CAMPO AÑADIDO
        btnRegistrar = findViewById(R.id.btnRegistrar)
        tvYaTienesCuenta = findViewById(R.id.tvYaTienesCuenta)

        // --- LÓGICA DE REGISTRO ACTUALIZADA ---
        btnRegistrar.setOnClickListener {
            // 1. Obtener los textos
            val nombre = inputNombre.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString()
            val pais = inputPais.text.toString().trim()
            val telefono = inputTelefono.text.toString().trim() // <-- CAMPO AÑADIDO

            // 2. Limpiar errores previos
            inputNombre.error = null
            inputEmail.error = null
            inputPassword.error = null
            inputPais.error = null
            inputTelefono.error = null // <-- CAMPO AÑADIDO

            // --- 3. INICIO DE VALIDACIONES ---

            // Validación de campos vacíos
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
            if (telefono.isEmpty()) { // <-- CAMPO AÑADIDO
                inputTelefono.error = "El teléfono no puede estar vacío"
                inputTelefono.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                inputPassword.error = "La contraseña no puede estar vacía"
                inputPassword.requestFocus()
                return@setOnClickListener
            }

            // Validación de Contraseña (tus reglas)
            if (password.length < 8) {
                inputPassword.error = "Debe tener al menos 8 caracteres"
                inputPassword.requestFocus()
                return@setOnClickListener
            }
            if (!password.any { it.isUpperCase() }) {
                inputPassword.error = "Debe incluir al menos una mayúscula"
                inputPassword.requestFocus()
                return@setOnClickListener
            }
            if (!password.any { !it.isLetterOrDigit() }) {
                inputPassword.error = "Debe incluir al menos un símbolo (ej: @, #, !)"
                inputPassword.requestFocus()
                return@setOnClickListener
            }

            // --- 4. FIN DE VALIDACIONES ---

            //Si todo está bien se insertar en la Base de datos
            val exito = dbHelper.insertarUsuario(nombre, email, password, pais, telefono) // <-- 'telefono' AÑADIDO

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
        dbHelper.close()
        super.onDestroy()
    }
}