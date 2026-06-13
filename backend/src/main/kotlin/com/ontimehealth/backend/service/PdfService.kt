package com.ontimehealth.backend.service

import com.lowagie.text.Document
import com.lowagie.text.Font
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import com.ontimehealth.backend.model.Recetas
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class PdfService {

    fun generarRecetaPdf(receta: Recetas): ByteArray {
        val out = ByteArrayOutputStream()
        val document = Document(PageSize.A4)
        PdfWriter.getInstance(document, out)
        document.open()

        val tituloFont = Font(Font.HELVETICA, 18f, Font.BOLD)
        val subFont = Font(Font.HELVETICA, 12f, Font.BOLD)
        val normalFont = Font(Font.HELVETICA, 11f)
        val firmaFont = Font(Font.HELVETICA, 11f, Font.ITALIC)

        val paciente = receta.paciente
        val profesional = receta.profesional

        document.add(Paragraph("On-Time Health", tituloFont))
        document.add(Paragraph("Receta médica #${receta.id}", subFont))
        document.add(Paragraph(" "))
        document.add(Paragraph("Fecha: ${receta.fecha}", normalFont))
        document.add(Paragraph(
            "Paciente: ${paciente?.usuario?.nombre ?: ""} ${paciente?.usuario?.apellido ?: ""} (DNI ${paciente?.dni ?: "-"})",
            normalFont
        ))
        document.add(Paragraph("Obra social: ${receta.obraSocial ?: "-"}", normalFont))
        document.add(Paragraph(
            "Médico: ${profesional?.usuario?.nombre ?: ""} ${profesional?.usuario?.apellido ?: ""} - Matrícula ${profesional?.matricula ?: "-"}",
            normalFont
        ))
        document.add(Paragraph(" "))

        document.add(Paragraph("Medicamentos prescritos:", subFont))
        receta.items.forEach { item ->
            val presentacion = item.medicamento?.presentacion?.let { " - $it" } ?: ""
            document.add(Paragraph("- ${item.medicamento?.nombre ?: ""}$presentacion", normalFont))
            val duracion = item.duracion?.let { " | Duración: $it" } ?: ""
            document.add(Paragraph("   Dosis: ${item.dosis}$duracion", normalFont))
            if (!item.indicaciones.isNullOrBlank()) {
                document.add(Paragraph("   ${item.indicaciones}", normalFont))
            }
        }

        if (!receta.contenido.isNullOrBlank()) {
            document.add(Paragraph(" "))
            document.add(Paragraph("Indicaciones generales:", subFont))
            document.add(Paragraph(receta.contenido, normalFont))
        }

        document.add(Paragraph(" "))
        document.add(Paragraph(receta.firma ?: "", firmaFont))

        document.close()
        return out.toByteArray()
    }
}