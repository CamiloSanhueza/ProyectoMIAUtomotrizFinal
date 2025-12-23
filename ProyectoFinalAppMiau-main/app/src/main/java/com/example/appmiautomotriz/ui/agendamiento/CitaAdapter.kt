package com.example.appmiautomotriz.ui.agendamiento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.model.Cita

class CitaAdapter(private var listaCitas: ArrayList<Cita>) :
    RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHora: TextView = view.findViewById(R.id.tvHoraCita)
        val tvTipo: TextView = view.findViewById(R.id.tvTipoCita)
        val tvPatente: TextView = view.findViewById(R.id.tvPatenteCita)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoCita)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaCitas[position]
        holder.tvHora.text = "${cita.fechaCita} - ${cita.horaCita}"
        holder.tvTipo.text = cita.tipoCita
        holder.tvPatente.text = "Patente: ${cita.patente}"
        holder.tvEstado.text = cita.estadoCita
    }

    override fun getItemCount(): Int = listaCitas.size

    fun actualizarLista(nuevaLista: ArrayList<Cita>) {
        listaCitas = nuevaLista
        notifyDataSetChanged()
    }
}
