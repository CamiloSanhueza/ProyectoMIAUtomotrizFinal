package com.example.appmiautomotriz.ui.inventario

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.database.MIAUtomotrizDbHelper
import com.example.appmiautomotriz.model.Repuesto
import com.google.android.material.floatingactionbutton.FloatingActionButton

class InventarioFragment : Fragment() {

    private lateinit var dbHelper: MIAUtomotrizDbHelper
    private lateinit var repuestoAdapter: RepuestoAdapter
    private var listaRepuestos = ArrayList<Repuesto>()
    private var listaOriginal = ArrayList<Repuesto>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_inventario, container, false)
        dbHelper = MIAUtomotrizDbHelper(requireContext())

        recyclerView = view.findViewById(R.id.rvInventario)
        recyclerView.layoutManager = LinearLayoutManager(context)
        repuestoAdapter = RepuestoAdapter(listaRepuestos)
        recyclerView.adapter = repuestoAdapter

        val fab = view.findViewById<FloatingActionButton>(R.id.fabAgregarRepuesto)
        
        // Verificar Rol para permisos (Tabla 2.3: Mecánico NO puede Crear Repuestos)
        val sharedPref = requireActivity().getSharedPreferences("AppSession", android.content.Context.MODE_PRIVATE)
        val rol = sharedPref.getString("role", "user")
        
        if (rol == "mechanic" || rol == "client") {
            fab.visibility = View.GONE
        } else {
            fab.visibility = View.VISIBLE
            fab.setOnClickListener {
                mostrarDialogoAgregarRepuesto()
            }
        }

        val etBuscar = view.findViewById<EditText>(R.id.etBuscarRepuesto)
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrar(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        cargarInventario()
        return view
    }

    private fun cargarInventario() {
        listaRepuestos.clear()
        listaOriginal.clear()
        val datos = dbHelper.leerTodosLosRepuestos()
        listaRepuestos.addAll(datos)
        listaOriginal.addAll(datos)
        repuestoAdapter.notifyDataSetChanged()
    }

    private fun filtrar(texto: String) {
        val listaFiltrada = ArrayList<Repuesto>()
        for (item in listaOriginal) {
            if (item.nombreRepuesto.lowercase().contains(texto.lowercase())) {
                listaFiltrada.add(item)
            }
        }
        repuestoAdapter.actualizarLista(listaFiltrada)
    }

    private fun mostrarDialogoAgregarRepuesto() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Nuevo Repuesto")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputNombre = EditText(requireContext())
        inputNombre.hint = "Nombre del Repuesto"
        layout.addView(inputNombre)

        val inputDesc = EditText(requireContext())
        inputDesc.hint = "Descripción"
        layout.addView(inputDesc)

        val inputStock = EditText(requireContext())
        inputStock.hint = "Stock Inicial"
        inputStock.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(inputStock)

        val inputPrecio = EditText(requireContext())
        inputPrecio.hint = "Precio Unitario"
        inputPrecio.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(inputPrecio)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nombre = inputNombre.text.toString()
            val desc = inputDesc.text.toString()
            val stockStr = inputStock.text.toString()
            val precioStr = inputPrecio.text.toString()

            if (nombre.isNotEmpty() && stockStr.isNotEmpty() && precioStr.isNotEmpty()) {
                val nuevoRepuesto = Repuesto(
                    nombreRepuesto = nombre,
                    descripcionRepuesto = desc,
                    stock = stockStr.toInt(),
                    precio = precioStr.toInt()
                )
                dbHelper.guardarRepuesto(nuevoRepuesto)
                cargarInventario()
                Toast.makeText(context, "Repuesto guardado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
