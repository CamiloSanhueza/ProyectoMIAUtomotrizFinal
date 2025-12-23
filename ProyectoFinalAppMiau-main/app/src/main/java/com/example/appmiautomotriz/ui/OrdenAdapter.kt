package com.example.appmiautomotriz.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.model.OrdenDeTrabajo
import com.example.appmiautomotriz.R

/**
 * Adaptador para el RecyclerView que muestra la lista de órdenes de trabajo.
 * Gestiona la visualización de cada ítem (patente, estado, observaciones) y los clics.
 */
class OrdenAdapter(private var listaOrdenes: ArrayList<OrdenDeTrabajo>) :
    RecyclerView.Adapter<OrdenAdapter.OrdenViewHolder>() {

    fun actualizarLista(nuevaLista: ArrayList<OrdenDeTrabajo>) {
        listaOrdenes = nuevaLista
        notifyDataSetChanged()
    }

    class OrdenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPatente: TextView = itemView.findViewById(R.id.tvPatente)
        val chipEstado: TextView = itemView.findViewById(R.id.chipEstado)
        val tvCausa: TextView = itemView.findViewById(R.id.tvCausa)
        val tvNombreCliente: TextView = itemView.findViewById(R.id.tvNombreCliente)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_orden_trabajo, parent, false) // Usa tu XML
        return OrdenViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listaOrdenes.size
    }

    override fun onBindViewHolder(holder: OrdenViewHolder, position: Int) {
        val ordenActual = listaOrdenes[position]

        holder.tvPatente.text = ordenActual.patente
        holder.chipEstado.text = ordenActual.estado
        holder.tvCausa.text = ordenActual.observaciones ?: "Sin observaciones"
        holder.tvFecha.text = ordenActual.fechaOrdenDeTrabajo
        
        if (!ordenActual.nombreCliente.isNullOrEmpty()) {
            holder.tvNombreCliente.text = "Cliente: ${ordenActual.nombreCliente}"
            holder.tvNombreCliente.visibility = View.VISIBLE
        } else {
            holder.tvNombreCliente.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetalleActivity::class.java)

            intent.putExtra("ORDEN_ID", ordenActual.numeroOrdenDeTrabajo) // Mantenemos ID numérico por compatibilidad
            intent.putExtra("FIRESTORE_ID", ordenActual.firestoreId) // Agregamos ID de Firestore (String)

            holder.itemView.context.startActivity(intent)
        }
    }
}

