package com.example.dvault

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ImagenProductoAdapter(private val rutas: List<String>) :
    RecyclerView.Adapter<ImagenProductoAdapter.ImagenViewHolder>() {

    inner class ImagenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImagen: ImageView = itemView.findViewById(R.id.ivImagen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_imagen_producto, parent, false)
        return ImagenViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImagenViewHolder, position: Int) {
        val rutaImagen = rutas[position]
        if (rutaImagen.isNotEmpty()) {
            val archivo = File(rutaImagen)
            if (archivo.exists()) {
                holder.ivImagen.setImageURI(Uri.fromFile(archivo))
            }
        }
    }

    override fun getItemCount(): Int = rutas.size
}