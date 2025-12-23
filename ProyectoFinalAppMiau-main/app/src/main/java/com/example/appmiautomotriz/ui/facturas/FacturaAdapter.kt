package com.example.appmiautomotriz.ui.facturas

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.model.Factura

class FacturaAdapter(private var listaFacturas: ArrayList<Factura>) :
    RecyclerView.Adapter<FacturaAdapter.FacturaViewHolder>() {

    class FacturaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigoFactura)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaFactura)
        val tvMonto: TextView = view.findViewById(R.id.tvMontoFactura)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoFactura)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_factura, parent, false)
        return FacturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {
        val factura = listaFacturas[position]
        holder.tvCodigo.text = factura.codigoFactura
        holder.tvFecha.text = factura.fechaFactura
        holder.tvMonto.text = "$ ${factura.montoTotal}"
        holder.tvEstado.text = factura.estadoFactura

        // Color simple para estado
        val color = if (factura.estadoFactura == "Pagada") {
            Color.parseColor("#4CAF50") // Verde
        } else {
            Color.parseColor("#FF9800") // Naranja
        }
        holder.tvEstado.background.setTint(color)
    }

    override fun getItemCount(): Int = listaFacturas.size
}
