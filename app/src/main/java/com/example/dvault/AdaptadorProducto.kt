package com.example.dvault

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import models.Producto

class AdaptadorProducto(
    private val contexto: Context,
    private val productos: List<Producto>,
    private val oyente: AlPulsarProductoListener
) : RecyclerView.Adapter<AdaptadorProducto.ProductoViewHolder>() {

    // Interfaz para manejar clicks
    interface AlPulsarProductoListener {
        fun alPulsarAgregarAlCarrito(producto: Producto)
    }

    inner class ProductoViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val imgProducto: ImageView = vista.findViewById(R.id.imgProducto)
        val tvMarca: TextView = vista.findViewById(R.id.tvMarca)
        val tvPrecio: TextView = vista.findViewById(R.id.tvPrecio)
        val tvUbicacion: TextView = vista.findViewById(R.id.tvUbicacion)
        val tvTalla: TextView = vista.findViewById(R.id.tvTalla)
        val btnAgregarCarrito: ImageButton = vista.findViewById(R.id.btnFavorito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val vista = LayoutInflater.from(contexto)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]

        holder.tvMarca.text = producto.marca
        holder.tvPrecio.text = "$${String.format("%.2f", producto.precio)}"
        holder.tvUbicacion.text = "Santiago, Chile"
        holder.tvTalla.text = "TU" // Talla única



        // Click en agregar al carrito
        holder.btnAgregarCarrito.setOnClickListener {
            oyente.alPulsarAgregarAlCarrito(producto)
        }

        // Click en el producto completo (para ver detalles)
        holder.itemView.setOnClickListener {
        }
    }

    override fun getItemCount(): Int = productos.size
}