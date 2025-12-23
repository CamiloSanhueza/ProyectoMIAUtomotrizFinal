package com.example.appmiautomotriz.ui.cotizaciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.database.MIAUtomotrizDbHelper
import com.example.appmiautomotriz.model.Cotizacion

class MisCotizacionesFragment : Fragment() {

    private lateinit var dbHelper: MIAUtomotrizDbHelper
    private lateinit var cotizacionAdapter: CotizacionAdapter
    private var listaCotizaciones = ArrayList<Cotizacion>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mis_cotizaciones, container, false)
        dbHelper = MIAUtomotrizDbHelper(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvMisCotizaciones)
        val tvSinCotizaciones = view.findViewById<View>(R.id.tvSinCotizaciones)

        recyclerView.layoutManager = LinearLayoutManager(context)
        cotizacionAdapter = CotizacionAdapter(listaCotizaciones)
        recyclerView.adapter = cotizacionAdapter

        // Cargar datos
        listaCotizaciones.clear()
        val datos = dbHelper.leerCotizaciones()
        
        if (datos.isNotEmpty()) {
            listaCotizaciones.addAll(datos)
            tvSinCotizaciones.visibility = View.GONE
        } else {
            tvSinCotizaciones.visibility = View.VISIBLE
        }
        cotizacionAdapter.notifyDataSetChanged()

        return view
    }
}
