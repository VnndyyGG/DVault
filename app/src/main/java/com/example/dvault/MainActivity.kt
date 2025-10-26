package com.example.dvault

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
            val cursor: Cursor? = dbHelper.getUserByEmail(email)

            if (cursor != null && cursor.count > 0) {
                cursor.moveToFirst()
                val storedPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"))

                if (password == storedPassword) {
                    // inicio de sesion
                    Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                    // Aquí puedes navegar a otra pantalla si lo deseas
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
            Toast.makeText(this, "Redirigiendo a registro...", Toast.LENGTH_SHORT).show()
            // Aquí iría la lógica para abrir la pantalla de registro
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