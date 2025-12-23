package com.example.appmiautomotriz.ui.inventario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.model.Repuesto

class RepuestoAdapter(private var listaRepuestos: ArrayList<Repuesto>) :
    RecyclerView.Adapter<RepuestoAdapter.RepuestoViewHolder>() {

    class RepuestoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreRepuesto)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioRepuesto)
        val tvStock: TextView = view.findViewById(R.id.tvStockRepuesto)
        val tvDesc: TextView = view.findViewById(R.id.tvDescRepuesto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepuestoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_repuesto, parent, false)
        return RepuestoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepuestoViewHolder, position: Int) {
        val repuesto = listaRepuestos[position]
        holder.tvNombre.text = repuesto.nombreRepuesto
        holder.tvPrecio.text = "$ ${repuesto.precio}"
        holder.tvStock.text = "Stock: ${repuesto.stock}"
        holder.tvDesc.text = repuesto.descripcionRepuesto
    }

    override fun getItemCount(): Int = listaRepuestos.size

    fun actualizarLista(nuevaLista: ArrayList<Repuesto>) {
        listaRepuestos = nuevaLista
        notifyDataSetChanged()
    }
}
