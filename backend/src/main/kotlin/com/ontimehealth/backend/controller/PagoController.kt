package com.ontimehealth.backend.controller;

import com.ontimehealth.backend.service.PagoService
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5500"], allowCredentials = "true")

public class PagoController (private val pagoService: PagoService){

    @PostMapping("/turno/{id}/preferencia")
    fun crearPreferencia(@PathVariable id: Long, session: HttpSession): ResponseEntity<Any> {
        val usuarioId = session.getAttribute("usuarioId") as? Long
                                                              ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        val rol = session.getAttribute("usuarioRol") as? String
        if (rol != "PACIENTE") return ResponseEntity.status(403).body(mapOf("error" to "Solo pacientes"))

        return try {
            val initPoint = pagoService.crearPreferencia(usuarioId, id)
            ResponseEntity.ok(mapOf("initPoint" to initPoint))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/turno/{id}/confirmar")
    fun confirmarPago(
            @PathVariable id: Long,
            @RequestParam(required = false) status: String?,
            session: HttpSession
    ): ResponseEntity<Any> {
        val usuarioId = session.getAttribute("usuarioId") as? Long
                ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))

        return try {
            pagoService.confirmarPagoPorRedireccion(usuarioId, id, status)
            ResponseEntity.ok(mapOf("ok" to true))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/webhook")
    fun webhook(
            @RequestParam(required = false) topic: String?,
            @RequestParam(required = false) type: String?,
            @RequestParam(required = false) id: String?,
            @RequestParam("data.id", required = false) dataId: String?,
            @RequestBody(required = false) body: Map<String, Any>?
    ): ResponseEntity<Any> {
        val tipo = type ?: topic
        @Suppress("UNCHECKED_CAST")
        val paymentId = dataId ?: id ?: (body?.get("data") as? Map<String, Any>)?.get("id")?.toString()

        if (tipo == "payment" && paymentId != null) {
            try {
                pagoService.procesarNotificacion(paymentId.toLong())
            } catch (e: Exception) {
                println("Error procesando notificación de pago $paymentId: ${e.message}")
            }
        }
        return ResponseEntity.ok().build()
    }
}
