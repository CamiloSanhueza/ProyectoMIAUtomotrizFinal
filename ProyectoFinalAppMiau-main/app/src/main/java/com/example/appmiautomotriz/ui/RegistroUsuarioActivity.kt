package com.example.appmiautomotriz.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.example.appmiautomotriz.R
import com.google.firebase.firestore.FirebaseFirestore

class RegistroUsuarioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_usuario)

        val etNombre = findViewById<TextInputEditText>(R.id.etNombreRegistro)
        val etRut = findViewById<TextInputEditText>(R.id.etRutRegistro)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreoRegistro)
        val etPatente = findViewById<TextInputEditText>(R.id.etPatenteRegistro)
        val etPassword = findViewById<TextInputEditText>(R.id.etPasswordRegistro)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrarUsuario)
        val tvVolver = findViewById<TextView>(R.id.tvVolverLogin)

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val rutRaw = etRut.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val patente = etPatente.text.toString().trim().uppercase()
            val password = etPassword.text.toString().trim()

            if (nombre.isEmpty() || rutRaw.isEmpty() || correo.isEmpty() || password.isEmpty() || patente.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                etPassword.error = "La contraseña debe tener al menos 8 caracteres"
                return@setOnClickListener
            }

            if (!com.example.appmiautomotriz.utils.RutValidator.validar(rutRaw)) {
                etRut.error = "RUT inválido"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                etCorreo.error = "Correo inválido"
                return@setOnClickListener
            }
            
            val regexPatente = Regex("^[A-Z]{2}[A-Z0-9]{2}[0-9]{2}$")
            if (!regexPatente.matches(patente)) {
                etPatente.error = "Formato inválido (Ej: AA1000 o BBBB10)"
                return@setOnClickListener
            }

            val rutFormateado = com.example.appmiautomotriz.utils.RutValidator.formatear(rutRaw)
            registrarUsuarioEnFirestore(nombre, rutFormateado, correo, password, patente)
        }

        tvVolver.setOnClickListener {
            finish()
        }
    }

    private fun registrarUsuarioEnFirestore(nombre: String, rut: String, correo: String, password: String, patente: String) {
        val db = FirebaseFirestore.getInstance()
        
        // Verificar si ya existe el RUT
        db.collection("usuarios")
            .whereEqualTo("usuario", rut) // Ahora 'usuario' guarda el RUT
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(this, "Este RUT ya está registrado", Toast.LENGTH_LONG).show()
                } else {
                    // Crear nuevo usuario
                    val nuevoUsuario = hashMapOf(
                        "usuario" to rut, // IDENTIFICADOR PRINCIPAL (LOGIN)
                        "email_contacto" to correo, // SOLO CONTACTO
                        "password" to password,
                        "rol" to "client",
                        "nombre" to nombre,
                        "patente" to patente,
                        "fecha_registro" to com.google.firebase.Timestamp.now()
                    )

                    db.collection("usuarios")
                        .add(nuevoUsuario)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al crear cuenta: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
