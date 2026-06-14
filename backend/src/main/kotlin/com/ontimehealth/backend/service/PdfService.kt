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

    @Suppress("UNCHECKED_CAST")
    fun generarEstadisticasPdf(stats: Map<String, Any?>): ByteArray {
        val out = ByteArrayOutputStream()
        val document = Document(PageSize.A4)
        PdfWriter.getInstance(document, out)
        document.open()

        val tituloFont = Font(Font.HELVETICA, 18f, Font.BOLD)
        val subFont    = Font(Font.HELVETICA, 13f, Font.BOLD)
        val normalFont = Font(Font.HELVETICA, 11f)
        val boldFont   = Font(Font.HELVETICA, 11f, Font.BOLD)

        val medico  = stats["medico"]  as Map<String, Any?>
        val resumen = stats["resumen"] as Map<String, Any?>
        val turnos  = stats["turnos"]  as List<Map<String, Any?>>

        document.add(Paragraph("On-Time Health", tituloFont))
        document.add(Paragraph("Estadísticas Médicas", subFont))
        document.add(Paragraph(" "))
        document.add(Paragraph("Médico: Dr/a. ${medico["nombre"]}", normalFont))
        document.add(Paragraph("Matrícula: ${medico["matricula"]}", normalFont))
        document.add(Paragraph("Especialidad: ${medico["especialidad"]}", normalFont))
        document.add(Paragraph("Fecha de generación: ${java.time.LocalDate.now()}", normalFont))
        document.add(Paragraph(" "))
        document.add(Paragraph("─────────────────────────────────────", normalFont))
        document.add(Paragraph(" "))
        document.add(Paragraph("RESUMEN", subFont))
        document.add(Paragraph(" "))
        document.add(Paragraph("Total de turnos: ${resumen["total"]}", normalFont))
        document.add(Paragraph("Atendidos: ${resumen["atendidos"]}", normalFont))
        document.add(Paragraph("Ausentes: ${resumen["ausentes"]}", normalFont))
        document.add(Paragraph("Pendientes (programados): ${resumen["programados"]}", normalFont))
        document.add(Paragraph(" "))
        document.add(Paragraph("Cancelaciones:", boldFont))
        document.add(Paragraph("  - Por el médico: ${resumen["canceladosMedico"]}", normalFont))
        document.add(Paragraph("  - Por el paciente: ${resumen["canceladosPaciente"]}", normalFont))
        document.add(Paragraph("  - Por administrativo: ${resumen["canceladosAdmin"]}", normalFont))
        document.add(Paragraph(" "))
        document.add(Paragraph("─────────────────────────────────────", normalFont))
        document.add(Paragraph(" "))
        document.add(Paragraph("DETALLE DE TURNOS", subFont))
        document.add(Paragraph(" "))

        turnos.take(100).forEach { t ->
            val estadoTexto = when {
                t["estado"] == "CANCELADO" -> {
                    val por = mapOf("MEDICO" to "por el médico", "PACIENTE" to "por el paciente", "ADMINISTRATIVO" to "por administrativo")
                    "Cancelado ${por[t["canceladoPor"]] ?: ""}"
                }
                t["estadoPaciente"] == "ATENDIDO"   -> "Atendido"
                t["estadoPaciente"] == "AUSENTE"    -> "Ausente"
                else -> "Programado"
            }
            val hora = (t["hora"] as? String)?.take(5) ?: ""
            document.add(Paragraph("${t["fecha"]}  $hora  ${t["pacienteNombre"]}  —  $estadoTexto", normalFont))
        }

        document.close()
        return out.toByteArray()
    }
}