package com.example.dvault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Referenciar los botones
        val btnComprar = findViewById<Button>(R.id.btnComprar)
        val btnVender = findViewById<Button>(R.id.btnVender)
        val btnBuscar = findViewById<Button>(R.id.btnBuscar)
        val btnListar = findViewById<Button>(R.id.btnListar)
        val btnFavoritos = findViewById<Button>(R.id.btnFavoritos)
        val btnPerfil = findViewById<Button>(R.id.btnPerfil)
        val btnSalir = findViewById<Button>(R.id.btnSalir)

        // Botón COMPRAR
        btnComprar.setOnClickListener {
            val intent = Intent(this, ComprarActivity::class.java)
            startActivity(intent)
        }

        // Botón VENDER
        btnVender.setOnClickListener {
            val intent = Intent(this, VenderActivity::class.java)
            startActivity(intent)
        }

        // Botón BUSCAR
        btnBuscar.setOnClickListener {
            val intent = Intent(this, BuscarActivity::class.java)
            startActivity(intent)
        }

        // Botón LISTAR
        btnListar.setOnClickListener {
            val intent = Intent(this, ListarActivity::class.java)
            startActivity(intent)
        }

        // Botón FAVORITOS
        btnFavoritos.setOnClickListener {
            val intent = Intent(this, FavoritosActivity::class.java)
            startActivity(intent)
        }

        // Botón PERFIL
        btnPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        // Botón SALIR - volver al login
        btnSalir.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}