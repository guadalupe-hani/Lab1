package com.ontimehealth.backend.controller

import com.ontimehealth.backend.service.TurnoService
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalTime

data class AgendarPacienteRequest(val profesionalId: Long, val fecha: String, val hora: String)
data class AgendarAdminRequest(val dniPaciente: String, val profesionalId: Long, val fecha: String, val hora: String)
data class CancelarRequest(val motivo: String?)

@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5500"], allowCredentials = "true")
class TurnoController(private val turnoService: TurnoService) {

    private fun sesion(session: HttpSession): Pair<Long, String>? {
        val id = session.getAttribute("usuarioId") as? Long ?: return null
        val rol = session.getAttribute("usuarioRol") as? String ?: return null
        return id to rol
    }

    @GetMapping("/disponibilidad/{profesionalId}")
    fun disponibilidad(
        @PathVariable profesionalId: Long,
        @RequestParam(required = false) desde: String?,
        @RequestParam(required = false) hasta: String?,
        session: HttpSession
    ): ResponseEntity<Any> {
        sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return try {
            val d = if (!desde.isNullOrBlank()) LocalDate.parse(desde) else LocalDate.now()
            val h = if (!hasta.isNullOrBlank()) LocalDate.parse(hasta) else LocalDate.now().plusDays(30)
            ResponseEntity.ok(turnoService.calcularDisponibilidad(profesionalId, d, h))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/paciente")
    fun agendarComoPaciente(@RequestBody req: AgendarPacienteRequest, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (s.second != "PACIENTE") return ResponseEntity.status(403).body(mapOf("error" to "Solo pacientes"))
        return try {
            val t = turnoService.agendarComoPaciente(
                s.first, req.profesionalId, LocalDate.parse(req.fecha), LocalTime.parse(req.hora)
            )
            ResponseEntity.ok(turnoService.toMap(t))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/admin")
    fun agendarComoAdmin(@RequestBody req: AgendarAdminRequest, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (s.second != "ADMINISTRATIVO") return ResponseEntity.status(403).body(mapOf("error" to "Solo administrativos"))
        return try {
            val t = turnoService.agendarComoAdmin(
                s.first, req.dniPaciente, req.profesionalId, LocalDate.parse(req.fecha), LocalTime.parse(req.hora)
            )
            ResponseEntity.ok(turnoService.toMap(t))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}/cancelar")
    fun cancelar(@PathVariable id: Long, @RequestBody(required = false) req: CancelarRequest?, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return try {
            val t = turnoService.cancelar(s.first, s.second, id, req?.motivo)
            ResponseEntity.ok(turnoService.toMap(t))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/mios")
    fun listarMios(session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return try {
            val lista = when (s.second) {
                "PACIENTE" -> turnoService.listarDePaciente(s.first)
                "MEDICO" -> turnoService.listarDeMedico(s.first)
                "ADMINISTRATIVO" -> turnoService.listarDeAdmin(s.first)
                else -> return ResponseEntity.status(403).body(mapOf("error" to "Rol no autorizado"))
            }
            ResponseEntity.ok(lista.map { turnoService.toMap(it) })
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
