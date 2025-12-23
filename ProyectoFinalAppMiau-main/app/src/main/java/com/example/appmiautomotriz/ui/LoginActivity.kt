package com.example.appmiautomotriz.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.api.ApiConstants
import com.example.appmiautomotriz.api.VolleySingleton
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var tvBienvenido: android.widget.TextView
    private lateinit var btnLogin: Button
    private lateinit var rbAdmin: android.widget.RadioButton
    private lateinit var rbMecanico: android.widget.RadioButton
    private lateinit var rbCliente: android.widget.RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Verificar si ya hay una sesión activa
        val sharedPref = getSharedPreferences("AppSession", MODE_PRIVATE)
        if (sharedPref.getBoolean("isLoggedIn", false)) {
            irAMainActivity()
            return
        }

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etContrasena)
        btnLogin = findViewById(R.id.btnLogin)
        tvBienvenido = findViewById(R.id.tvBienvenido)
        val rgRol = findViewById<android.widget.RadioGroup>(R.id.rgRol)
        rbAdmin = findViewById(R.id.rbAdmin)
        rbMecanico = findViewById(R.id.rbMecanico)
        rbCliente = findViewById(R.id.rbCliente)

        // Referencia al Layout del Input para cambiar el Hint
        val tilUsuario = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilUsuario)
        
        // Listener para cambio de rol
        rgRol.setOnCheckedChangeListener { _, checkedId ->
            actualizarColores(checkedId)
            when (checkedId) {
                R.id.rbCliente -> {
                    tilUsuario.hint = "RUT (Sin puntos ni guión)"
                    etUsuario.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    // Limitar a 9 caracteres para RUT
                    etUsuario.filters = arrayOf(android.text.InputFilter.LengthFilter(9))
                }
                else -> {
                    tilUsuario.hint = "Correo Electrónico"
                    etUsuario.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    // Limitar a 25 caracteres para Correo
                    etUsuario.filters = arrayOf(android.text.InputFilter.LengthFilter(25))
                }
            }
        }

        // Detección automática de Rol según lo que se escribe
        etUsuario.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val input = s.toString().lowercase().trim()
                
                // 1. Prioridad: Dominios específicos (Admin / Mecánico)
                if (input.endsWith("@taller.cl")) {
                    if (!rbAdmin.isChecked) rbAdmin.isChecked = true
                    return
                } 
                if (input.endsWith("@gmail.cl")) {
                    if (!rbMecanico.isChecked) rbMecanico.isChecked = true
                    return
                }

                // 2. Detección de RUT (Cliente)
                // Si empieza con un número y NO tiene @ (para no confundir con correos raros al inicio)
                if (input.isNotEmpty() && input[0].isDigit() && !input.contains("@")) {
                     if (!rbCliente.isChecked) rbCliente.isChecked = true
                }
            }
        })

        // Estado inicial (Forzar trigger del listener para setear hint correcto)
        val selectedId = rgRol.checkedRadioButtonId
        if (selectedId == R.id.rbMecanico) { // Mecánico es default
             tilUsuario.hint = "Correo Electrónico"
        }
        actualizarColores(selectedId)

        btnLogin.setOnClickListener {
            val usuarioRaw = etUsuario.text.toString().trim()
            val password = etPassword.text.toString()
            
            val rolSeleccionado = when {
                rbAdmin.isChecked -> "admin"
                rbMecanico.isChecked -> "mechanic"
                else -> "client"
            }

            if (usuarioRaw.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var usuarioFinal = usuarioRaw

            // Validación Dinámica
            if (rolSeleccionado == "client") {
                 // Validar RUT
                 if (!com.example.appmiautomotriz.utils.RutValidator.validar(usuarioRaw)) {
                     etUsuario.error = "RUT inválido"
                     return@setOnClickListener
                 }
                 usuarioFinal = com.example.appmiautomotriz.utils.RutValidator.formatear(usuarioRaw)
            } else {
                // Validación opcional de Email para Admin/Mecánico
            }
            
            // Logica Legacy / Hardcoded para demos (RUT ficticio si es cliente, Email si es staff)
            // ... (Mantenemos la lógica de redirección si coincide con los hardcoded)
            
            // Usuarios de prueba Hardcoded (Compatibilidad)
            // Login contra Firestore
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                var token: String? = null
                if (task.isSuccessful) token = task.result
                validarLogin(usuarioFinal, password, rolSeleccionado, token)
            }
        }

        val tvRegistrarse = findViewById<android.widget.TextView>(R.id.tvRegistrarse)
        tvRegistrarse.setOnClickListener {
            val intent = Intent(this, RegistroUsuarioActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validarLogin(usuario: String, pass: String, rol: String, fcmToken: String?) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        // Consultar usuario en Firestore
        db.collection("usuarios")
            .whereEqualTo("usuario", usuario)
            .whereEqualTo("password", pass) // Nota: En producción usar hashing!
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val rolServidor = document.getString("rol") ?: rol
                    val patente = document.getString("patente") ?: ""
                    
                    Toast.makeText(this, "Bienvenido $usuario", Toast.LENGTH_SHORT).show()
                    guardarSesion(usuario, rolServidor, patente)
                    
                    // Actualizar Token FCM si es necesario
                    fcmToken?.let { token ->
                        document.reference.update("fcm_token", token)
                    }
                    
                    irAMainActivity()
                } else {
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error de conexión: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun guardarSesion(usuario: String, rol: String, patente: String = "") {
        val sharedPref = getSharedPreferences("AppSession", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", true)
            putString("username", usuario)
            putString("role", rol)
            putString("patente", patente)
            apply()
        }
    }

    private fun actualizarColores(checkedId: Int) {
        val colorRes = when (checkedId) {
            R.id.rbAdmin -> R.color.admin_primary
            R.id.rbMecanico -> R.color.mechanic_primary
            else -> R.color.client_primary // Rojo para cliente
        }
        
        val colorInt = androidx.core.content.ContextCompat.getColor(this, colorRes)
        val colorStateList = android.content.res.ColorStateList.valueOf(colorInt)
        
        btnLogin.backgroundTintList = colorStateList
        tvBienvenido.setTextColor(colorStateList)
        
        // Actualizar tint de todos los radio buttons para feedback visual
        androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbAdmin, colorStateList)
        androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbMecanico, colorStateList)
        androidx.core.widget.CompoundButtonCompat.setButtonTintList(rbCliente, colorStateList)
    }

    private fun irAMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Cierra el Login para que no se pueda volver atrás con el botón Back
    }
}