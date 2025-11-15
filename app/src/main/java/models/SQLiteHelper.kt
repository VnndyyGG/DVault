package models

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "MiBaseDeDatos.db"
        private const val VERSION_BD = 7

        // Tabla Usuarios
        private const val TABLA_USUARIOS = "Usuarios"
        private const val COLUMNA_ID = "id"
        private const val COLUMNA_NOMBRE = "nombre"
        private const val COLUMNA_EMAIL = "email"
        private const val COLUMNA_PASSWORD = "password"
        private const val COLUMNA_PAIS = "pais"
        private const val COLUMNA_TELEFONO = "telefono"

        // Tabla Productos
        private const val TABLA_PRODUCTOS = "Productos"
        private const val COLUMNA_PRODUCTO_ID = "producto_id"
        private const val COLUMNA_PRODUCTO_NOMBRE = "producto_nombre"
        private const val COLUMNA_PRODUCTO_MARCA = "producto_marca"
        private const val COLUMNA_PRODUCTO_PRECIO = "producto_precio"
        private const val COLUMNA_PRODUCTO_DESCRIPCION = "producto_descripcion"
        private const val COLUMNA_PRODUCTO_IMAGEN = "producto_imagen"
        private const val COLUMNA_VENDEDOR_ID = "vendedor_id"
        private const val COLUMNA_PRECIO_OFERTA = "precio_oferta"
        private const val COLUMNA_FECHA_FIN_OFERTA = "fecha_fin_oferta"

        // Tabla Carrito
        private const val TABLA_CARRITO = "Carrito"
        private const val COLUMNA_CARRITO_ID = "carrito_id"
        private const val COLUMNA_USUARIO_ID = "usuario_id"
        private const val COLUMNA_CARRITO_PRODUCTO_ID = "producto_id"
        private const val COLUMNA_CANTIDAD = "cantidad"

        // Tabla Calificaciones
        private const val TABLA_CALIFICACIONES = "Calificaciones"
        private const val COLUMNA_CALIFICACION_ID = "calificacion_id"
        private const val COLUMNA_VENDEDOR_CALIFICADO_ID = "vendedor_id"
        private const val COLUMNA_COMPRADOR_ID = "comprador_id"
        private const val COLUMNA_ESTRELLAS = "estrellas"
        private const val COLUMNA_COMENTARIO = "comentario"
        private const val COLUMNA_FECHA = "fecha"

        // Tabla Favoritos
        private const val TABLA_FAVORITOS = "Favoritos"
        private const val COLUMNA_FAVORITO_ID = "favorito_id"
        private const val COLUMNA_FAVORITO_USUARIO_ID = "usuario_id"
        private const val COLUMNA_FAVORITO_PRODUCTO_ID = "producto_id"
        private const val COLUMNA_FECHA_AGREGADO = "fecha_agregado"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla Usuarios (ACTUALIZADA)
        val crearTablaUsuarios = """
            CREATE TABLE $TABLA_USUARIOS (
                $COLUMNA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMNA_NOMBRE TEXT NOT NULL,
                $COLUMNA_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMNA_PASSWORD TEXT NOT NULL,
                $COLUMNA_PAIS TEXT DEFAULT 'Chile',
                $COLUMNA_TELEFONO TEXT 
            )
        """.trimIndent() // <-- Columna 'telefono' añadida

        // Crear tabla Productos (ACTUALIZADA con campos de oferta)
        val crearTablaProductos = """
            CREATE TABLE $TABLA_PRODUCTOS (
                $COLUMNA_PRODUCTO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMNA_PRODUCTO_NOMBRE TEXT NOT NULL,
                $COLUMNA_PRODUCTO_MARCA TEXT NOT NULL,
                $COLUMNA_PRODUCTO_PRECIO REAL NOT NULL,
                $COLUMNA_PRODUCTO_DESCRIPCION TEXT,
                $COLUMNA_PRODUCTO_IMAGEN TEXT,
                $COLUMNA_VENDEDOR_ID INTEGER NOT NULL,
                $COLUMNA_PRECIO_OFERTA REAL,
                $COLUMNA_FECHA_FIN_OFERTA TEXT,
                FOREIGN KEY ($COLUMNA_VENDEDOR_ID) REFERENCES $TABLA_USUARIOS($COLUMNA_ID)
            )
        """.trimIndent()

        // Crear tabla Carrito (Sin cambios)
        val crearTablaCarrito = """
            CREATE TABLE $TABLA_CARRITO (
                $COLUMNA_CARRITO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMNA_USUARIO_ID INTEGER NOT NULL,
                $COLUMNA_CARRITO_PRODUCTO_ID INTEGER NOT NULL,
                $COLUMNA_CANTIDAD INTEGER DEFAULT 1,
                FOREIGN KEY ($COLUMNA_USUARIO_ID) REFERENCES $TABLA_USUARIOS($COLUMNA_ID),
                FOREIGN KEY ($COLUMNA_CARRITO_PRODUCTO_ID) REFERENCES $TABLA_PRODUCTOS($COLUMNA_PRODUCTO_ID)
            )
        """.trimIndent()

        // Crear tabla Calificaciones
        val crearTablaCalificaciones = """
            CREATE TABLE $TABLA_CALIFICACIONES (
                $COLUMNA_CALIFICACION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMNA_VENDEDOR_CALIFICADO_ID INTEGER NOT NULL,
                $COLUMNA_COMPRADOR_ID INTEGER NOT NULL,
                $COLUMNA_ESTRELLAS INTEGER NOT NULL CHECK($COLUMNA_ESTRELLAS >= 1 AND $COLUMNA_ESTRELLAS <= 5),
                $COLUMNA_COMENTARIO TEXT,
                $COLUMNA_FECHA TEXT NOT NULL,
                FOREIGN KEY ($COLUMNA_VENDEDOR_CALIFICADO_ID) REFERENCES $TABLA_USUARIOS($COLUMNA_ID),
                FOREIGN KEY ($COLUMNA_COMPRADOR_ID) REFERENCES $TABLA_USUARIOS($COLUMNA_ID)
            )
        """.trimIndent()

        // Crear tabla Favoritos
        val crearTablaFavoritos = """
            CREATE TABLE $TABLA_FAVORITOS (
                $COLUMNA_FAVORITO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMNA_FAVORITO_USUARIO_ID INTEGER NOT NULL,
                $COLUMNA_FAVORITO_PRODUCTO_ID INTEGER NOT NULL,
                $COLUMNA_FECHA_AGREGADO TEXT NOT NULL,
                FOREIGN KEY ($COLUMNA_FAVORITO_USUARIO_ID) REFERENCES $TABLA_USUARIOS($COLUMNA_ID),
                FOREIGN KEY ($COLUMNA_FAVORITO_PRODUCTO_ID) REFERENCES $TABLA_PRODUCTOS($COLUMNA_PRODUCTO_ID),
                UNIQUE($COLUMNA_FAVORITO_USUARIO_ID, $COLUMNA_FAVORITO_PRODUCTO_ID)
            )
        """.trimIndent()

        db.execSQL(crearTablaUsuarios)
        db.execSQL(crearTablaProductos)
        db.execSQL(crearTablaCarrito)
        db.execSQL(crearTablaCalificaciones)
        db.execSQL(crearTablaFavoritos)
    }

    // onUpgrade (ACTUALIZADO)
    // Al subir la versión, se borrarán las tablas viejas y se crearán las nuevas.
    override fun onUpgrade(db: SQLiteDatabase, versionAntigua: Int, versionNueva: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_FAVORITOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_CALIFICACIONES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_CARRITO")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_PRODUCTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        onCreate(db)
    }

    // ========== FUNCIONES DE USUARIOS ==========

    // obtenerUsuarioPorEmail (Sin cambios)
    fun obtenerUsuarioPorEmail(email: String): Cursor? {
        val db = this.readableDatabase
        return db.query(
            TABLA_USUARIOS,
            null,
            "$COLUMNA_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
    }

    // obtenerUsuarioPorId (Sin cambios)
    fun obtenerUsuarioPorId(usuarioId: Int): Cursor? {
        val db = this.readableDatabase
        return db.query(
            TABLA_USUARIOS,
            null,
            "$COLUMNA_ID = ?",
            arrayOf(usuarioId.toString()),
            null,
            null,
            null
        )
    }

    // --- insertarUsuario (ACTUALIZADA) ---
    fun insertarUsuario(nombre: String, email: String, password: String, pais: String, telefono: String): Long {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(COLUMNA_NOMBRE, nombre)
            put(COLUMNA_EMAIL, email)
            put(COLUMNA_PASSWORD, password)
            put(COLUMNA_PAIS, pais)
            put(COLUMNA_TELEFONO, telefono) // <-- AÑADIDO
        }
        return db.insert(TABLA_USUARIOS, null, valores)
    }

    // ========== FUNCIONES DE PRODUCTOS ==========
    // (Todas sin cambios)
    fun insertarProducto(
        nombre: String,
        marca: String,
        precio: Double,
        descripcion: String,
        imagen: String,
        vendedorId: Int
    ): Long {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(COLUMNA_PRODUCTO_NOMBRE, nombre)
            put(COLUMNA_PRODUCTO_MARCA, marca)
            put(COLUMNA_PRODUCTO_PRECIO, precio)
            put(COLUMNA_PRODUCTO_DESCRIPCION, descripcion)
            put(COLUMNA_PRODUCTO_IMAGEN, imagen)
            put(COLUMNA_VENDEDOR_ID, vendedorId)
        }
        return db.insert(TABLA_PRODUCTOS, null, valores)
    }

    fun obtenerTodosLosProductos(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLA_PRODUCTOS", null)
    }
    fun obtenerProductoPorId(productoId: Int): Cursor? {
        val db = this.readableDatabase
        return db.query(
            TABLA_PRODUCTOS,
            null,
            "$COLUMNA_PRODUCTO_ID = ?",
            arrayOf(productoId.toString()),
            null,
            null,
            null
        )
    }
    fun obtenerProductosPorVendedor(vendedorId: Int): Cursor? {
        val db = this.readableDatabase
        return db.query(
            TABLA_PRODUCTOS,
            null,
            "$COLUMNA_VENDEDOR_ID = ?",
            arrayOf(vendedorId.toString()),
            null,
            null,
            null
        )
    }

    // ========== FUNCIONES DE CARRITO ==========

    // agregarAlCarrito (Sin cambios)
    fun agregarAlCarrito(usuarioId: Int, productoId: Int, cantidad: Int = 1): Long {
        val db = this.writableDatabase
        val cursor = db.query(
            TABLA_CARRITO,
            null,
            "$COLUMNA_USUARIO_ID = ? AND $COLUMNA_CARRITO_PRODUCTO_ID = ?",
            arrayOf(usuarioId.toString(), productoId.toString()),
            null,
            null,
            null
        )
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            val cantidadActual = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMNA_CANTIDAD))
            val carritoId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMNA_CARRITO_ID))
            cursor.close()
            val valores = ContentValues().apply {
                put(COLUMNA_CANTIDAD, cantidadActual + cantidad)
            }
            return db.update(
                TABLA_CARRITO,
                valores,
                "$COLUMNA_CARRITO_ID = ?",
                arrayOf(carritoId.toString())
            ).toLong()
        } else {
            cursor?.close()
            val valores = ContentValues().apply {
                put(COLUMNA_USUARIO_ID, usuarioId)
                put(COLUMNA_CARRITO_PRODUCTO_ID, productoId)
                put(COLUMNA_CANTIDAD, cantidad)
            }
            return db.insert(TABLA_CARRITO, null, valores)
        }
    }

    // obtenerProductosCarrito (ACTUALIZADA)
    fun obtenerProductosCarrito(usuarioId: Int): Cursor? {
        val db = this.readableDatabase
        val consulta = """
            SELECT 
                c.$COLUMNA_CARRITO_ID,
                c.$COLUMNA_CANTIDAD,
                p.$COLUMNA_PRODUCTO_ID,
                p.$COLUMNA_PRODUCTO_NOMBRE,
                p.$COLUMNA_PRODUCTO_MARCA,
                p.$COLUMNA_PRODUCTO_PRECIO,
                p.$COLUMNA_PRODUCTO_IMAGEN,
                p.$COLUMNA_PRODUCTO_DESCRIPCION,
                p.$COLUMNA_VENDEDOR_ID,
                u.$COLUMNA_NOMBRE as nombre_vendedor,
                u.$COLUMNA_EMAIL as email_vendedor,
                u.$COLUMNA_PAIS as pais_vendedor 
            FROM $TABLA_CARRITO c
            INNER JOIN $TABLA_PRODUCTOS p ON c.$COLUMNA_CARRITO_PRODUCTO_ID = p.$COLUMNA_PRODUCTO_ID
            INNER JOIN $TABLA_USUARIOS u ON p.$COLUMNA_VENDEDOR_ID = u.$COLUMNA_ID
            WHERE c.$COLUMNA_USUARIO_ID = ?
        """.trimIndent()

        return db.rawQuery(consulta, arrayOf(usuarioId.toString()))
    }

    // (El resto de funciones de carrito sin cambios)
    fun eliminarDelCarrito(carritoId: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLA_CARRITO, "$COLUMNA_CARRITO_ID = ?", arrayOf(carritoId.toString()))
    }
    fun vaciarCarrito(usuarioId: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLA_CARRITO, "$COLUMNA_USUARIO_ID = ?", arrayOf(usuarioId.toString()))
    }
    fun obtenerCantidadProductosCarrito(usuarioId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMNA_CANTIDAD) as total FROM $TABLA_CARRITO WHERE $COLUMNA_USUARIO_ID = ?",
            arrayOf(usuarioId.toString())
        )
        var cantidad = 0
        if (cursor.moveToFirst()) {
            cantidad = cursor.getInt(0)
        }
        cursor.close()
        return cantidad
    }

    // ========== FUNCIONES DE CALIFICACIONES ==========

    // Insertar una calificación
    fun insertarCalificacion(
        vendedorId: Int,
        compradorId: Int,
        estrellas: Int,
        comentario: String,
        fecha: String
    ): Long {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(COLUMNA_VENDEDOR_CALIFICADO_ID, vendedorId)
            put(COLUMNA_COMPRADOR_ID, compradorId)
            put(COLUMNA_ESTRELLAS, estrellas)
            put(COLUMNA_COMENTARIO, comentario)
            put(COLUMNA_FECHA, fecha)
        }
        return db.insert(TABLA_CALIFICACIONES, null, valores)
    }

    // Obtener todas las calificaciones de un vendedor
    fun obtenerCalificacionesVendedor(vendedorId: Int): Cursor? {
        val db = this.readableDatabase
        val consulta = """
            SELECT 
                c.$COLUMNA_CALIFICACION_ID,
                c.$COLUMNA_ESTRELLAS,
                c.$COLUMNA_COMENTARIO,
                c.$COLUMNA_FECHA,
                u.$COLUMNA_NOMBRE as nombre_comprador
            FROM $TABLA_CALIFICACIONES c
            INNER JOIN $TABLA_USUARIOS u ON c.$COLUMNA_COMPRADOR_ID = u.$COLUMNA_ID
            WHERE c.$COLUMNA_VENDEDOR_CALIFICADO_ID = ?
            ORDER BY c.$COLUMNA_FECHA DESC
        """.trimIndent()
        return db.rawQuery(consulta, arrayOf(vendedorId.toString()))
    }

    // Obtener promedio de calificaciones de un vendedor
    fun obtenerPromedioCalificaciones(vendedorId: Int): Double {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT AVG($COLUMNA_ESTRELLAS) as promedio FROM $TABLA_CALIFICACIONES WHERE $COLUMNA_VENDEDOR_CALIFICADO_ID = ?",
            arrayOf(vendedorId.toString())
        )
        var promedio = 0.0
        if (cursor.moveToFirst()) {
            promedio = cursor.getDouble(0)
        }
        cursor.close()
        return promedio
    }

    // Obtener cantidad total de calificaciones de un vendedor
    fun obtenerCantidadCalificaciones(vendedorId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) as total FROM $TABLA_CALIFICACIONES WHERE $COLUMNA_VENDEDOR_CALIFICADO_ID = ?",
            arrayOf(vendedorId.toString())
        )
        var cantidad = 0
        if (cursor.moveToFirst()) {
            cantidad = cursor.getInt(0)
        }
        cursor.close()
        return cantidad
    }

    // ========== FUNCIONES DE FAVORITOS ==========

    // Agregar producto a favoritos
    fun agregarAFavoritos(usuarioId: Int, productoId: Int): Long {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(COLUMNA_FAVORITO_USUARIO_ID, usuarioId)
            put(COLUMNA_FAVORITO_PRODUCTO_ID, productoId)
            put(COLUMNA_FECHA_AGREGADO, System.currentTimeMillis().toString())
        }
        return db.insert(TABLA_FAVORITOS, null, valores)
    }

    // Eliminar de favoritos
    fun eliminarDeFavoritos(usuarioId: Int, productoId: Int): Int {
        val db = this.writableDatabase
        return db.delete(
            TABLA_FAVORITOS,
            "$COLUMNA_FAVORITO_USUARIO_ID = ? AND $COLUMNA_FAVORITO_PRODUCTO_ID = ?",
            arrayOf(usuarioId.toString(), productoId.toString())
        )
    }

    // Verificar si un producto está en favoritos
    fun esFavorito(usuarioId: Int, productoId: Int): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLA_FAVORITOS,
            null,
            "$COLUMNA_FAVORITO_USUARIO_ID = ? AND $COLUMNA_FAVORITO_PRODUCTO_ID = ?",
            arrayOf(usuarioId.toString(), productoId.toString()),
            null,
            null,
            null
        )
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    // Obtener todos los favoritos de un usuario
    fun obtenerFavoritos(usuarioId: Int): Cursor? {
        val db = this.readableDatabase
        val consulta = """
            SELECT 
                p.*,
                u.$COLUMNA_NOMBRE as nombre_vendedor
            FROM $TABLA_FAVORITOS f
            INNER JOIN $TABLA_PRODUCTOS p ON f.$COLUMNA_FAVORITO_PRODUCTO_ID = p.$COLUMNA_PRODUCTO_ID
            INNER JOIN $TABLA_USUARIOS u ON p.$COLUMNA_VENDEDOR_ID = u.$COLUMNA_ID
            WHERE f.$COLUMNA_FAVORITO_USUARIO_ID = ?
            ORDER BY f.$COLUMNA_FECHA_AGREGADO DESC
        """.trimIndent()
        return db.rawQuery(consulta, arrayOf(usuarioId.toString()))
    }

    // ========== FUNCIONES DE OFERTAS ==========

    // Actualizar producto (con posibilidad de agregar oferta)
    fun actualizarProducto(
        productoId: Int,
        nombre: String,
        marca: String,
        precio: Double,
        descripcion: String,
        imagen: String,
        precioOferta: Double? = null,
        fechaFinOferta: String? = null
    ): Int {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(COLUMNA_PRODUCTO_NOMBRE, nombre)
            put(COLUMNA_PRODUCTO_MARCA, marca)
            put(COLUMNA_PRODUCTO_PRECIO, precio)
            put(COLUMNA_PRODUCTO_DESCRIPCION, descripcion)
            put(COLUMNA_PRODUCTO_IMAGEN, imagen)
            if (precioOferta != null) {
                put(COLUMNA_PRECIO_OFERTA, precioOferta)
            }
            if (fechaFinOferta != null) {
                put(COLUMNA_FECHA_FIN_OFERTA, fechaFinOferta)
            }
        }
        return db.update(
            TABLA_PRODUCTOS,
            valores,
            "$COLUMNA_PRODUCTO_ID = ?",
            arrayOf(productoId.toString())
        )
    }

    // Crear oferta temporal
    fun crearOferta(productoId: Int, precioOferta: Double, fechaFinOferta: String): Int {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            put(COLUMNA_PRECIO_OFERTA, precioOferta)
            put(COLUMNA_FECHA_FIN_OFERTA, fechaFinOferta)
        }
        return db.update(
            TABLA_PRODUCTOS,
            valores,
            "$COLUMNA_PRODUCTO_ID = ?",
            arrayOf(productoId.toString())
        )
    }

    // Eliminar oferta (cuando expira)
    fun eliminarOferta(productoId: Int): Int {
        val db = this.writableDatabase
        val valores = ContentValues().apply {
            putNull(COLUMNA_PRECIO_OFERTA)
            putNull(COLUMNA_FECHA_FIN_OFERTA)
        }
        return db.update(
            TABLA_PRODUCTOS,
            valores,
            "$COLUMNA_PRODUCTO_ID = ?",
            arrayOf(productoId.toString())
        )
    }

    // Verificar si oferta está activa
    fun ofertaActiva(productoId: Int): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLA_PRODUCTOS,
            arrayOf(COLUMNA_FECHA_FIN_OFERTA),
            "$COLUMNA_PRODUCTO_ID = ?",
            arrayOf(productoId.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val fechaFin = cursor.getString(0)
            cursor.close()
            if (fechaFin != null) {
                val fechaFinMillis = fechaFin.toLongOrNull() ?: return false
                return System.currentTimeMillis() < fechaFinMillis
            }
        } else {
            cursor.close()
        }
        return false
    }
}