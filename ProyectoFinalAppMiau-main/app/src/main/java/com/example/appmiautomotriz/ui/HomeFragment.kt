package com.example.appmiautomotriz.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.database.MIAUtomotrizDbHelper
import com.example.appmiautomotriz.model.OrdenDeTrabajo
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.api.ApiConstants
import com.example.appmiautomotriz.api.VolleySingleton
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Fragmento principal de la aplicación.
 * Muestra la lista de órdenes de trabajo pendientes.
 * Incluye funcionalidad de búsqueda por patente y botón para registrar nuevas órdenes.
 */
class HomeFragment : Fragment() {

    private lateinit var dbHelper: MIAUtomotrizDbHelper
    private lateinit var ordenAdapter: OrdenAdapter
    private var listaDeOrdenes = ArrayList<OrdenDeTrabajo>()
    private var listaOriginal = ArrayList<OrdenDeTrabajo>() // Copia de seguridad
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        dbHelper = MIAUtomotrizDbHelper(requireContext())

        recyclerView = view.findViewById(R.id.recyclerViewHistorial)
        recyclerView.layoutManager = LinearLayoutManager(context)
        ordenAdapter = OrdenAdapter(listaDeOrdenes)
        recyclerView.adapter = ordenAdapter

        // Inicializar FAB
        fab = view.findViewById(R.id.floatingActionButton3)
        
        // Verificar Rol para permisos (Tabla 2.3: Mecánico NO puede Crear)
        val sharedPref = requireActivity().getSharedPreferences("AppSession", android.content.Context.MODE_PRIVATE)
        val rol = sharedPref.getString("role", "client") // Default to client (least privilege)
        
        // Según la tabla: Mecánico NO puede Crear (X). Cliente NO puede Crear. Solo Admin.
        if (rol == "admin") {
            fab.visibility = View.VISIBLE
            fab.setOnClickListener {
                val intent = Intent(context, RegistroActivity::class.java)
                startActivity(intent)
            }
        } else {
            fab.visibility = View.GONE
        }

        // Inicializar Buscador
        val etBuscar = view.findViewById<android.widget.EditText>(R.id.etBuscar)
        
        if (rol == "client") {
             etBuscar.visibility = View.GONE
        } else {
             etBuscar.visibility = View.VISIBLE
             etBuscar.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    filtrar(s.toString())
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Intentar cargar desde API primero, fallback a local
        cargarDatosDesdeApi()
    }

    private fun cargarDatosDesdeApi() {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val sharedPref = requireActivity().getSharedPreferences("AppSession", android.content.Context.MODE_PRIVATE)
        val rol = sharedPref.getString("role", "client")
        val emailUsuario = sharedPref.getString("username", "") ?: ""
        
        var query: com.google.firebase.firestore.Query = db.collection("ordenes_trabajo")
        
        // Si es cliente, filtramos por PATENTE (Prioridad)
        if (rol == "client") {
            var patenteSesion = sharedPref.getString("patente", "") ?: ""
            // Normalizar a Mayúsculas para coincidir con el formato estándar
            patenteSesion = patenteSesion.uppercase().trim()
            
            if (patenteSesion.isNotEmpty()) {
                // Filtro por Patente
                // NOTA: Firestore es Case-Sensitive. Buscamos la versión Mayúscula.
                query = query.whereEqualTo("patente", patenteSesion)
            } else if (emailUsuario.isNotEmpty()) {
                // Fallback: Si no tiene patente, buscamos por RUT (legacy)
                 query = query.whereEqualTo("id_cliente_rut", emailUsuario) 
            }
        }
        
        query.get()
            .addOnSuccessListener { result ->
                listaDeOrdenes.clear()
                listaOriginal.clear()
                for (document in result) {
                    try {
                        val orden = OrdenDeTrabajo(
                            numeroOrdenDeTrabajo = document.getLong("numero_orden")?.toInt() ?: 0,
                            fechaOrdenDeTrabajo = document.getString("fecha") ?: "",
                            estado = document.getString("estado") ?: "Pendiente",
                            observaciones = document.getString("observaciones") ?: "",
                            patente = document.getString("patente") ?: "",
                            idSeguro = document.getLong("id_seguro")?.toInt(),
                            firestoreId = document.id,
                            nombreCliente = run {
                                val idCli = document.getLong("id_cliente")?.toInt()
                                if (idCli != null) {
                                    dbHelper.leerClientePorId(idCli)?.nombre
                                } else {
                                    null
                                }
                            }
                        )
                        // Solo agregar si es Pendiente (filtrado en cliente por seguridad)
                        if (orden.estado == "Pendiente") {
                            listaDeOrdenes.add(orden)
                            listaOriginal.add(orden)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                ordenAdapter.notifyDataSetChanged()
                
                if (listaDeOrdenes.isEmpty()) {
                    // Si Firestore dice que está vacío, ES PORQUE ESTÁ VACÍO.
                    // No cargamos localmente para respetar que se hayan borrado en la nube.
                    // Si el usuario quiere ver datos offline, debe desconectarse internet.
                    android.widget.Toast.makeText(context, "No tienes órdenes disponibles", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    // Opcional: Toast de éxito o silencio total
                    // android.widget.Toast.makeText(context, "Datos actualizados", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                android.util.Log.w("Firestore", "Error getting documents.", exception)
                android.widget.Toast.makeText(context, "Error cargando desde Firestore. Usando datos locales.", android.widget.Toast.LENGTH_LONG).show()
                cargarDatosDeLaDb()
            }
    }

    private fun cargarDatosDeLaDb() {
        try {
            listaDeOrdenes.clear()
            listaOriginal.clear()
            var datosNuevos = dbHelper.leerOrdenesPendientes().filter { it.estado == "Pendiente" }
            
            // FILTRO DE SEGURIDAD LOCAL
            val sharedPref = requireActivity().getSharedPreferences("AppSession", android.content.Context.MODE_PRIVATE)
            val rol = sharedPref.getString("role", "client")
            val username = sharedPref.getString("username", "") ?: ""
            
            if (rol == "client") {
                val patenteSesion = sharedPref.getString("patente", "") ?: ""
                
                if (patenteSesion.isNotEmpty()) {
                    // Filtrar por Patente (como pidió el usuario)
                    // Esto es más seguro y directo que buscar por RUT que no existe en la DB local
                    datosNuevos = datosNuevos.filter { 
                        it.patente.equals(patenteSesion, ignoreCase = true) 
                    }
                } else {
                    // Si el cliente no tiene patente registrada en sesión, 
                    // intentamos por ID Cliente si existe, o no mostramos nada.
                    // Fallback: Si no hay patente, filtrar por username (RUT) si alguna vez
                    // logramos guardar el RUT en el cliente local. Por ahora, empty.
                    
                    // INTENTO 2: Filtrar por ID Numérico si 'username' coincide con 'telefono' o 'nombre' (heurística)
                    // Como el modelo Cliente no tiene RUT, no podemos machear exacto RUT.
                    // Mejor vaciar para evitar leaks.
                    datosNuevos = emptyList()
                }
            }
            
            listaDeOrdenes.addAll(datosNuevos)
            listaOriginal.addAll(datosNuevos) // Guardar copia
            ordenAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Error al cargar base de datos local: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun filtrar(texto: String) {
        val listaFiltrada = ArrayList<OrdenDeTrabajo>()
        for (orden in listaOriginal) { // Filtrar desde la copia original
            if (orden.patente.lowercase().contains(texto.lowercase()) ||
                orden.numeroOrdenDeTrabajo.toString().contains(texto)) {
                listaFiltrada.add(orden)
            }
        }
        ordenAdapter.actualizarLista(listaFiltrada)
    }
}