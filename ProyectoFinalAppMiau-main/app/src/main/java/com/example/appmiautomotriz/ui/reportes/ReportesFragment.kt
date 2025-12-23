package com.example.appmiautomotriz.ui.reportes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appmiautomotriz.R
import com.example.appmiautomotriz.database.MIAUtomotrizDbHelper
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class ReportesFragment : Fragment() {

    private lateinit var dbHelper: MIAUtomotrizDbHelper
    private lateinit var barChartIngresos: BarChart
    private lateinit var pieChartAverias: PieChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_reportes, container, false)
        dbHelper = MIAUtomotrizDbHelper(requireContext())

        barChartIngresos = view.findViewById(R.id.barChartIngresos)
        pieChartAverias = view.findViewById(R.id.pieChartAverias)

        cargarDatosFirestore()

        return view
    }

    private fun cargarDatosFirestore() {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        db.collection("ordenes_trabajo")
            .get()
            .addOnSuccessListener { result ->
                procesarDatos(result)
            }
            .addOnFailureListener { e ->
                android.widget.Toast.makeText(context, "Error cargando reportes: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
    }

    private fun procesarDatos(documents: com.google.firebase.firestore.QuerySnapshot) {
        val conteoAverias = HashMap<String, Float>()
        val conteoEstados = HashMap<String, Float>()
        
        // Inicializar contadores
        conteoEstados["Pendiente"] = 0f
        conteoEstados["Finalizada"] = 0f
        conteoEstados["Eliminada"] = 0f

        for (doc in documents) {
            // A. Extraer Estado
            val estado = doc.getString("estado") ?: "Pendiente"
            conteoEstados[estado] = (conteoEstados[estado] ?: 0f) + 1f

            // B. Extraer Avería desde "observaciones"
            val obs = doc.getString("observaciones") ?: ""
            val categoria = clasificarAveria(obs)
            conteoAverias[categoria] = (conteoAverias[categoria] ?: 0f) + 1f
        }

        actualizarGraficoAverias(conteoAverias)
        actualizarGraficoEstadosBarra(conteoEstados)
    }

    private fun clasificarAveria(observacion: String): String {
        val texto = observacion.lowercase()
        return when {
            texto.contains("fren") -> "Frenos"
            texto.contains("motor") || texto.contains("aceite") -> "Motor"
            texto.contains("eléctric") || texto.contains("batería") || texto.contains("luz") -> "Eléctrico"
            texto.contains("neumát") || texto.contains("rueda") -> "Neumáticos"
            texto.contains("suspen") -> "Suspensión"
            texto.contains("transmi") || texto.contains("embraf") -> "Transmisión"
            texto.contains("falla reportada") -> {
                // Intento de extraer lo que sigue a "reportada: "
                try {
                    val partes = observacion.split("reportada:")
                    if (partes.size > 1) partes[1].trim() else "Otras"
                } catch (e: Exception) { "Otras" }
            }
            else -> "Otras"
        }
    }

    private fun actualizarGraficoAverias(conteo: Map<String, Float>) {
        val entries = ArrayList<PieEntry>()
        
        for ((key, value) in conteo) {
            entries.add(PieEntry(value, key))
        }

        if (entries.isEmpty()) entries.add(PieEntry(1f, "Sin Datos"))

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = android.graphics.Color.WHITE

        val data = PieData(dataSet)
        pieChartAverias.data = data
        pieChartAverias.centerText = "Averías"
        pieChartAverias.description.isEnabled = false
        pieChartAverias.animateY(1000)
        pieChartAverias.invalidate()
    }

    private fun parsearMes(fecha: String): Int {
        try {
            // Intento muy básico de detectar mes. 
            // Si es YYYY-MM-DD, el mes está en indices 5..6
            // Si es DD/MM/YYYY, el mes está en indices 3..4
            if (fecha.contains("-")) {
                val partes = fecha.split("-")
                if (partes.size >= 2) return partes[1].toInt() - 1
            } else if (fecha.contains("/")) {
                val partes = fecha.split("/")
                if (partes.size >= 2) return partes[1].toInt() - 1
            }
        } catch (e: Exception) {
            return -1
        }
        return -1
    }



    private fun actualizarGraficoEstadosBarra(conteoEstados: Map<String, Float>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        
        // 1: Pendiente, 2: Finalizada, 3: Eliminada
        entries.add(BarEntry(1f, conteoEstados["Pendiente"] ?: 0f))
        entries.add(BarEntry(2f, conteoEstados["Finalizada"] ?: 0f))
        entries.add(BarEntry(3f, conteoEstados["Eliminada"] ?: 0f))

        val dataSet = BarDataSet(entries, "Estado de Órdenes")
        dataSet.colors = listOf(
            android.graphics.Color.rgb(255, 193, 7),  // Amber (Pendiente)
            android.graphics.Color.rgb(76, 175, 80),  // Green (Finalizada)
            android.graphics.Color.rgb(244, 67, 54)   // Red (Eliminada)
        )
        dataSet.valueTextSize = 14f
        
        val data = BarData(dataSet)
        barChartIngresos.data = data
        barChartIngresos.description.text = "Cantidad por Estado"
        
        // Configurar Eje X para mostrar etiquetas (1=Pend, 2=Fin, 3=Elim)
        // Nota: MPAndroidChart simple no soporta strings directos en eje X sin Formatter, 
        // pero usaremos leyenda de colores para simplificar.
        
        barChartIngresos.animateY(1000)
        barChartIngresos.invalidate()
    }
}
