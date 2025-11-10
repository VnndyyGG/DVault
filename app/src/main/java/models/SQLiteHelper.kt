package models

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "MiBaseDeDatos.db"
        // CAMBIO: La versión ahora es 5
        private const val VERSION_BD = 5

        // Tabla Usuarios
        private const val TABLA_USUARIOS = "Usuarios"
        private const val COLUMNA_ID = "id"
        private const val COLUMNA_NOMBRE = "nombre"
        private const val COLUMNA_EMAIL = "email"
        private const val COLUMNA_PASSWORD = "password"
        private const val COLUMNA_PAIS = "pais"
        private const val COLUMNA_TELEFONO = "telefono" // <-- NUEVA COLUMNA

        // Tabla Productos (Sin cambios)
        private const val TABLA_PRODUCTOS = "Productos"
        private const val COLUMNA_PRODUCTO_ID = "producto_id"
        private const val COLUMNA_PRODUCTO_NOMBRE = "producto_nombre"
        private const val COLUMNA_PRODUCTO_MARCA = "producto_marca"
        private const val COLUMNA_PRODUCTO_PRECIO = "producto_precio"
        private const val COLUMNA_PRODUCTO_DESCRIPCION = "producto_descripcion"
        private const val COLUMNA_PRODUCTO_IMAGEN = "producto_imagen"
        private const val COLUMNA_VENDEDOR_ID = "vendedor_id"


        // Tabla Carrito (Sin cambios)
        private const val TABLA_CARRITO = "Carrito"
        private const val COLUMNA_CARRITO_ID = "carrito_id"
        private const val COLUMNA_USUARIO_ID = "usuario_id"
        private const val COLUMNA_CARRITO_PRODUCTO_ID = "producto_id"
        private const val COLUMNA_CANTIDAD = "cantidad"
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

        // Crear tabla Productos (Sin cambios)
        val crearTablaProductos = """
            CREATE TABLE $TABLA_PRODUCTOS (
                $COLUMNA_PRODUCTO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMNA_PRODUCTO_NOMBRE TEXT NOT NULL,
                $COLUMNA_PRODUCTO_MARCA TEXT NOT NULL,
                $COLUMNA_PRODUCTO_PRECIO REAL NOT NULL,
                $COLUMNA_PRODUCTO_DESCRIPCION TEXT,
                $COLUMNA_PRODUCTO_IMAGEN TEXT,
                $COLUMNA_VENDEDOR_ID INTEGER NOT NULL,
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

        db.execSQL(crearTablaUsuarios)
        db.execSQL(crearTablaProductos)
        db.execSQL(crearTablaCarrito)
    }

    // onUpgrade (ACTUALIZADO)
    // Al subir la versión, se borrarán las tablas viejas y se crearán las nuevas.
    override fun onUpgrade(db: SQLiteDatabase, versionAntigua: Int, versionNueva: Int) {
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
}