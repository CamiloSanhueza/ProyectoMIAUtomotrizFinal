package com.example.appmiautomotriz.ui.historial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.database.MIAUtomotrizDbHelper
import com.example.appmiautomotriz.model.OrdenDeTrabajo
import com.example.appmiautomotriz.ui.OrdenAdapter

class HistorialVehiculoFragment : Fragment() {

    private lateinit var dbHelper: MIAUtomotrizDbHelper
    private lateinit var ordenAdapter: OrdenAdapter
    private var listaHistorial = ArrayList<OrdenDeTrabajo>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvSinResultados: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_historial_vehiculo, container, false)
        dbHelper = MIAUtomotrizDbHelper(requireContext())

        recyclerView = view.findViewById(R.id.rvHistorialVehiculo)
        tvSinResultados = view.findViewById(R.id.tvSinResultados)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        ordenAdapter = OrdenAdapter(listaHistorial)
        recyclerView.adapter = ordenAdapter

        val etPatente = view.findViewById<EditText>(R.id.etBuscarPatente)
        val btnBuscar = view.findViewById<Button>(R.id.btnBuscarHistorial)

        // Verificar Rol
        val sharedPref = requireActivity().getSharedPreferences("AppSession", android.content.Context.MODE_PRIVATE)
        val rol = sharedPref.getString("role", "user")

        if (rol == "client") {
            // Cliente: Pre-llenar con SU patente real guardada en sesión
            val patenteGuardada = sharedPref.getString("patente", "") ?: ""
            if (patenteGuardada.isNotEmpty()) {
                etPatente.setText(patenteGuardada)
                etPatente.isEnabled = false // Bloquear para que no vea la de otros
                btnBuscar.visibility = View.GONE
                buscarHistorial(patenteGuardada)
            }
        } else {
             // Admin/Mecánico: libre
        }

        btnBuscar.setOnClickListener {
            val patente = etPatente.text.toString().trim()
            if (patente.isNotEmpty()) {
                buscarHistorial(patente)
            } else {
                Toast.makeText(context, "Ingrese una patente", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun buscarHistorial(patente: String) {
        listaHistorial.clear()
        val resultados = dbHelper.leerOrdenesPorPatente(patente)
        
        if (resultados.isNotEmpty()) {
            listaHistorial.addAll(resultados)
            tvSinResultados.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        } else {
            tvSinResultados.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
        ordenAdapter.notifyDataSetChanged()
    }
}
