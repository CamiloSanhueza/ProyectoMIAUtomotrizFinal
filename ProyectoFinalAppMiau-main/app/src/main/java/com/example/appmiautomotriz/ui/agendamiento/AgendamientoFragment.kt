package com.example.appmiautomotriz.ui.agendamiento

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.database.MIAUtomotrizDbHelper
import com.example.appmiautomotriz.model.Cita
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgendamientoFragment : Fragment() {

    private lateinit var dbHelper: MIAUtomotrizDbHelper
    private lateinit var citaAdapter: CitaAdapter
    private var listaCitasDelDia = ArrayList<Cita>()
    private var fechaSeleccionada: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_agendamiento, container, false)
        dbHelper = MIAUtomotrizDbHelper(requireContext())

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val rvCitas = view.findViewById<RecyclerView>(R.id.rvCitas)
        val tvCitasFecha = view.findViewById<TextView>(R.id.tvCitasFecha)
        val fabAgregar = view.findViewById<FloatingActionButton>(R.id.fabAgregarCita)
        val rol = requireActivity().getSharedPreferences("AppSession", android.content.Context.MODE_PRIVATE).getString("role", "client")
        
        if (rol == "client" || rol == "mechanic") {
            calendarView.visibility = if (rol == "client") View.GONE else View.VISIBLE
            tvCitasFecha.text = if (rol == "client") "Mis Citas (Historial)" else "Citas para: $fechaSeleccionada"
            fabAgregar.visibility = View.GONE // Mecánicos y Clientes NO pueden agendar
        } else {
             // Admin
             calendarView.visibility = View.VISIBLE
             tvCitasFecha.text = "Citas para: $fechaSeleccionada"
             fabAgregar.visibility = View.VISIBLE
        }

        rvCitas.layoutManager = LinearLayoutManager(context)
        citaAdapter = CitaAdapter(listaCitasDelDia)
        rvCitas.adapter = citaAdapter

        // Inicializar con fecha actual
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fechaSeleccionada = dateFormat.format(calendar.time)
        cargarCitas(fechaSeleccionada)
        tvCitasFecha.text = "Citas para: $fechaSeleccionada"

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            fechaSeleccionada = dateFormat.format(cal.time)
            
            val sharedPref = requireActivity().getSharedPreferences("AppSession", android.content.Context.MODE_PRIVATE)
            val rol = sharedPref.getString("role", "client")
            if (rol == "client") {
                tvCitasFecha.text = "Mis Citas (Historial)"
            } else {
                tvCitasFecha.text = "Citas para: $fechaSeleccionada"
            }
            cargarCitas(fechaSeleccionada)
        }
        
        // Carga inicial textual


        fabAgregar.setOnClickListener {
            mostrarDialogoAgregarCita()
        }

        return view
    }

    private fun cargarCitas(fecha: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val sharedPref = requireActivity().getSharedPreferences("AppSession", android.content.Context.MODE_PRIVATE)
        val rol = sharedPref.getString("role", "client")
        val patenteUsuario = sharedPref.getString("patente", "") ?: ""

        val referencia = db.collection("citas")
        
        // Lógica de consulta diferenciada
        val query = if (rol == "client" && patenteUsuario.isNotEmpty()) {
            // Clientes ven SU historial completo (ordenado por fecha idealmente)
            referencia.whereEqualTo("patente", patenteUsuario)
        } else {
            // Admin/Mecánico ven agenda del DÍA
            referencia.whereEqualTo("fecha", fecha)
        }
        
        query.get()
            .addOnSuccessListener { result ->
                listaCitasDelDia.clear()
                for (document in result) {
                    try {
                        val cita = Cita(
                            idCita = document.getLong("id_cita")?.toInt() ?: 0,
                            idCliente = document.getLong("id_cliente")?.toInt() ?: 0,
                            patente = document.getString("patente") ?: "",
                            fechaCita = document.getString("fecha") ?: "",
                            horaCita = document.getString("hora") ?: "",
                            tipoCita = document.getString("tipo") ?: "Mantenimiento",
                            estadoCita = document.getString("estado") ?: "Confirmada"
                        )
                        listaCitasDelDia.add(cita)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Ordenar localmente por hora si es Admin, o fecha/hora si es Cliente
                if (rol == "client") {
                    listaCitasDelDia.sortByDescending { it.fechaCita }
                } else {
                    listaCitasDelDia.sortBy { it.horaCita }
                }

                actualizarVistaVacia(rol)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error cargando citas: ${it.message}", Toast.LENGTH_SHORT).show()
                actualizarVistaVacia(rol)
            }
    }

    private fun actualizarVistaVacia(rol: String?) {
        val tvSinCitas = view?.findViewById<View>(R.id.tvSinCitas)
        if (listaCitasDelDia.isEmpty()) {
            tvSinCitas?.visibility = View.VISIBLE
            if (rol == "client") {
                (tvSinCitas as? TextView)?.text = "No tienes citas para este día"
            } else {
                (tvSinCitas as? TextView)?.text = "No hay citas programadas"
            }
        } else {
            tvSinCitas?.visibility = View.GONE
        }
        citaAdapter.notifyDataSetChanged()
    }

    private fun mostrarDialogoAgregarCita() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Agendar Nueva Cita")

        val context = requireContext()
        val layoutDialog = android.widget.LinearLayout(context)
        layoutDialog.orientation = android.widget.LinearLayout.VERTICAL
        layoutDialog.setPadding(50, 40, 50, 10)

        // Recuperar datos de sesión para pre-llenado
        val sharedPref = requireActivity().getSharedPreferences("AppSession", android.content.Context.MODE_PRIVATE)
        val rol = sharedPref.getString("role", "client")
        val patenteUsuario = sharedPref.getString("patente", "") ?: ""

        val etPatente = EditText(context)
        etPatente.hint = "Patente (Ej: AB1234)"
        
        if (rol == "client" && patenteUsuario.isNotEmpty()) {
             etPatente.setText(patenteUsuario)
             // Opcional: Bloquear edición si queremos forzar que sea su auto
             // etPatente.isEnabled = false 
        }
        
        layoutDialog.addView(etPatente)

        val etHora = EditText(context)
        etHora.hint = "Hora (Ej: 10:00)"
        etHora.isFocusable = false 
        layoutDialog.addView(etHora)

        val spinnerTipo = Spinner(context)
        val tipos = arrayOf("Mantenimiento", "Reparación", "Revisión", "Otro")
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, tipos)
        spinnerTipo.adapter = adapter
        layoutDialog.addView(spinnerTipo)

        etHora.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                etHora.setText(String.format("%02d:%02d", hour, minute))
            }
            TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        builder.setView(layoutDialog)

        builder.setPositiveButton("Agendar") { _, _ ->
            val patente = etPatente.text.toString().trim().uppercase()
            val hora = etHora.text.toString()
            val tipo = spinnerTipo.selectedItem.toString()

            if (patente.isNotEmpty() && hora.isNotEmpty()) {
                guardarCitaEnFirestore(patente, hora, tipo)
            } else {
                Toast.makeText(context, "Complete los campos", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun guardarCitaEnFirestore(patente: String, hora: String, tipo: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        // ID aleatorio seguro
        val nuevoId = kotlin.random.Random.nextInt(100000, 2147483647)
        
        val citaData = hashMapOf(
            "id_cita" to nuevoId,
            "id_cliente" to 0, // Por ahora 0, idealmente buscar ID usuario
            "patente" to patente,
            "fecha" to fechaSeleccionada,
            "hora" to hora,
            "tipo" to tipo,
            "estado" to "Confirmada"
        )

        db.collection("citas")
            .add(citaData)
            .addOnSuccessListener {
                Toast.makeText(context, "Cita agendada exitosamente", Toast.LENGTH_SHORT).show()
                cargarCitas(fechaSeleccionada)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al agendar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
