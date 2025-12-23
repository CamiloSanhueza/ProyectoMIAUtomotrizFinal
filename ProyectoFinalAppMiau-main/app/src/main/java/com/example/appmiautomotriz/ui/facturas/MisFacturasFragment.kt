package com.example.appmiautomotriz.ui.facturas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.database.MIAUtomotrizDbHelper
import com.example.appmiautomotriz.model.Factura

class MisFacturasFragment : Fragment() {

    private lateinit var dbHelper: MIAUtomotrizDbHelper
    private lateinit var facturaAdapter: FacturaAdapter
    private var listaFacturas = ArrayList<Factura>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mis_facturas, container, false)
        dbHelper = MIAUtomotrizDbHelper(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvMisFacturas)
        val tvSinFacturas = view.findViewById<View>(R.id.tvSinFacturas)

        recyclerView.layoutManager = LinearLayoutManager(context)
        facturaAdapter = FacturaAdapter(listaFacturas)
        recyclerView.adapter = facturaAdapter

        // Cargar datos
        listaFacturas.clear()
        val datos = dbHelper.leerFacturas()
        
        // Simular datos si está vacío para demostración (Opcional, pero útil para que el usuario vea algo)
        if (datos.isEmpty()) {
            // dbHelper.guardarFactura(Factura(0, "FAC-001", 1, 1, "2023-12-01", 150000, "Pagada"))
            // dbHelper.guardarFactura(Factura(0, "FAC-002", 1, 2, "2023-12-15", 85000, "Pendiente"))
            // datos.addAll(dbHelper.leerFacturas())
        }
        
        if (datos.isNotEmpty()) {
            listaFacturas.addAll(datos)
            tvSinFacturas.visibility = View.GONE
        } else {
            tvSinFacturas.visibility = View.VISIBLE
        }
        facturaAdapter.notifyDataSetChanged()

        return view
    }
}
