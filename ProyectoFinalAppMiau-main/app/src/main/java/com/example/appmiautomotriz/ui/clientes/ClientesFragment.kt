package com.example.appmiautomotriz.ui.clientes

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.database.MIAUtomotrizDbHelper
import com.example.appmiautomotriz.model.Cliente

class ClientesFragment : Fragment() {

    private lateinit var dbHelper: MIAUtomotrizDbHelper
    private lateinit var clienteAdapter: ClienteAdapter
    private var listaClientes = ArrayList<Cliente>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_clientes, container, false)
        dbHelper = MIAUtomotrizDbHelper(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvClientes)
        val tvSinClientes = view.findViewById<View>(R.id.tvSinClientes)
        val etBuscar = view.findViewById<EditText>(R.id.etBuscarCliente)

        recyclerView.layoutManager = LinearLayoutManager(context)
        clienteAdapter = ClienteAdapter(listaClientes)
        recyclerView.adapter = clienteAdapter

        // Cargar todos los clientes inicialmente
        cargarClientes()
        
        // Configurar borrado (Long Click)
        clienteAdapter.setOnClientLongClickListener { cliente ->
            // Verificar si es Admin
            val sharedPref = requireActivity().getSharedPreferences("AppSession", android.content.Context.MODE_PRIVATE)
            val rol = sharedPref.getString("role", "user")
            
            if (rol == "admin") {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Cliente")
                    .setMessage("¿Estás seguro de que deseas eliminar a ${cliente.nombre}?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        dbHelper.eliminarCliente(cliente.idCliente)
                        cargarClientes() // Recargar lista
                        android.widget.Toast.makeText(context, "Cliente eliminado", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                 android.widget.Toast.makeText(context, "Solo el Administrador puede borrar clientes", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // FAB Eliminado por solicitud del cliente (Gestión centralizada en Login)

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buscarClientes(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun cargarClientes() {
        listaClientes.clear()
        val datos = dbHelper.leerTodosLosClientes()
        
        // Datos de prueba si está vacío
        if (datos.isEmpty()) {
            dbHelper.guardarCliente(Cliente(0, "Juan Perez", "+56911111111", "juan@mail.com"))
            dbHelper.guardarCliente(Cliente(0, "Maria Gonzalez", "+56922222222", "maria@mail.com"))
            datos.addAll(dbHelper.leerTodosLosClientes())
        }

        listaClientes.addAll(datos)
        clienteAdapter.notifyDataSetChanged()
    }

    private fun buscarClientes(texto: String) {
        val resultados = dbHelper.buscarClientes(texto)
        clienteAdapter.actualizarLista(resultados)
        
        val tvSinClientes = view?.findViewById<View>(R.id.tvSinClientes)
        if (resultados.isEmpty()) {
            tvSinClientes?.visibility = View.VISIBLE
        } else {
            tvSinClientes?.visibility = View.GONE
        }
    }
}
