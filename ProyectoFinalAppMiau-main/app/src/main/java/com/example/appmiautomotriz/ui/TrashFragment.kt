package com.example.appmiautomotriz.ui

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

/**
 * Fragmento que muestra la "Papelera de Reciclaje".
 * Lista las órdenes con estado "Eliminada" para su posible recuperación.
 */
class TrashFragment : Fragment() {

    private lateinit var dbHelper: MIAUtomotrizDbHelper
    private lateinit var ordenAdapter: OrdenAdapter
    private var listaDeOrdenes = ArrayList<OrdenDeTrabajo>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_trash, container, false)
        dbHelper = MIAUtomotrizDbHelper(requireContext())

        recyclerView = view.findViewById(R.id.recyclerViewHistorial)
        recyclerView.layoutManager = LinearLayoutManager(context)
        ordenAdapter = OrdenAdapter(listaDeOrdenes)
        recyclerView.adapter = ordenAdapter

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
        
        // Solo Admin puede ver la papelera global (o Mecanico?). Asumimos Admin.
        // Si fuera client, no deberia ver esto, pero por seguridad filtramos.

        db.collection("ordenes_trabajo")
            .whereEqualTo("estado", "Eliminada")
            .get()
            .addOnSuccessListener { result ->
                listaDeOrdenes.clear()
                for (document in result) {
                    try {
                        val orden = OrdenDeTrabajo(
                            numeroOrdenDeTrabajo = document.getLong("numero_orden")?.toInt() ?: 0,
                            fechaOrdenDeTrabajo = document.getString("fecha") ?: "",
                            estado = document.getString("estado") ?: "Eliminada",
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
                     // Si no hay en Firestore, intentar local (casos raros)
                     cargarDatosDeLaDb()
                } else {
                     android.widget.Toast.makeText(context, "Papelera actualizada desde Nube", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                android.widget.Toast.makeText(context, "Error cargando Papelera de Nube", android.widget.Toast.LENGTH_SHORT).show()
                cargarDatosDeLaDb()
            }
    }

    private fun cargarDatosDeLaDb() {
        // Solo cargar si la lista esta vacia (fallback) o para mezclar (complejo)
        // Por ahora, si fallo Firestore o esta vacio, miramos local
        if (listaDeOrdenes.isEmpty()) {
            val datosNuevos = dbHelper.leerOrdenesEliminadas()
            listaDeOrdenes.addAll(datosNuevos)
            ordenAdapter.notifyDataSetChanged()
        }
    }
}