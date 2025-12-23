package com.example.appmiautomotriz.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.appmiautomotriz.model.OrdenDeTrabajo
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object PdfGenerator {

    fun generarPdfOrden(context: Context, orden: OrdenDeTrabajo, detalles: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Logo
        val logoBitmap = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.appmiautomotriz.R.drawable.logo_app)
        if (logoBitmap != null) {
            val scaledLogo = android.graphics.Bitmap.createScaledBitmap(logoBitmap, 80, 80, false)
            canvas.drawBitmap(scaledLogo, 50f, 40f, paint)
        }

        // Título (Desplazado para no tapar el logo)
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("MIAUtomotriz - Orden de Trabajo", 150f, 90f, paint)

        // Información General
        paint.textSize = 14f
        paint.isFakeBoldText = false
        
        // Bajamos el texto para que no choque con el logo (que ocupa hasta y=120 aprox)
        var currentY = 160f
        
        canvas.drawText("N° Orden: ${orden.numeroOrdenDeTrabajo}", 50f, currentY, paint)
        currentY += 20f
        canvas.drawText("Fecha: ${orden.fechaOrdenDeTrabajo}", 50f, currentY, paint)
        currentY += 20f
        canvas.drawText("Patente: ${orden.patente}", 50f, currentY, paint)
        currentY += 20f
        canvas.drawText("Estado: ${orden.estado}", 50f, currentY, paint)
        currentY += 40f

        // Observaciones
        paint.isFakeBoldText = true
        canvas.drawText("Observaciones:", 50f, currentY, paint)
        paint.isFakeBoldText = false
        currentY += 20f
        
        // Manejo básico de texto largo (multilínea simple)
        val lineas = (orden.observaciones ?: "").chunked(60) // Cortar cada 60 caracteres aprox
        for (linea in lineas) {
            canvas.drawText(linea, 50f, currentY, paint)
            currentY += 20f
        }

        // Detalles Técnicos (Causas y Repuestos)
        currentY += 20f
        paint.isFakeBoldText = true
        canvas.drawText("Detalles Técnicos:", 50f, currentY, paint)
        paint.isFakeBoldText = false
        currentY += 20f
        
        val lineasDetalle = detalles.split("\n")
        for (linea in lineasDetalle) {
            canvas.drawText(linea, 50f, currentY, paint)
            currentY += 20f
        }

        pdfDocument.finishPage(page)

        // Guardar archivo usando MediaStore (Compatible con Android 10+)
        val fileName = "Orden_${orden.numeroOrdenDeTrabajo}_${System.currentTimeMillis()}.pdf"
        
        try {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                val outputStream = resolver.openOutputStream(uri)
                if (outputStream != null) {
                    pdfDocument.writeTo(outputStream)
                    outputStream.close()
                    Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Error: No se pudo abrir el stream de escritura", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Error al crear archivo en MediaStore", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al guardar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }
}
