package com.example.appmiautomotriz.ui.finished

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.database.MIAUtomotrizDbHelper
import com.example.appmiautomotriz.ui.OrdenAdapter
import com.example.appmiautomotriz.model.OrdenDeTrabajo
import com.example.appmiautomotriz.R

class FinishedTasksFragment : Fragment() {

    private lateinit var dbHelper: MIAUtomotrizDbHelper
    private lateinit var ordenAdapter: OrdenAdapter
    private var listaDeOrdenes = ArrayList<OrdenDeTrabajo>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false) // Reusing fragment_home layout as it's just a recycler view
        dbHelper = MIAUtomotrizDbHelper(requireContext())

        recyclerView = view.findViewById(R.id.recyclerViewHistorial)
        recyclerView.layoutManager = LinearLayoutManager(context)
        ordenAdapter = OrdenAdapter(listaDeOrdenes)
        recyclerView.adapter = ordenAdapter

        // Hide FAB since we don't create new tasks from finished list
        val fab = view.findViewById<View>(R.id.floatingActionButton3)
        fab.visibility = View.GONE

        return view
    }

    override fun onResume() {
        super.onResume()
        cargarDatosDesdeApi()
    }

    private fun cargarDatosDesdeApi() {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val sharedPref = requireActivity().getSharedPreferences("AppSession", android.content.Context.MODE_PRIVATE)
        val rol = sharedPref.getString("role", "client")
        val emailUsuario = sharedPref.getString("username", "") ?: ""

        var query = db.collection("ordenes_trabajo")
            .whereEqualTo("estado", "Finalizada")
            
         // Si es cliente, filtrar solo SUS finalizadas por PATENTE
        if (rol == "client") {
            var patenteSesion = sharedPref.getString("patente", "") ?: ""
            patenteSesion = patenteSesion.uppercase().trim()
            
            if (patenteSesion.isNotEmpty()) {
                query = query.whereEqualTo("patente", patenteSesion)
            } else if (emailUsuario.isNotEmpty()) {
                 query = query.whereEqualTo("id_cliente_rut", emailUsuario) 
            }
        }

        query.get()
            .addOnSuccessListener { result ->
                listaDeOrdenes.clear()
                for (document in result) {
                    try {
                        val orden = OrdenDeTrabajo(
                            numeroOrdenDeTrabajo = document.getLong("numero_orden")?.toInt() ?: 0,
                            fechaOrdenDeTrabajo = document.getString("fecha") ?: "",
                            estado = document.getString("estado") ?: "Finalizada",
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
                        listaDeOrdenes.add(orden)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                ordenAdapter.notifyDataSetChanged()
                
                if (listaDeOrdenes.isEmpty()) {
                     // Solo cargar local si NO estamos conectados o explícitamente offline. 
                     // Pero para consistencia con Home, intentamos un fallback seguro silenciado.
                     cargarDatosDeLaDb()
                     // Si aun asi esta vacio:
                     if (listaDeOrdenes.isEmpty()) {
                         android.widget.Toast.makeText(context, "No hay tareas finalizadas", android.widget.Toast.LENGTH_SHORT).show()
                     }
                }
            }
            .addOnFailureListener {
                android.widget.Toast.makeText(context, "Error cargando Historial", android.widget.Toast.LENGTH_SHORT).show()
                cargarDatosDeLaDb()
            }
    }

    private fun cargarDatosDeLaDb() {
         listaDeOrdenes.clear()
         var datosFinalizados = dbHelper.leerOrdenesFinalizadas()
         
         // FILTRO LOCAL PARA CLIENTES (SEGURIDAD)
         val sharedPref = requireActivity().getSharedPreferences("AppSession", android.content.Context.MODE_PRIVATE)
         val rol = sharedPref.getString("role", "client")
         
         if (rol == "client") {
            val patenteSesion = sharedPref.getString("patente", "") ?: ""
            if (patenteSesion.isNotEmpty()) {
                 datosFinalizados = ArrayList(datosFinalizados.filter { 
                     it.patente.equals(patenteSesion, ignoreCase = true) 
                 })
            } else {
                 datosFinalizados.clear() // Si no hay patente identificada, mejor no mostrar nada
            }
         }
         
         listaDeOrdenes.addAll(datosFinalizados)
         ordenAdapter.notifyDataSetChanged()
    }
}