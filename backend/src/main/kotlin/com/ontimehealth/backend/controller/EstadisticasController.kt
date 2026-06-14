package com.ontimehealth.backend.controller

import com.ontimehealth.backend.service.EstadisticasService
import com.ontimehealth.backend.service.PdfService
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/estadisticas")
@CrossOrigin(origins = ["http://localhost:5173"], allowCredentials = "true")
class EstadisticasController(
    private val estadisticasService: EstadisticasService,
    private val pdfService: PdfService
) {
    private fun sesion(session: HttpSession): Pair<Long, String>? {
        val id  = session.getAttribute("usuarioId")  as? Long   ?: return null
        val rol = session.getAttribute("usuarioRol") as? String ?: return null
        return id to rol
    }

    @GetMapping("/medico/{profesionalId}")
    fun estadisticas(@PathVariable profesionalId: Long, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (s.second != "ADMINISTRATIVO") return ResponseEntity.status(403).body(mapOf("error" to "Solo administrativos"))
        return try {
            ResponseEntity.ok(estadisticasService.obtenerEstadisticas(s.second, profesionalId))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/medico/{profesionalId}/pdf")
    fun pdf(@PathVariable profesionalId: Long, session: HttpSession): ResponseEntity<ByteArray> {
        val s = sesion(session) ?: return ResponseEntity.status(401).build()
        if (s.second != "ADMINISTRATIVO") return ResponseEntity.status(403).build()
        return try {
            val stats = estadisticasService.obtenerEstadisticas(s.second, profesionalId)
            val pdf   = pdfService.generarEstadisticasPdf(stats)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=estadisticas-medico-$profesionalId.pdf")
                .body(pdf)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
}