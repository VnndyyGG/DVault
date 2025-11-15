package com.example.dvault

import android.content.Context
import android.widget.Toast
import models.SQLiteHelper
import java.text.SimpleDateFormat
import java.util.*


fun agregarCalificacionesDePrueba(context: Context, vendedorId: Int) {
    val dbHelper = SQLiteHelper(context)

    // Formato de fecha
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())



    // Calificación 1: 5 estrellas
    dbHelper.insertarCalificacion(
        vendedorId = vendedorId,
        compradorId = 1, // ID del comprador (debe existir en la BD)
        estrellas = 5,
        comentario = "Excelente vendedor, el producto llegó en perfectas condiciones. Muy recomendado!",
        fecha = dateFormat.format(Date(System.currentTimeMillis() - 86400000)) // Hace 1 día
    )

    // Calificación 2: 5 estrellas
    dbHelper.insertarCalificacion(
        vendedorId = vendedorId,
        compradorId = 2,
        estrellas = 5,
        comentario = "Producto tal como se describe, envío rápido. Volveré a comprar sin duda.",
        fecha = dateFormat.format(Date(System.currentTimeMillis() - 172800000)) // Hace 2 días
    )

    // Calificación 3: 4 estrellas
    dbHelper.insertarCalificacion(
        vendedorId = vendedorId,
        compradorId = 3,
        estrellas = 4,
        comentario = "Buen producto, aunque tardó un poco en llegar. El vendedor fue amable.",
        fecha = dateFormat.format(Date(System.currentTimeMillis() - 259200000)) // Hace 3 días
    )

    // Calificación 4: 5 estrellas
    dbHelper.insertarCalificacion(
        vendedorId = vendedorId,
        compradorId = 1,
        estrellas = 5,
        comentario = "Perfecto! Muy profesional y responsable.",
        fecha = dateFormat.format(Date(System.currentTimeMillis() - 345600000)) // Hace 4 días
    )

    // Calificación 5: 3 estrellas
    dbHelper.insertarCalificacion(
        vendedorId = vendedorId,
        compradorId = 2,
        estrellas = 3,
        comentario = "El producto está bien pero no es exactamente como esperaba.",
        fecha = dateFormat.format(Date(System.currentTimeMillis() - 432000000)) // Hace 5 días
    )

    dbHelper.close()

    Toast.makeText(context, "Calificaciones de prueba agregadas", Toast.LENGTH_LONG).show()
}