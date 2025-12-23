package com.example.appmiautomotriz.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.appmiautomotriz.database.MIAUtomotrizDbHelper
import com.example.appmiautomotriz.model.OrdenDeTrabajo
import com.example.appmiautomotriz.model.Cliente
import com.example.appmiautomotriz.api.VolleySingleton
import com.example.appmiautomotriz.R

/**
 * Actividad para registrar una nueva orden o editar una existente.
 * Maneja el formulario de ingreso (Patente, Causa) y la lógica de guardado (Local + API).
 */
class RegistroActivity : AppCompatActivity() {

    private lateinit var dbHelper: MIAUtomotrizDbHelper
    private lateinit var etPatente: EditText
    private lateinit var etNombreCliente: EditText
    private lateinit var etCodigoPais: EditText // Nuevo campo
    private lateinit var etTelefonoCliente: EditText
    private lateinit var etEmailCliente: EditText
    private lateinit var etFecha: EditText
    private lateinit var spinnerCausa: Spinner
    private lateinit var btnGuardarOrden: Button

    private var modoEditar = false
    private var idOrdenEditar = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_registro)

            dbHelper = MIAUtomotrizDbHelper(this)

            etPatente = findViewById(R.id.etPatente)
            etNombreCliente = findViewById(R.id.etNombreCliente)
            etCodigoPais = findViewById(R.id.etCodigoPais)
            etTelefonoCliente = findViewById(R.id.etTelefonoCliente)
            etEmailCliente = findViewById(R.id.etEmailCliente)
            etFecha = findViewById(R.id.etFecha)
            spinnerCausa = findViewById(R.id.spinnerCausa)
            btnGuardarOrden = findViewById(R.id.btnGuardarOrden)

            etFecha.setOnClickListener {
                mostrarDatePicker()
            }
            
            val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            etFecha.setText(fechaHoy)

            val toolbar = findViewById<MaterialToolbar>(R.id.toolbarRegistro)
            toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            idOrdenEditar = intent.getIntExtra("ORDEN_ID_EDITAR", -1)

            if (idOrdenEditar != -1) {
                modoEditar = true
                cargarDatosParaEditar(idOrdenEditar)
            } else {
                modoEditar = false
            }

            cargarSpinner()

            btnGuardarOrden.setOnClickListener {
                guardarOActualizarOrden()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error Critical: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * NUEVA FUNCIÓN:
     * Carga los datos de una orden existente en los campos
     * cuando estamos en modo Edición.
     */
    private fun cargarDatosParaEditar(id: Int) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        db.collection("ordenes_trabajo")
            .whereEqualTo("numero_orden", id)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val orden = OrdenDeTrabajo(
                        numeroOrdenDeTrabajo = document.getLong("numero_orden")?.toInt() ?: 0,
                        fechaOrdenDeTrabajo = document.getString("fecha") ?: "",
                        estado = document.getString("estado") ?: "Pendiente",
                        observaciones = document.getString("observaciones") ?: "",
                        patente = document.getString("patente") ?: "",
                        idSeguro = document.getLong("id_seguro")?.toInt(),
                        idCliente = document.getLong("id_cliente")?.toInt()
                    )

                    etPatente.setText(orden.patente)
                    btnGuardarOrden.text = "Actualizar Orden"
                    
                    // Cargar Spinner selección
                    val causaGuardada = orden.observaciones?.replace("Falla reportada: ", "")
                    val adapter = spinnerCausa.adapter as ArrayAdapter<String>
                    val posicion = adapter.getPosition(causaGuardada)
                    if (posicion >= 0) {
                        spinnerCausa.setSelection(posicion)
                    }

                    // Cargar datos del cliente (intentamos buscar por ID, si no está en local, habría que buscar en Firestore)
                    // Por simplicidad, si no está en local, dejamos vacío o mostramos ID
                    if (orden.idCliente != null) {
                         val cliente = dbHelper.leerClientePorId(orden.idCliente)
                         if (cliente != null) {
                             etNombreCliente.setText(cliente.nombre)
                    // Intentar separar el código del número si es posible
                    val telefonoCompleto = cliente.telefono ?: ""
                    if (telefonoCompleto.length > 3 && telefonoCompleto.startsWith("+")) {
                        etCodigoPais.setText(telefonoCompleto.substring(0, 3)) // Ej: +56
                        etTelefonoCliente.setText(telefonoCompleto.substring(3)) // Resto
                    } else {
                         // Fallback si no tiene formato esperado
                         etTelefonoCliente.setText(telefonoCompleto)
                    }
                    etEmailCliente.setText(cliente.email)
                         }
                    }
                } else {
                    Toast.makeText(this, "Error: No se encontraron datos para editar", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun cargarSpinner() {
        val causas = dbHelper.leerNombresCausaAveria()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, causas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCausa.adapter = adapter
    }

    private fun guardarOActualizarOrden() {
        val patente = etPatente.text.toString().trim().uppercase()
        val causaSeleccionada = spinnerCausa.selectedItem.toString()
        val nombreCliente = etNombreCliente.text.toString()
        
        val codigoPais = etCodigoPais.text.toString().trim()
        val numeroTelefono = etTelefonoCliente.text.toString().trim()
        
        // Validación de teléfono (9 dígitos exactos para el número)
        if (numeroTelefono.length != 9) {
            etTelefonoCliente.error = "Debe tener 9 dígitos"
            Toast.makeText(this, "El teléfono debe ser de 9 dígitos", Toast.LENGTH_SHORT).show()
            return
        }
        
        val telefonoCliente = "$codigoPais$numeroTelefono" // Concatenar
        
        val emailCliente = etEmailCliente.text.toString()
        val fechaSeleccionada = etFecha.text.toString()

        if (patente.isBlank()) {
            Toast.makeText(this, "Debe ingresar una patente", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validación Estricta de Patente
        if (!validarPatente(patente)) {
             etPatente.error = "Formato inválido. Use AA1000, BBBB10 o AAAAA0"
             Toast.makeText(this, "Patente inválida. Formatos aceptados: AA-10-00, BBBB10, AAAAA-0", Toast.LENGTH_LONG).show()
             return // DETIENE EL GUARDADO
        }

        // Guardar Cliente primero
        val cliente = Cliente(
            nombre = nombreCliente,
            telefono = telefonoCliente,
            email = emailCliente
        )
        // Nota: Aquí siempre creamos un cliente nuevo por simplicidad en este paso.
        // En una app real, buscaríamos si ya existe.
        val idClienteNuevo = dbHelper.guardarCliente(cliente).toInt()

        var estadoOrden = "Pendiente"
        if (modoEditar) {
            val ordenExistente = dbHelper.leerOrdenPorId(idOrdenEditar)
            if (ordenExistente != null) {
                estadoOrden = ordenExistente.estado
            }
        }

        // Generar ID único si es nuevo
        val idFinal = if (idOrdenEditar != -1) idOrdenEditar else kotlin.random.Random.nextInt(100000, 2147483647)

        val orden = OrdenDeTrabajo(
            numeroOrdenDeTrabajo = idFinal,
            fechaOrdenDeTrabajo = fechaSeleccionada,
            estado = estadoOrden,
            observaciones = "Falla reportada: $causaSeleccionada",
            patente = patente,
            idSeguro = null,
            idCliente = idClienteNuevo
        )

        // 1. Guardar en Base de Datos Local
        if (modoEditar) {
            dbHelper.actualizarOrden(orden)
        } else {
            dbHelper.guardarOrden(orden)
        }

        // 2. Enviar a la API (Volley/Firestore)
        enviarOrdenAlServidor(orden, modoEditar, emailCliente)
    }

    private fun validarPatente(patente: String): Boolean {
        // Normalizamos a mayúsculas y quitamos espacios extra y guiones solo para validar formato "limpio" si se quiere, 
        // pero el regex maneja con y sin guion.
        val p = patente.uppercase().trim()
        
        // Regex para formatos chilenos:
        // 1. Antigua (1985-2007): AA-10-00 o AA1000 (2 letras, 4 números)
        // ^[A-Z]{2}[-]?[0-9]{2}[-]?[0-9]{2}$ cubre AA-10-00 y AA1000 (y variantes AA-1000)
        // Simplificado: 2 letras + opcional guion + 4 números (con opcional guion al medio)
        val regexAntigua = Regex("^[A-Z]{2}-?\\d{4}$|^[A-Z]{2}-?\\d{2}-?\\d{2}$")
        
        // 2. Actual (2007-Presente): BB-BB-10 o BBBB10 (4 letras, 2 números)
        val regexActual = Regex("^[A-Z]{4}-?\\d{2}$|^[A-Z]{2}-?\\d{0}-?[A-Z]{2}-?\\d{2}$|^[A-Z]{2}\\.?[A-Z]{2}\\.?\\d{2}$") 
        // Ajuste simple: 4 letras seguidas de 2 números (con o sin guion separador bloque letras-nums)
        val regexActualSimple = Regex("^[A-Z]{4}-?\\d{2}$|^[A-Z]{2}-?[A-Z]{2}-?\\d{2}$")
        
        // 3. Futura (2025/2029+): BBBBB-0 (5 letras, 1 número)
        val regexProxima = Regex("^[A-Z]{5}-?\\d{1}$")

        return regexAntigua.matches(p) || regexActualSimple.matches(p) || regexProxima.matches(p)
    }

    private fun enviarOrdenAlServidor(orden: OrdenDeTrabajo, esEdicion: Boolean, emailCliente: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "numero_orden" to orden.numeroOrdenDeTrabajo, 
            "patente" to orden.patente,
            "observaciones" to orden.observaciones,
            "estado" to orden.estado,
            "fecha" to orden.fechaOrdenDeTrabajo,
            "id_cliente" to orden.idCliente,
            "id_seguro" to orden.idSeguro,
            "email_cliente" to emailCliente,
            "id_cliente_rut" to emailCliente 
        )

        if (esEdicion) {
            // ... (Lógica de actualización igual)
            db.collection("ordenes_trabajo")
                .whereEqualTo("numero_orden", orden.numeroOrdenDeTrabajo)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.update(data as Map<String, Any>)
                    }
                    Toast.makeText(this, "Orden actualizada en Firestore", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error actualizando: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
        } else {
            // Guardar Orden Nueva
            db.collection("ordenes_trabajo")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Orden registrada en Firestore", Toast.LENGTH_SHORT).show()
                    
                    // Lógica de Agendamiento Automático
                    val cbAgendar = findViewById<android.widget.CheckBox>(R.id.cbAgendarCita)
                    if (cbAgendar != null && cbAgendar.isChecked) {
                        crearCitaAutomatica(orden, db)
                    }
                    
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error guardando: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
        }
    }

    private fun crearCitaAutomatica(orden: OrdenDeTrabajo, db: com.google.firebase.firestore.FirebaseFirestore) {
        val nuevoIdCita = kotlin.random.Random.nextInt(100000, 2147483647)
        // Datos de la cita
        val citaData = hashMapOf(
            "id_cita" to nuevoIdCita,
            "id_cliente" to (orden.idCliente ?: 0),
            "patente" to orden.patente,
            "fecha" to orden.fechaOrdenDeTrabajo, // La fecha de la orden
            "hora" to "09:00", // Hora por defecto al ser automático
            "tipo" to "Revisión / Orden",
            "estado" to "Confirmada"
        )
        
        db.collection("citas").add(citaData)
            .addOnSuccessListener { 
                // Silencioso o Toast opcional
            }
            .addOnFailureListener { e ->
                android.util.Log.e("RegistroActivity", "Error creando cita auto: ${e.message}")
            }
    }

    private fun mostrarDatePicker() {
        val calendario = java.util.Calendar.getInstance()
        val anio = calendario.get(java.util.Calendar.YEAR)
        val mes = calendario.get(java.util.Calendar.MONTH)
        val dia = calendario.get(java.util.Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Formato yyyy-MM-dd
                val fecha = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                etFecha.setText(fecha)
            },
            anio, mes, dia
        )
        datePickerDialog.show()
    }
}