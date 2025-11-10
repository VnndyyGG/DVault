package com.example.dvault

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.database.Cursor
import models.SQLiteHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnCreate = findViewById<Button>(R.id.btnCreate)
        val forgotText = findViewById<TextView>(R.id.forgot)
        val appleButton = findViewById<ImageButton>(R.id.appleButton)
        val googleButton = findViewById<ImageButton>(R.id.googleButton)

        btnLogin.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (email.isEmpty()) {
                inputEmail.error = "Ingresa tu correo"
                inputEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                inputPassword.error = "Ingresa tu contraseña"
                inputPassword.requestFocus()
                return@setOnClickListener
            }

            // verificar en la base de datos
            val dbHelper = SQLiteHelper(this)
            val cursor: Cursor? = dbHelper.obtenerUsuarioPorEmail(email)

            if (cursor != null && cursor.count > 0) {
                cursor.moveToFirst()
                val storedPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"))
                val userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val userName = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))

                // --- CAMBIO AQUÍ ---
                // Ahora obtenemos el país en lugar de 'esVendedor'
                val pais = cursor.getString(cursor.getColumnIndexOrThrow("pais"))

                if (password == storedPassword) {
                    // inicio de sesion exitoso
                    Toast.makeText(this, "¡Bienvenido $userName!", Toast.LENGTH_SHORT).show()

                    // --- CAMBIO AQUÍ ---
                    // Guardar datos del usuario en SharedPreferences
                    val sharedPref = getSharedPreferences("DVaultPrefs", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putInt("USUARIO_ID", userId)
                        putString("USUARIO_EMAIL", email)
                        putString("USUARIO_NOMBRE", userName)
                        putString("USUARIO_PAIS", pais) // <-- Guardamos el país
                        apply()
                    }

                    // Navegar al menú principal
                    val intent = Intent(this, MenuActivity::class.java)
                    startActivity(intent)
                    finish() // Cierra el login para que no pueda volver con el botón atrás

                } else {
                    // Contraseña incorrecta
                    inputPassword.error = "Contraseña incorrecta"
                    inputPassword.requestFocus()
                }
            } else {
                // Correo no registrado
                inputEmail.error = "El correo no existe. Crea una cuenta."
                inputEmail.requestFocus()
            }

            cursor?.close()
            dbHelper.close()
        }

        // Crear cuenta
        btnCreate.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }

        // Olvidaste tu contraseña?
        forgotText.setOnClickListener {
            Toast.makeText(this, "Restablecer contraseña", Toast.LENGTH_SHORT).show()
        }

        // Iniciar sesión con Apple
        appleButton.setOnClickListener {
            Toast.makeText(this, "Apple Sign-In no disponible en Android", Toast.LENGTH_SHORT).show()
        }

        // Iniciar sesión con Google
        googleButton.setOnClickListener {
            Toast.makeText(this, "Iniciar sesión con Google", Toast.LENGTH_SHORT).show()
        }
    }
}