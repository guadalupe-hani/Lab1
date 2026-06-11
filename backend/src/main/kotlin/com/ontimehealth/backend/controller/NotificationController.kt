package com.ontimehealth.backend.controller

import com.ontimehealth.backend.service.NotificacionService
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5500"], allowCredentials = "true")
class NotificacionController(private val notificacionService: NotificacionService) {

    private fun userId(session: HttpSession): Long? =
        session.getAttribute("usuarioId") as? Long

    @GetMapping
    fun listar(session: HttpSession): ResponseEntity<Any> {
        val id = userId(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No autenticado"))
        return ResponseEntity.ok(notificacionService.listar(id))
    }

    @GetMapping("/no-leidas")
    fun contarNoLeidas(session: HttpSession): ResponseEntity<Any> {
        val id = userId(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No autenticado"))
        return ResponseEntity.ok(mapOf("count" to notificacionService.contarNoLeidas(id)))
    }

    @PutMapping("/{id}/leer")
    fun marcarLeida(@PathVariable id: Long, session: HttpSession): ResponseEntity<Any> {
        val userId = userId(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No autenticado"))
        notificacionService.marcarLeida(id, userId)
        return ResponseEntity.ok(mapOf("ok" to true))
    }

    @PutMapping("/leer-todas")
    fun marcarTodasLeidas(session: HttpSession): ResponseEntity<Any> {
        val id = userId(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No autenticado"))
        notificacionService.marcarTodasLeidas(id)
        return ResponseEntity.ok(mapOf("ok" to true))
    }
}