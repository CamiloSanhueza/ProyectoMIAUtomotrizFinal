package com.example.appmiautomotriz.ui.clientes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.model.Cliente

class ClienteAdapter(private var listaClientes: ArrayList<Cliente>) :
    RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    class ClienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCliente)
        val tvEmail: TextView = view.findViewById(R.id.tvEmailCliente)
        val tvTelefono: TextView = view.findViewById(R.id.tvTelefonoCliente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = listaClientes[position]
        holder.tvNombre.text = cliente.nombre
        holder.tvEmail.text = cliente.email
        holder.tvTelefono.text = cliente.telefono
        
        holder.itemView.setOnLongClickListener {
            onLongClick?.invoke(cliente)
            true
        }
    }

    override fun getItemCount(): Int = listaClientes.size

    fun actualizarLista(nuevaLista: ArrayList<Cliente>) {
        listaClientes = nuevaLista
        notifyDataSetChanged()
    }

    private var onLongClick: ((Cliente) -> Unit)? = null

    fun setOnClientLongClickListener(listener: (Cliente) -> Unit) {
        onLongClick = listener
    }
}
