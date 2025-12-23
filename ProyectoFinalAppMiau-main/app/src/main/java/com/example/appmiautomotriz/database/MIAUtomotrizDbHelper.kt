package com.example.appmiautomotriz.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.ArrayList
import com.example.appmiautomotriz.model.OrdenDeTrabajo
import com.example.appmiautomotriz.model.Cliente
import com.example.appmiautomotriz.model.Repuesto
import com.example.appmiautomotriz.model.Factura
import com.example.appmiautomotriz.model.Cotizacion
import com.example.appmiautomotriz.model.Cita


class MIAUtomotrizDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Configuración de la DB
        private const val DATABASE_VERSION = 8
        private const val DATABASE_NAME = "MIAUtomotriz.db"

        // --- TABLA ORDEN DE TRABAJO ---
        const val TABLE_ORDEN_TRABAJO = "OrdenDeTrabajo"
        const val COL_NUMERO_ORDEN = "numeroOrdenDeTrabajo"
        const val COL_FECHA_ORDEN = "fechaOrdenDeTrabajo"
        const val COL_ESTADO_ORDEN = "estado"
        const val COL_OBSERVACIONES_ORDEN = "ObservacionesOrdenDeTrabajo"
        const val COL_PATENTE_ORDEN = "patente"
        const val COL_ID_SEGURO_ORDEN = "IDSeguro"
        const val COL_ID_CLIENTE_ORDEN = "IDCliente"

        // --- TABLA CAUSA AVERIA ---
        const val TABLE_CAUSA_AVERIA = "CausaAveria"
        const val COL_CODIGO_CAUSA = "codigoCausaAveria"
        const val COL_NOMBRE_CAUSA = "nombreCausaAveria"
        const val COL_DESC_CAUSA = "descripcion"

        // --- TABLA CLIENTE ---
        const val TABLE_CLIENTE = "Cliente"
        const val COL_ID_CLIENTE = "idCliente"
        const val COL_NOMBRE_CLIENTE = "nombre"
        const val COL_TELEFONO_CLIENTE = "telefono"
        const val COL_EMAIL_CLIENTE = "email"

        // --- TABLA REPUESTOS (INVENTARIO) ---
        const val TABLE_REPUESTO = "Repuesto"
        const val COL_ID_REPUESTO = "idRepuesto"
        const val COL_NOMBRE_REPUESTO = "nombreRepuesto"
        const val COL_DESC_REPUESTO = "descripcionRepuesto"
        const val COL_STOCK_REPUESTO = "stock"
        const val COL_PRECIO_REPUESTO = "precio"

        // --- TABLA FACTURAS ---
        const val TABLE_FACTURA = "Factura"
        const val COL_ID_FACTURA = "idFactura"
        const val COL_CODIGO_FACTURA = "codigoFactura"
        const val COL_ID_CLIENTE_FACTURA = "idClienteFactura"
        const val COL_ID_ORDEN_FACTURA = "idOrdenFactura"
        const val COL_FECHA_FACTURA = "fechaFactura"
        const val COL_MONTO_FACTURA = "montoTotal"
        const val COL_ESTADO_FACTURA = "estadoFactura"

        // --- TABLA COTIZACIONES ---
        const val TABLE_COTIZACION = "Cotizacion"
        const val COL_ID_COTIZACION = "idCotizacion"
        const val COL_CODIGO_COTIZACION = "codigoCotizacion"
        const val COL_ID_CLIENTE_COTIZACION = "idClienteCotizacion"
        const val COL_PATENTE_COTIZACION = "patenteCotizacion"
        const val COL_FECHA_COTIZACION = "fechaCotizacion"
        const val COL_MONTO_COTIZACION = "montoCotizacion"
        const val COL_ESTADO_COTIZACION = "estadoCotizacion"

        // --- TABLA CITAS (AGENDAMIENTO) ---
        const val TABLE_CITA = "Cita"
        const val COL_ID_CITA = "idCita"
        const val COL_ID_CLIENTE_CITA = "idClienteCita"
        const val COL_PATENTE_CITA = "patenteCita"
        const val COL_FECHA_CITA = "fechaCita"
        const val COL_HORA_CITA = "horaCita"
        const val COL_TIPO_CITA = "tipoCita"
        const val COL_ESTADO_CITA = "estadoCita"

        // --- TABLAS DE RELACIÓN ---
        const val TABLE_ORDEN_REPUESTO = "OrdenRepuesto"
        const val COL_ID_ORDEN_REP = "idOrdenRepuesto" // PK
        const val COL_OR_ID_ORDEN = "idOrdenRel"
        const val COL_OR_ID_REPUESTO = "idRepuestoRel"
        const val COL_OR_CANTIDAD = "cantidadRepuesto"

        const val TABLE_ORDEN_CAUSA = "OrdenCausa"
        const val COL_ID_ORDEN_CAUSA = "idOrdenCausa" // PK
        const val COL_OC_ID_ORDEN = "idOrdenRelCausa"
        const val COL_OC_ID_CAUSA = "idCausaRel"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Orden de Trabajo
        val CREATE_TABLE_ORDEN_TRABAJO = (
                "CREATE TABLE $TABLE_ORDEN_TRABAJO ("
                        + "$COL_NUMERO_ORDEN INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "$COL_FECHA_ORDEN DATE NOT NULL DEFAULT CURRENT_DATE,"
                        + "$COL_ESTADO_ORDEN VARCHAR(20) NOT NULL DEFAULT 'Pendiente',"
                        + "$COL_OBSERVACIONES_ORDEN TEXT,"
                        + "$COL_PATENTE_ORDEN VARCHAR(6) NOT NULL,"
                        + "$COL_ID_SEGURO_ORDEN INT,"
                        + "$COL_ID_CLIENTE_ORDEN INT)"
                )
        db.execSQL(CREATE_TABLE_ORDEN_TRABAJO)

        // 2. Causa Avería
        val CREATE_TABLE_CAUSA_AVERIA = (
                "CREATE TABLE $TABLE_CAUSA_AVERIA ("
                        + "$COL_CODIGO_CAUSA INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "$COL_NOMBRE_CAUSA VARCHAR(100) NOT NULL,"
                        + "$COL_DESC_CAUSA TEXT)"
                )
        db.execSQL(CREATE_TABLE_CAUSA_AVERIA)

        // 3. Cliente
        val CREATE_TABLE_CLIENTE = (
                "CREATE TABLE $TABLE_CLIENTE ("
                        + "$COL_ID_CLIENTE INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "$COL_NOMBRE_CLIENTE VARCHAR(100) NOT NULL,"
                        + "$COL_TELEFONO_CLIENTE VARCHAR(20),"
                        + "$COL_EMAIL_CLIENTE VARCHAR(100))"
                )
        db.execSQL(CREATE_TABLE_CLIENTE)

        // 4. Repuestos
        val CREATE_TABLE_REPUESTO = (
                "CREATE TABLE $TABLE_REPUESTO ("
                        + "$COL_ID_REPUESTO INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "$COL_NOMBRE_REPUESTO VARCHAR(100) NOT NULL,"
                        + "$COL_DESC_REPUESTO TEXT,"
                        + "$COL_STOCK_REPUESTO INTEGER DEFAULT 0,"
                        + "$COL_PRECIO_REPUESTO INTEGER DEFAULT 0)"
                )
        db.execSQL(CREATE_TABLE_REPUESTO)

        // 5. Facturas
        val CREATE_TABLE_FACTURA = (
                "CREATE TABLE $TABLE_FACTURA ("
                        + "$COL_ID_FACTURA INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "$COL_CODIGO_FACTURA VARCHAR(50) UNIQUE,"
                        + "$COL_ID_CLIENTE_FACTURA INTEGER,"
                        + "$COL_ID_ORDEN_FACTURA INTEGER,"
                        + "$COL_FECHA_FACTURA DATE,"
                        + "$COL_MONTO_FACTURA INTEGER,"
                        + "$COL_ESTADO_FACTURA VARCHAR(20))"
                )
        db.execSQL(CREATE_TABLE_FACTURA)

        // 6. Cotizaciones
        val CREATE_TABLE_COTIZACION = (
                "CREATE TABLE $TABLE_COTIZACION ("
                        + "$COL_ID_COTIZACION INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "$COL_CODIGO_COTIZACION VARCHAR(50) UNIQUE,"
                        + "$COL_ID_CLIENTE_COTIZACION INTEGER,"
                        + "$COL_PATENTE_COTIZACION VARCHAR(10),"
                        + "$COL_FECHA_COTIZACION DATE,"
                        + "$COL_MONTO_COTIZACION INTEGER,"
                        + "$COL_ESTADO_COTIZACION VARCHAR(20))"
                )
        db.execSQL(CREATE_TABLE_COTIZACION)

        // 7. Citas
        val CREATE_TABLE_CITA = (
                "CREATE TABLE $TABLE_CITA ("
                        + "$COL_ID_CITA INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "$COL_ID_CLIENTE_CITA INTEGER,"
                        + "$COL_PATENTE_CITA VARCHAR(10),"
                        + "$COL_FECHA_CITA DATE,"
                        + "$COL_HORA_CITA VARCHAR(10),"
                        + "$COL_TIPO_CITA VARCHAR(50),"
                        + "$COL_ESTADO_CITA VARCHAR(20))"
                )
        db.execSQL(CREATE_TABLE_CITA)

        // 8. Orden - Repuesto (Relación)
        val CREATE_TABLE_ORDEN_REPUESTO = (
                "CREATE TABLE $TABLE_ORDEN_REPUESTO ("
                        + "$COL_ID_ORDEN_REP INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "$COL_OR_ID_ORDEN INTEGER,"
                        + "$COL_OR_ID_REPUESTO INTEGER,"
                        + "$COL_OR_CANTIDAD INTEGER DEFAULT 1)"
                )
        db.execSQL(CREATE_TABLE_ORDEN_REPUESTO)

        // 9. Orden - Causa (Relación)
        val CREATE_TABLE_ORDEN_CAUSA = (
                "CREATE TABLE $TABLE_ORDEN_CAUSA ("
                        + "$COL_ID_ORDEN_CAUSA INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "$COL_OC_ID_ORDEN INTEGER,"
                        + "$COL_OC_ID_CAUSA INTEGER)"
                )
        db.execSQL(CREATE_TABLE_ORDEN_CAUSA)

        insertarDatosIniciales(db)
    }

    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        db.execSQL("INSERT INTO CausaAveria (nombreCausaAveria, descripcion) VALUES ('Accidente', 'Daño por colisión/impacto');")
        db.execSQL("INSERT INTO CausaAveria (nombreCausaAveria, descripcion) VALUES('Frenos', 'Rendimiento deficiente');")
        db.execSQL("INSERT INTO CausaAveria (nombreCausaAveria, descripcion) VALUES ('Eléctrico', 'Problema con el circuito');")
        
        // Repuestos de prueba
        db.execSQL("INSERT INTO $TABLE_REPUESTO ($COL_NOMBRE_REPUESTO, $COL_STOCK_REPUESTO, $COL_PRECIO_REPUESTO) VALUES ('Filtro de Aceite', 50, 15000);")
        db.execSQL("INSERT INTO $TABLE_REPUESTO ($COL_NOMBRE_REPUESTO, $COL_STOCK_REPUESTO, $COL_PRECIO_REPUESTO) VALUES ('Pastillas de Freno', 20, 45000);")
        db.execSQL("INSERT INTO $TABLE_REPUESTO ($COL_NOMBRE_REPUESTO, $COL_STOCK_REPUESTO, $COL_PRECIO_REPUESTO) VALUES ('Batería 12V', 10, 85000);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            // Crear tablas si no existen
            val tablas = listOf(
                "CREATE TABLE IF NOT EXISTS $TABLE_REPUESTO ($COL_ID_REPUESTO INTEGER PRIMARY KEY AUTOINCREMENT, $COL_NOMBRE_REPUESTO VARCHAR(100) NOT NULL, $COL_DESC_REPUESTO TEXT, $COL_STOCK_REPUESTO INTEGER DEFAULT 0, $COL_PRECIO_REPUESTO INTEGER DEFAULT 0)",
                "CREATE TABLE IF NOT EXISTS $TABLE_FACTURA ($COL_ID_FACTURA INTEGER PRIMARY KEY AUTOINCREMENT, $COL_CODIGO_FACTURA VARCHAR(50) UNIQUE, $COL_ID_CLIENTE_FACTURA INTEGER, $COL_ID_ORDEN_FACTURA INTEGER, $COL_FECHA_FACTURA DATE, $COL_MONTO_FACTURA INTEGER, $COL_ESTADO_FACTURA VARCHAR(20))",
                "CREATE TABLE IF NOT EXISTS $TABLE_COTIZACION ($COL_ID_COTIZACION INTEGER PRIMARY KEY AUTOINCREMENT, $COL_CODIGO_COTIZACION VARCHAR(50) UNIQUE, $COL_ID_CLIENTE_COTIZACION INTEGER, $COL_PATENTE_COTIZACION VARCHAR(10), $COL_FECHA_COTIZACION DATE, $COL_MONTO_COTIZACION INTEGER, $COL_ESTADO_COTIZACION VARCHAR(20))",
                "CREATE TABLE IF NOT EXISTS $TABLE_CITA ($COL_ID_CITA INTEGER PRIMARY KEY AUTOINCREMENT, $COL_ID_CLIENTE_CITA INTEGER, $COL_PATENTE_CITA VARCHAR(10), $COL_FECHA_CITA DATE, $COL_HORA_CITA VARCHAR(10), $COL_TIPO_CITA VARCHAR(50), $COL_ESTADO_CITA VARCHAR(20))"
            )
            for (sql in tablas) {
                db.execSQL(sql)
            }
            
            // Insertar datos si la tabla Repuesto está vacía
            val cursor = db.rawQuery("SELECT count(*) FROM $TABLE_REPUESTO", null)
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                 db.execSQL("INSERT INTO $TABLE_REPUESTO ($COL_NOMBRE_REPUESTO, $COL_STOCK_REPUESTO, $COL_PRECIO_REPUESTO) VALUES ('Filtro de Aceite', 50, 15000);")
                 db.execSQL("INSERT INTO $TABLE_REPUESTO ($COL_NOMBRE_REPUESTO, $COL_STOCK_REPUESTO, $COL_PRECIO_REPUESTO) VALUES ('Pastillas de Freno', 20, 45000);")
            }
            cursor.close()
        }
        
        if (oldVersion < 6) {
            // Migración a versión 6
            val tablasV6 = listOf(
                "CREATE TABLE IF NOT EXISTS $TABLE_ORDEN_REPUESTO ($COL_ID_ORDEN_REP INTEGER PRIMARY KEY AUTOINCREMENT, $COL_OR_ID_ORDEN INTEGER, $COL_OR_ID_REPUESTO INTEGER, $COL_OR_CANTIDAD INTEGER DEFAULT 1)",
                "CREATE TABLE IF NOT EXISTS $TABLE_ORDEN_CAUSA ($COL_ID_ORDEN_CAUSA INTEGER PRIMARY KEY AUTOINCREMENT, $COL_OC_ID_ORDEN INTEGER, $COL_OC_ID_CAUSA INTEGER)"
            )
            for (sql in tablasV6) {
                db.execSQL(sql)
            }
        }
    }

    // --- MÉTODOS ORDEN DE TRABAJO ---
    fun guardarOrden(orden: OrdenDeTrabajo) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_FECHA_ORDEN, orden.fechaOrdenDeTrabajo)
            put(COL_ESTADO_ORDEN, orden.estado)
            put(COL_OBSERVACIONES_ORDEN, orden.observaciones)
            put(COL_PATENTE_ORDEN, orden.patente)
            put(COL_ID_SEGURO_ORDEN, orden.idSeguro)
            put(COL_ID_CLIENTE_ORDEN, orden.idCliente)
        }
        db.insert(TABLE_ORDEN_TRABAJO, null, values)
        db.close()
    }

    fun leerTodasLasOrdenes(): ArrayList<OrdenDeTrabajo> {
        return leerOrdenesGenerico(null, null)
    }

    fun leerOrdenesPorEstado(estadoBusqueda: String): ArrayList<OrdenDeTrabajo> {
        return leerOrdenesGenerico("$COL_ESTADO_ORDEN = ?", arrayOf(estadoBusqueda))
    }

    fun leerOrdenesPendientes(): ArrayList<OrdenDeTrabajo> {
        return leerOrdenesGenerico("$COL_ESTADO_ORDEN != 'Finalizada' AND $COL_ESTADO_ORDEN != 'Eliminada'", null)
    }

    fun leerOrdenesFinalizadas(): ArrayList<OrdenDeTrabajo> {
        return leerOrdenesPorEstado("Finalizada")
    }

    fun leerOrdenPorId(idOrden: Int): OrdenDeTrabajo? {
        val lista = leerOrdenesGenerico("$COL_NUMERO_ORDEN = ?", arrayOf(idOrden.toString()))
        return if (lista.isNotEmpty()) lista[0] else null
    }

    fun leerOrdenesPorPatente(patente: String): ArrayList<OrdenDeTrabajo> {
        return leerOrdenesGenerico("$COL_PATENTE_ORDEN LIKE ?", arrayOf("%$patente%"))
    }

    private fun leerOrdenesGenerico(selection: String?, selectionArgs: Array<String>?): ArrayList<OrdenDeTrabajo> {
        val listaOrdenes = ArrayList<OrdenDeTrabajo>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_ORDEN_TRABAJO, null, selection, selectionArgs, null, null, "$COL_NUMERO_ORDEN DESC")

        if (cursor.moveToFirst()) {
            do {
                val orden = OrdenDeTrabajo(
                    numeroOrdenDeTrabajo = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NUMERO_ORDEN)),
                    fechaOrdenDeTrabajo = cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA_ORDEN)),
                    estado = cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO_ORDEN)),
                    observaciones = cursor.getString(cursor.getColumnIndexOrThrow(COL_OBSERVACIONES_ORDEN)),
                    patente = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATENTE_ORDEN)),
                    idSeguro = run {
                        val idx = cursor.getColumnIndex(COL_ID_SEGURO_ORDEN)
                        if (idx != -1 && !cursor.isNull(idx)) cursor.getInt(idx) else null
                    },
                    idCliente = run {
                        val idx = cursor.getColumnIndex(COL_ID_CLIENTE_ORDEN)
                        if (idx != -1 && !cursor.isNull(idx)) cursor.getInt(idx) else null
                    }
                )
                listaOrdenes.add(orden)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return listaOrdenes
    }

    fun borrarOrden(idOrden: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_ORDEN_TRABAJO, "$COL_NUMERO_ORDEN = ?", arrayOf(idOrden.toString()))
        db.close()
    }

    fun actualizarOrden(orden: OrdenDeTrabajo): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_PATENTE_ORDEN, orden.patente)
            put(COL_ESTADO_ORDEN, orden.estado)
            put(COL_OBSERVACIONES_ORDEN, orden.observaciones)
        }
        val filas = db.update(TABLE_ORDEN_TRABAJO, values, "$COL_NUMERO_ORDEN = ?", arrayOf(orden.numeroOrdenDeTrabajo.toString()))
        db.close()
        return filas
    }

    fun leerOrdenesEliminadas(): ArrayList<OrdenDeTrabajo> {
        return leerOrdenesPorEstado("Eliminada")
    }

    fun leerNombresCausaAveria(): ArrayList<String> {
        val listaCausas = ArrayList<String>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_CAUSA_AVERIA, arrayOf(COL_NOMBRE_CAUSA), null, null, null, null, COL_NOMBRE_CAUSA)
        if (cursor.moveToFirst()) {
            do {
                listaCausas.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_CAUSA)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return listaCausas
    }

    // --- MÉTODOS CLIENTE ---
    fun guardarCliente(cliente: Cliente): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_NOMBRE_CLIENTE, cliente.nombre)
            put(COL_TELEFONO_CLIENTE, cliente.telefono)
            put(COL_EMAIL_CLIENTE, cliente.email)
        }
        val id = db.insert(TABLE_CLIENTE, null, values)
        db.close()
        return id
    }

    fun eliminarCliente(idCliente: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_CLIENTE, "$COL_ID_CLIENTE = ?", arrayOf(idCliente.toString()))
        db.close()
    }

    fun leerClientePorId(id: Int): Cliente? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_CLIENTE, null, "$COL_ID_CLIENTE = ?", arrayOf(id.toString()), null, null, null)
        var cliente: Cliente? = null
        if (cursor.moveToFirst()) {
            cliente = Cliente(
                idCliente = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_CLIENTE)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_CLIENTE)),
                telefono = cursor.getString(cursor.getColumnIndexOrThrow(COL_TELEFONO_CLIENTE)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL_CLIENTE))
            )
        }
        cursor.close()
        db.close()
        return cliente
    }

    fun leerTodosLosClientes(): ArrayList<Cliente> {
        val lista = ArrayList<Cliente>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_CLIENTE, null, null, null, null, null, COL_NOMBRE_CLIENTE)
        if (cursor.moveToFirst()) {
            do {
                lista.add(Cliente(
                    idCliente = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_CLIENTE)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_CLIENTE)),
                    telefono = cursor.getString(cursor.getColumnIndexOrThrow(COL_TELEFONO_CLIENTE)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL_CLIENTE))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun buscarClientes(busqueda: String): ArrayList<Cliente> {
        val lista = ArrayList<Cliente>()
        val db = this.readableDatabase
        val query = "$COL_NOMBRE_CLIENTE LIKE ? OR $COL_EMAIL_CLIENTE LIKE ?"
        val args = arrayOf("%$busqueda%", "%$busqueda%")
        
        val cursor = db.query(TABLE_CLIENTE, null, query, args, null, null, COL_NOMBRE_CLIENTE)
        if (cursor.moveToFirst()) {
            do {
                lista.add(Cliente(
                    idCliente = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_CLIENTE)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_CLIENTE)),
                    telefono = cursor.getString(cursor.getColumnIndexOrThrow(COL_TELEFONO_CLIENTE)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL_CLIENTE))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // --- MÉTODOS REPUESTOS ---
    fun guardarRepuesto(repuesto: Repuesto): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_NOMBRE_REPUESTO, repuesto.nombreRepuesto)
            put(COL_DESC_REPUESTO, repuesto.descripcionRepuesto)
            put(COL_STOCK_REPUESTO, repuesto.stock)
            put(COL_PRECIO_REPUESTO, repuesto.precio)
        }
        val id = db.insert(TABLE_REPUESTO, null, values)
        db.close()
        return id
    }

    fun leerTodosLosRepuestos(): ArrayList<Repuesto> {
        val lista = ArrayList<Repuesto>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_REPUESTO, null, null, null, null, null, COL_NOMBRE_REPUESTO)
        if (cursor.moveToFirst()) {
            do {
                lista.add(Repuesto(
                    idRepuesto = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_REPUESTO)),
                    nombreRepuesto = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_REPUESTO)),
                    descripcionRepuesto = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESC_REPUESTO)),
                    stock = cursor.getInt(cursor.getColumnIndexOrThrow(COL_STOCK_REPUESTO)),
                    precio = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRECIO_REPUESTO))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // --- MÉTODOS FACTURAS ---
    fun guardarFactura(factura: Factura): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_CODIGO_FACTURA, factura.codigoFactura)
            put(COL_ID_CLIENTE_FACTURA, factura.idCliente)
            put(COL_ID_ORDEN_FACTURA, factura.idOrden)
            put(COL_FECHA_FACTURA, factura.fechaFactura)
            put(COL_MONTO_FACTURA, factura.montoTotal)
            put(COL_ESTADO_FACTURA, factura.estadoFactura)
        }
        val id = db.insert(TABLE_FACTURA, null, values)
        db.close()
        return id
    }

    fun leerFacturas(): ArrayList<Factura> {
        val lista = ArrayList<Factura>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_FACTURA, null, null, null, null, null, "$COL_FECHA_FACTURA DESC")
        if (cursor.moveToFirst()) {
            do {
                lista.add(Factura(
                    idFactura = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_FACTURA)),
                    codigoFactura = cursor.getString(cursor.getColumnIndexOrThrow(COL_CODIGO_FACTURA)),
                    idCliente = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_CLIENTE_FACTURA)),
                    idOrden = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_ORDEN_FACTURA)),
                    fechaFactura = cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA_FACTURA)),
                    montoTotal = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MONTO_FACTURA)),
                    estadoFactura = cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO_FACTURA))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // --- MÉTODOS COTIZACIONES ---
    fun guardarCotizacion(cotizacion: Cotizacion): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_CODIGO_COTIZACION, cotizacion.codigoCotizacion)
            put(COL_ID_CLIENTE_COTIZACION, cotizacion.idCliente)
            put(COL_PATENTE_COTIZACION, cotizacion.patente)
            put(COL_FECHA_COTIZACION, cotizacion.fechaCotizacion)
            put(COL_MONTO_COTIZACION, cotizacion.montoCotizacion)
            put(COL_ESTADO_COTIZACION, cotizacion.estadoCotizacion)
        }
        val id = db.insert(TABLE_COTIZACION, null, values)
        db.close()
        return id
    }

    fun leerCotizaciones(): ArrayList<Cotizacion> {
        val lista = ArrayList<Cotizacion>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_COTIZACION, null, null, null, null, null, "$COL_FECHA_COTIZACION DESC")
        if (cursor.moveToFirst()) {
            do {
                lista.add(Cotizacion(
                    idCotizacion = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_COTIZACION)),
                    codigoCotizacion = cursor.getString(cursor.getColumnIndexOrThrow(COL_CODIGO_COTIZACION)),
                    idCliente = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_CLIENTE_COTIZACION)),
                    patente = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATENTE_COTIZACION)),
                    fechaCotizacion = cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA_COTIZACION)),
                    montoCotizacion = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MONTO_COTIZACION)),
                    estadoCotizacion = cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO_COTIZACION))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // --- MÉTODOS CITAS ---
    fun guardarCita(cita: Cita): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_ID_CLIENTE_CITA, cita.idCliente)
            put(COL_PATENTE_CITA, cita.patente)
            put(COL_FECHA_CITA, cita.fechaCita)
            put(COL_HORA_CITA, cita.horaCita)
            put(COL_TIPO_CITA, cita.tipoCita)
            put(COL_ESTADO_CITA, cita.estadoCita)
        }
        val id = db.insert(TABLE_CITA, null, values)
        db.close()
        return id
    }

    fun leerCitas(): ArrayList<Cita> {
        val lista = ArrayList<Cita>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_CITA, null, null, null, null, null, "$COL_FECHA_CITA ASC")
        if (cursor.moveToFirst()) {
            do {
                lista.add(Cita(
                    idCita = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_CITA)),
                    idCliente = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID_CLIENTE_CITA)),
                    patente = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATENTE_CITA)),
                    fechaCita = cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA_CITA)),
                    horaCita = cursor.getString(cursor.getColumnIndexOrThrow(COL_HORA_CITA)),
                    tipoCita = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPO_CITA)),
                    estadoCita = cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO_CITA))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // --- MÉTODOS RELACIÓN ORDEN-REPUESTO ---
    fun agregarRepuestoAOrden(idOrden: Int, idRepuesto: Int, cantidad: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_OR_ID_ORDEN, idOrden)
            put(COL_OR_ID_REPUESTO, idRepuesto)
            put(COL_OR_CANTIDAD, cantidad)
        }
        db.insert(TABLE_ORDEN_REPUESTO, null, values)
        db.close()
    }

    fun obtenerRepuestosDeOrden(idOrden: Int): ArrayList<Map<String, Any>> {
        val lista = ArrayList<Map<String, Any>>()
        val db = this.readableDatabase
        val query = "SELECT r.$COL_NOMBRE_REPUESTO, r.$COL_PRECIO_REPUESTO, rel.$COL_OR_CANTIDAD " +
                    "FROM $TABLE_REPUESTO r " +
                    "INNER JOIN $TABLE_ORDEN_REPUESTO rel ON r.$COL_ID_REPUESTO = rel.$COL_OR_ID_REPUESTO " +
                    "WHERE rel.$COL_OR_ID_ORDEN = ?"
        
        val cursor = db.rawQuery(query, arrayOf(idOrden.toString()))
        if (cursor.moveToFirst()) {
            do {
                val item = HashMap<String, Any>()
                item["nombre"] = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_REPUESTO))
                item["precio"] = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRECIO_REPUESTO))
                item["cantidad"] = cursor.getInt(cursor.getColumnIndexOrThrow(COL_OR_CANTIDAD))
                lista.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // --- MÉTODOS RELACIÓN ORDEN-CAUSA ---
    fun agregarCausaAOrden(idOrden: Int, idCausa: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_OC_ID_ORDEN, idOrden)
            put(COL_OC_ID_CAUSA, idCausa)
        }
        db.insert(TABLE_ORDEN_CAUSA, null, values)
        db.close()
    }

    fun obtenerCausasDeOrden(idOrden: Int): ArrayList<String> {
        val lista = ArrayList<String>()
        val db = this.readableDatabase
        val query = "SELECT c.$COL_NOMBRE_CAUSA " +
                    "FROM $TABLE_CAUSA_AVERIA c " +
                    "INNER JOIN $TABLE_ORDEN_CAUSA rel ON c.$COL_CODIGO_CAUSA = rel.$COL_OC_ID_CAUSA " +
                    "WHERE rel.$COL_OC_ID_ORDEN = ?"
        
        val cursor = db.rawQuery(query, arrayOf(idOrden.toString()))
        if (cursor.moveToFirst()) {
            do {
                lista.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_CAUSA)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }
    
    // Método auxiliar para obtener lista simple de causas (ID y Nombre) para diálogos
    fun obtenerListaCausas(): ArrayList<Map<String, Any>> {
        val lista = ArrayList<Map<String, Any>>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_CAUSA_AVERIA, null, null, null, null, null, COL_NOMBRE_CAUSA)
        if (cursor.moveToFirst()) {
            do {
                val item = HashMap<String, Any>()
                item["id"] = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CODIGO_CAUSA))
                item["nombre"] = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE_CAUSA))
                lista.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }
}