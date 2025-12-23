package com.example.appmiautomotriz.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.appbar.MaterialToolbar
import com.example.appmiautomotriz.ui.MainActivity
import com.example.appmiautomotriz.database.MIAUtomotrizDbHelper
import com.example.appmiautomotriz.model.OrdenDeTrabajo
import com.example.appmiautomotriz.api.VolleySingleton
import com.example.appmiautomotriz.R

/**
 * Actividad que muestra el detalle completo de una orden.
 * Permite acciones como Editar, Eliminar (Papelera), Finalizar, Recuperar y Compartir.
 * También gestiona el detalle técnico (Causas y Repuestos).
 */
class DetalleActivity : AppCompatActivity() {

    private lateinit var dbHelper: MIAUtomotrizDbHelper
    private var idOrdenRecibido: Int = -1

    private lateinit var tvPatente: TextView
    private lateinit var tvCliente: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvObservaciones: TextView
    private lateinit var btnEliminar: Button
    private lateinit var btnEditar: Button
    
    private lateinit var tvListaCausas: TextView
    private lateinit var tvListaRepuestos: TextView
    
    // Propiedad para almacenar la orden cargada desde Firestore
    private var currentOrden: OrdenDeTrabajo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_detalle)

            dbHelper = MIAUtomotrizDbHelper(this)

            tvPatente = findViewById(R.id.tvDetallePatente)
            tvCliente = findViewById(R.id.tvDetalleCliente)
            tvTelefono = findViewById(R.id.tvDetalleTelefono)
            tvEstado = findViewById(R.id.tvDetalleEstado)
            tvFecha = findViewById(R.id.tvDetalleFecha)
            tvObservaciones = findViewById(R.id.tvDetalleObservaciones)
            
            tvListaCausas = findViewById(R.id.tvListaCausas)
            tvListaRepuestos = findViewById(R.id.tvListaRepuestos)
            val btnAgregarCausa = findViewById<Button>(R.id.btnAgregarCausa)
            val btnAgregarRepuesto = findViewById<Button>(R.id.btnAgregarRepuesto)

            btnEditar = findViewById(R.id.btnEditar)
            btnEliminar = findViewById(R.id.btnEliminar)
            val btnFinalizar = findViewById<Button>(R.id.btnFinalizar)
            val btnRecuperar = findViewById<Button>(R.id.btnRecuperar)
            val btnCompartir = findViewById<Button>(R.id.btnCompartir)
            val btnExportarPdf = findViewById<Button>(R.id.btnExportarPdf)

            val toolbar = findViewById<MaterialToolbar>(R.id.toolbarDetalle)
            toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            idOrdenRecibido = intent.getIntExtra("ORDEN_ID", -1)

            if (idOrdenRecibido != -1) {
                // Estado Inicial: Ocultar todo hasta que se carguen datos (Local o Nube)
                val btnFinalizar = findViewById<Button>(R.id.btnFinalizar)
                btnFinalizar.visibility = View.GONE
                btnEditar.visibility = View.GONE
                btnEliminar.visibility = View.GONE
                findViewById<Button>(R.id.btnRecuperar).visibility = View.GONE

                // 1. Cargar desde la Nube (Autoridad principal)
                cargarDatosDeLaOrden(idOrdenRecibido)
                cargarDetallesTecnicos(idOrdenRecibido)

                // 2. Intentar mostrar algo precargado desde Local si existe
                val ordenLocal = dbHelper.leerOrdenPorId(idOrdenRecibido)
                if (ordenLocal != null) {
                    currentOrden = ordenLocal
                    // Actualizar UI preliminarmente con datos locales mientras llega Firestore
                    actualizarVisibilidadBotones(ordenLocal)
                }
            } else {
                Toast.makeText(this, "Error: No se pudo cargar la orden", Toast.LENGTH_LONG).show()
                finish()
            }

            btnExportarPdf.setOnClickListener {
                if (currentOrden != null) {
                    val detalles = """
                        Causas:
                        ${tvListaCausas.text}
                        
                        Repuestos:
                        ${tvListaRepuestos.text}
                    """.trimIndent()
                    
                    com.example.appmiautomotriz.utils.PdfGenerator.generarPdfOrden(this, currentOrden!!, detalles)
                }
            }

            btnAgregarCausa.setOnClickListener {
                mostrarDialogoAgregarCausa()
            }

            btnAgregarRepuesto.setOnClickListener {
                mostrarDialogoAgregarRepuesto()
            }

            btnFinalizar.setOnClickListener {
                if (currentOrden != null) {
                    Toast.makeText(this, "Finalizando orden...", Toast.LENGTH_SHORT).show()
                    val ordenFinalizada = currentOrden!!.copy(estado = "Finalizada")
                    
                    // Intentar update local (best effort)
                    dbHelper.actualizarOrden(ordenFinalizada)
                    
                    // Update principal en Nube
                    finalizarOrdenEnServidor(ordenFinalizada)
                } else {
                     Toast.makeText(this, "Error: Orden no cargada", Toast.LENGTH_SHORT).show()
                }
            }

            btnRecuperar.setOnClickListener {
                if (currentOrden != null) {
                    Toast.makeText(this, "Recuperando orden...", Toast.LENGTH_SHORT).show()
                    val ordenRecuperada = currentOrden!!.copy(estado = "Pendiente")
                    
                    // 1. Update Local (si existe)
                    dbHelper.actualizarOrden(ordenRecuperada)
                    
                    // 2. Update Firestore
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val idDoc = ordenRecuperada.firestoreId
                    
                    if (!idDoc.isNullOrEmpty()) {
                        db.collection("ordenes_trabajo").document(idDoc)
                            .update("estado", "Pendiente")
                            .addOnSuccessListener {
                                Toast.makeText(this, "¡Orden recuperada!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al recuperar: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Error: No tiene ID de nube", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnEliminar.setOnClickListener {
                if (currentOrden != null) {
                    Toast.makeText(this, "Iniciando eliminación...", Toast.LENGTH_SHORT).show()
                    
                    // 1. Actualizar objeto en memoria
                    val ordenEliminada = currentOrden!!.copy(estado = "Eliminada")
                    
                    // 2. Intentar actualizar localmente (si existe)
                    // No importa si falla (ej: no existe local), lo importante es la Nube
                    dbHelper.actualizarOrden(ordenEliminada)
                    
                    // 3. Borrar de la Nube (La fuente de la verdad)
                    eliminarOrdenEnServidor(ordenEliminada)
                } else {
                    Toast.makeText(this, "Error: Orden no cargada completamente", Toast.LENGTH_SHORT).show()
                }
            }

            btnCompartir.setOnClickListener {
                compartirDetalleOrden()
            }

            btnEditar.setOnClickListener {
                val intent = Intent(this, RegistroActivity::class.java)
                intent.putExtra("ORDEN_ID_EDITAR", idOrdenRecibido)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error Detalle: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun cargarDetallesTecnicos(idOrden: Int) {
        // Cargar Causas
        // ... (Existing implementation)
        val causas = dbHelper.obtenerCausasDeOrden(idOrden)
        if (causas.isNotEmpty()) {
            tvListaCausas.text = causas.joinToString("\n") { "• $it" }
        } else {
            tvListaCausas.text = "Ninguna registrada"
        }

        // Cargar Repuestos
        val repuestos = dbHelper.obtenerRepuestosDeOrden(idOrden)
        if (repuestos.isNotEmpty()) {
            tvListaRepuestos.text = repuestos.joinToString("\n") { 
                "• ${it["nombre"]} (x${it["cantidad"]}) - $${it["precio"]}" 
            }
        } else {
            tvListaRepuestos.text = "Ninguno registrado"
        }
    }

    private fun mostrarDialogoAgregarCausa() {
         // ... (Existing implementation)
         val listaCausas = dbHelper.obtenerListaCausas()
         if (listaCausas.isEmpty()) {
            Toast.makeText(this, "No hay causas definidas en el sistema", Toast.LENGTH_SHORT).show()
            return
        }

        val nombres = listaCausas.map { it["nombre"] as String }.toTypedArray()
        val ids = listaCausas.map { it["id"] as Int }

        android.app.AlertDialog.Builder(this)
            .setTitle("Seleccionar Causa")
            .setItems(nombres) { _, which ->
                dbHelper.agregarCausaAOrden(idOrdenRecibido, ids[which])
                cargarDetallesTecnicos(idOrdenRecibido)
                Toast.makeText(this, "Causa agregada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoAgregarRepuesto() {
        // ... (Existing implementation)
        val listaRepuestos = dbHelper.leerTodosLosRepuestos()
        if (listaRepuestos.isEmpty()) {
            Toast.makeText(this, "No hay repuestos en inventario", Toast.LENGTH_SHORT).show()
            return
        }

        val nombres = listaRepuestos.map { "${it.nombreRepuesto} ($${it.precio})" }.toTypedArray()
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Seleccionar Repuesto")
            .setItems(nombres) { _, which ->
                val repuestoSeleccionado = listaRepuestos[which]
                dbHelper.agregarRepuestoAOrden(idOrdenRecibido, repuestoSeleccionado.idRepuesto, 1)
                cargarDetallesTecnicos(idOrdenRecibido)
                Toast.makeText(this, "Repuesto agregado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cargarDatosDeLaOrden(idNumerico: Int) {
        val firestoreId = intent.getStringExtra("FIRESTORE_ID") // Recuperar ID String
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        // Estrategia: Si tenemos ID de Firestore (String), usamos ese (Directo y Exacto).
        // Si no, usamos el ID Numérico (LegacySearch).
        
        val task = if (!firestoreId.isNullOrEmpty()) {
            db.collection("ordenes_trabajo").document(firestoreId).get()
        } else {
            // Fallback Legacy
            db.collection("ordenes_trabajo")
                .whereEqualTo("numero_orden", idNumerico)
                .get()
                .continueWith { task -> 
                    if (task.isSuccessful && !task.result.isEmpty) {
                        task.result.documents[0]
                    } else {
                        null
                    }
                }
        }

        task.addOnSuccessListener { result ->
            // El resultado puede ser DocumentSnapshot (Directo) o QuerySnapshot (Legacy tratado)
            // Unificamos lógica: 'result' aquí es DocumentSnapshot si usamos .document().get()
            // Pero si usamos whereEqualTo, recibimos QuerySnapshot.
            // Para simplificar, manejamos ambos flujos arriba o aquí.
            // REFACTOR: Usaremos lógica separada para claridad.
        }
        
        if (!firestoreId.isNullOrEmpty()) {
             // BUSQUEDA DIRECTA POR ID
             db.collection("ordenes_trabajo").document(firestoreId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        procesarDocumentoOrden(document)
                    } else {
                        Toast.makeText(this, "Error: Documento no encontrado", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error cargando orden", Toast.LENGTH_SHORT).show()
                }
        } else {
            // BUSQUEDA LEGACY POR NUMERO
            db.collection("ordenes_trabajo")
            .whereEqualTo("numero_orden", idNumerico)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    procesarDocumentoOrden(documents.documents[0])
                } else {
                     // FALLBACK LOCAL: Si no esta en Firestore, intentar cargar de SQLite
                     val ordenLocal = dbHelper.leerOrdenPorId(idNumerico)
                     if (ordenLocal != null) {
                         currentOrden = ordenLocal
                         // Populating UI manually since procesarDocumentoOrden expects a DocumentSnapshot
                         tvPatente.text = "Patente: ${ordenLocal.patente}"
                         tvEstado.text = "Estado: ${ordenLocal.estado}"
                         tvFecha.text = "Fecha: ${ordenLocal.fechaOrdenDeTrabajo}"
                         tvObservaciones.text = "Observaciones: ${ordenLocal.observaciones}"
                         
                         // Cargar Cliente Local
                         if (ordenLocal.idCliente != null) {
                             val cliente = dbHelper.leerClientePorId(ordenLocal.idCliente)
                             if (cliente != null) {
                                 tvCliente.text = "Cliente: ${cliente.nombre}"
                                 tvTelefono.text = "Teléfono: ${cliente.telefono}"
                             }
                         }
                         actualizarVisibilidadBotones(ordenLocal)
                         // Toast.makeText(this, "Modo Offline: Mostrando datos locales", Toast.LENGTH_SHORT).show()
                     } else {
                         Toast.makeText(this, "Orden no encontrada (Ni Local ni Nube)", Toast.LENGTH_LONG).show()
                     }
                }
            }
        }
    }

    private fun procesarDocumentoOrden(document: com.google.firebase.firestore.DocumentSnapshot) {
        val orden = OrdenDeTrabajo(
            numeroOrdenDeTrabajo = document.getLong("numero_orden")?.toInt() ?: 0,
            fechaOrdenDeTrabajo = document.getString("fecha") ?: "",
            estado = document.getString("estado") ?: "Pendiente",
            observaciones = document.getString("observaciones") ?: "",
            patente = document.getString("patente") ?: "",
            idSeguro = document.getLong("id_seguro")?.toInt(),
            idCliente = document.getLong("id_cliente")?.toInt(),
            firestoreId = document.id
        )
        
        currentOrden = orden

        tvPatente.text = "Patente: ${orden.patente}"
        tvEstado.text = "Estado: ${orden.estado}"
        tvFecha.text = "Fecha: ${orden.fechaOrdenDeTrabajo}"
        tvObservaciones.text = "Observaciones: ${orden.observaciones}"

        // Cargar Cliente
        if (orden.idCliente != null) {
            val cliente = dbHelper.leerClientePorId(orden.idCliente)
            if (cliente != null) {
                tvCliente.text = "Cliente: ${cliente.nombre}"
                tvTelefono.text = "Teléfono: ${cliente.telefono}"
            } else {
                tvCliente.text = "Cliente: ID ${orden.idCliente}" 
                tvTelefono.text = ""
            }
        }
        
        actualizarVisibilidadBotones(orden)
    }

    private fun actualizarVisibilidadBotones(orden: OrdenDeTrabajo) {
        val sharedPref = getSharedPreferences("AppSession", MODE_PRIVATE)
        val rol = sharedPref.getString("role", "client")
        
        val btnFinalizar = findViewById<Button>(R.id.btnFinalizar)
        val btnRecuperar = findViewById<Button>(R.id.btnRecuperar)
        val btnCompartir = findViewById<Button>(R.id.btnCompartir)
        val btnExportarPdf = findViewById<Button>(R.id.btnExportarPdf)
        val btnAgregarCausa = findViewById<Button>(R.id.btnAgregarCausa)
        val btnAgregarRepuesto = findViewById<Button>(R.id.btnAgregarRepuesto)

        val esEditable = orden.estado != "Finalizada" && orden.estado != "Eliminada"
        
        if (esEditable) {
            when (rol) {
                "admin" -> {
                    btnFinalizar.visibility = View.VISIBLE
                    btnEditar.visibility = View.VISIBLE
                    btnEliminar.visibility = View.VISIBLE
                    btnAgregarCausa.visibility = View.VISIBLE
                    btnAgregarRepuesto.visibility = View.VISIBLE
                    btnRecuperar.visibility = View.GONE
                }
                "mechanic" -> {
                    btnFinalizar.visibility = View.VISIBLE
                    btnEditar.visibility = View.VISIBLE
                    btnEliminar.visibility = View.GONE
                    btnAgregarCausa.visibility = View.VISIBLE
                    btnAgregarRepuesto.visibility = View.VISIBLE
                    btnRecuperar.visibility = View.GONE
                }
                "client" -> {
                    btnFinalizar.visibility = View.GONE
                    btnEditar.visibility = View.GONE
                    btnEliminar.visibility = View.GONE
                    btnAgregarCausa.visibility = View.GONE
                    btnAgregarRepuesto.visibility = View.GONE
                    btnRecuperar.visibility = View.GONE
                }
                else -> {
                    ocultarBotonesEdicion()
                }
            }
        } else {
            ocultarBotonesEdicion()
            
            // ADMIN: En estados no editables (Finalizada/Eliminada)
            // Ya no mostramos Eliminar (Usuario pidió quitarlo para Finalizadas también)
            btnEliminar.visibility = View.GONE

            // Permitir RECUPERAR (Reabrir) tanto si está Eliminada como si está Finalizada
            if (rol == "admin" && (orden.estado == "Eliminada" || orden.estado == "Finalizada")) {
                 btnRecuperar.visibility = View.VISIBLE
                 // Opcional: Cambiar texto según el caso, ej: "Reabrir Orden" si es finalizada
                 if (orden.estado == "Finalizada") {
                     btnRecuperar.text = "Reabrir Orden"
                 } else {
                     btnRecuperar.text = "Recuperar Orden"
                 }
            } else {
                 btnRecuperar.visibility = View.GONE
            }
        }
        
        // Compartir y PDF siempre visibles si la orden existe
        btnCompartir.visibility = View.VISIBLE
        btnExportarPdf.visibility = View.VISIBLE
    }

    private fun ocultarBotonesEdicion() {
        // ... (Existing implementation)
        val btnFinalizar = findViewById<Button>(R.id.btnFinalizar)
        val btnAgregarCausa = findViewById<Button>(R.id.btnAgregarCausa)
        val btnAgregarRepuesto = findViewById<Button>(R.id.btnAgregarRepuesto)
        
        btnFinalizar.visibility = View.GONE
        btnEditar.visibility = View.GONE
        btnEliminar.visibility = View.GONE
        btnAgregarCausa.visibility = View.GONE
        btnAgregarRepuesto.visibility = View.GONE
    }

    private fun compartirDetalleOrden() {
        val causas = tvListaCausas.text.toString()
        val repuestos = tvListaRepuestos.text.toString()

        val textoCompartir = """
            🚗 *Detalle de Orden de Trabajo* 🚗
            
            *Patente:* ${tvPatente.text.toString().replace("Patente: ", "")}
            *Cliente:* ${tvCliente.text.toString().replace("Cliente: ", "")}
            *Estado:* ${tvEstado.text.toString().replace("Estado: ", "")}
            *Fecha:* ${tvFecha.text.toString().replace("Fecha: ", "")}
            
            📝 *Observaciones:*
            ${tvObservaciones.text.toString().replace("Observaciones: ", "")}
            
            🔧 *Diagnóstico:*
            $causas
            
            🔩 *Repuestos:*
            $repuestos
            
            Enviado desde App MIAUtomotriz 🐱
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Detalle Orden ${tvPatente.text}")
            putExtra(Intent.EXTRA_TEXT, textoCompartir)
        }
        startActivity(Intent.createChooser(intent, "Compartir vía"))
    }

    private fun finalizarOrdenEnServidor(orden: OrdenDeTrabajo) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        db.collection("ordenes_trabajo")
            .whereEqualTo("numero_orden", orden.numeroOrdenDeTrabajo)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("estado", "Finalizada")
                }
                Toast.makeText(this, "Orden finalizada en Firestore", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Finalizado local. Error Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun eliminarOrdenEnServidor(orden: OrdenDeTrabajo) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        val firestoreId = intent.getStringExtra("FIRESTORE_ID") ?: currentOrden?.firestoreId

        if (!firestoreId.isNullOrEmpty()) {
             db.collection("ordenes_trabajo").document(firestoreId)
               .update("estado", "Eliminada")
               .addOnSuccessListener {
                   Toast.makeText(this, "Orden eliminada de Firestore", Toast.LENGTH_SHORT).show()
                   finish()
               }
               .addOnFailureListener {
                   Toast.makeText(this, "Error eliminando en Nube: ${it.message}", Toast.LENGTH_LONG).show()
                   finish() 
               }
        } else {
            // Fallback Legacy
            db.collection("ordenes_trabajo")
                .whereEqualTo("numero_orden", orden.numeroOrdenDeTrabajo)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.update("estado", "Eliminada")
                    }
                    Toast.makeText(this, "Orden eliminada de Firestore", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error eliminando en Nube (Legacy): ${it.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
        }
    }
}