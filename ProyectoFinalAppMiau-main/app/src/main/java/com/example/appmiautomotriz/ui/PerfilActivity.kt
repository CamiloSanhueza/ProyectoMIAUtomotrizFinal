package com.example.appmiautomotriz.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.example.appmiautomotriz.R
import com.google.firebase.firestore.FirebaseFirestore

class PerfilActivity : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etRut: TextInputEditText
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etPatente: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnGuardar: Button
    private lateinit var tvVolver: TextView
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        db = FirebaseFirestore.getInstance()

        etNombre = findViewById(R.id.etPerfilNombre)
        etRut = findViewById(R.id.etPerfilRut)
        etCorreo = findViewById(R.id.etPerfilCorreoReal)
        etPatente = findViewById(R.id.etPerfilPatente)
        etPassword = findViewById(R.id.etPerfilPassword)
        btnGuardar = findViewById(R.id.btnGuardarPerfil)
        tvVolver = findViewById(R.id.tvVolver)

        cargarDatosUsuario()

        btnGuardar.setOnClickListener {
            guardarCambios()
        }
        
        tvVolver.setOnClickListener {
            finish()
        }
    }

    private fun cargarDatosUsuario() {
        val sharedPref = getSharedPreferences("AppSession", MODE_PRIVATE)
        val rutUsuario = sharedPref.getString("username", "") ?: ""
        val patenteSesion = sharedPref.getString("patente", "") ?: ""
        
        etRut.setText(rutUsuario) // Muestra el RUT en el campo RUT
        etPatente.setText(patenteSesion)

        val rol = sharedPref.getString("role", "client")
        
        // Referencia al Layout para cambiar el Hint
        val tilRut = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPerfilRut) // Necesitaremos agregar ID en XML
        
        if (rol != "client") {
            // Si NO es cliente (Admin/Mecanico), ocultamos la patente y cambiamos label RUT -> Email
            etPatente.visibility = android.view.View.GONE
            tilRut?.hint = "Email (Usuario)"
        } else {
             etPatente.visibility = android.view.View.VISIBLE
             tilRut?.hint = "RUT (Usuario)"
        }

        if (rutUsuario.isNotEmpty()) {
            db.collection("usuarios")
                .whereEqualTo("usuario", rutUsuario)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val doc = documents.documents[0]
                        etNombre.setText(doc.getString("nombre") ?: "")
                        etCorreo.setText(doc.getString("email_contacto") ?: "") // Cargar Correo REAL
                        etPatente.setText(doc.getString("patente") ?: "") 
                    }
                }
        }
    }

    private fun guardarCambios() {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val patente = etPatente.text.toString().trim().uppercase()
        val password = etPassword.text.toString().trim()
        
        // Rut no se edita, se usa para buscar
        val rutUsuario = etRut.text.toString()

        if (nombre.isEmpty() || (etPatente.visibility == android.view.View.VISIBLE && patente.isEmpty()) || correo.isEmpty()) {
            Toast.makeText(this, "Nombre y Correo son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.error = "Correo inválido"
            return
        }

        if (etPatente.visibility == android.view.View.VISIBLE) {
             val regexPatente = Regex("^[A-Z]{2}[A-Z0-9]{2}[0-9]{2}$")
             if (!regexPatente.matches(patente)) {
                etPatente.error = "Formato inválido (Ej: AA1000)"
                return
             }
        }

        db.collection("usuarios")
            .whereEqualTo("usuario", rutUsuario)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val updates = hashMapOf<String, Any>(
                        "nombre" to nombre,
                        "email_contacto" to correo, // Guardar nuevo correo
                        "patente" to patente
                    )
                    if (password.isNotEmpty()) {
                        updates["password"] = password
                    }

                    doc.reference.update(updates)
                        .addOnSuccessListener {
                            // Actualizar sesión local
                            val sharedPref = getSharedPreferences("AppSession", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("patente", patente)
                                apply()
                            }
                            Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }
}
