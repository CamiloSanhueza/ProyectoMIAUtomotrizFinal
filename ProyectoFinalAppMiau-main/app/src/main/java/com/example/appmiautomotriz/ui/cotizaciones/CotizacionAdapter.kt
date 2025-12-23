package com.example.appmiautomotriz.ui.cotizaciones

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.model.Cotizacion

class CotizacionAdapter(private var listaCotizaciones: ArrayList<Cotizacion>) :
    RecyclerView.Adapter<CotizacionAdapter.CotizacionViewHolder>() {

    class CotizacionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigoCotizacion)
        val tvPatente: TextView = view.findViewById(R.id.tvPatenteCotizacion)
        val tvMonto: TextView = view.findViewById(R.id.tvMontoCotizacion)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoCotizacion)
        val llAcciones: LinearLayout = view.findViewById(R.id.llAccionesCotizacion)
        val btnAprobar: Button = view.findViewById(R.id.btnAprobarCotizacion)
        val btnRechazar: Button = view.findViewById(R.id.btnRechazarCotizacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CotizacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cotizacion, parent, false)
        return CotizacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CotizacionViewHolder, position: Int) {
        val cotizacion = listaCotizaciones[position]
        holder.tvCodigo.text = cotizacion.codigoCotizacion
        holder.tvPatente.text = "Patente: ${cotizacion.patente}"
        holder.tvMonto.text = "$ ${cotizacion.montoCotizacion}"
        holder.tvEstado.text = cotizacion.estadoCotizacion

        // Colores y Visibilidad de botones
        val color = when (cotizacion.estadoCotizacion) {
            "Aprobada" -> {
                holder.llAcciones.visibility = View.GONE
                Color.parseColor("#4CAF50") // Verde
            }
            "Rechazada" -> {
                holder.llAcciones.visibility = View.GONE
                Color.parseColor("#F44336") // Rojo
            }
            else -> { // Pendiente
                holder.llAcciones.visibility = View.VISIBLE
                Color.parseColor("#FF9800") // Naranja
            }
        }
        // Usamos setTint para mantener el shape del background
        holder.tvEstado.background?.setTint(color)

        holder.btnAprobar.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Aprobando cotización...", Toast.LENGTH_SHORT).show()
            // Aquí iría la lógica para actualizar BD
        }

        holder.btnRechazar.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Rechazando cotización...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = listaCotizaciones.size
}
