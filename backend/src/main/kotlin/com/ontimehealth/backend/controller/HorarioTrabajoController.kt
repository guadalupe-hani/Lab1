package com.ontimehealth.backend.controller

import com.ontimehealth.backend.service.HorarioTrabajoService
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalTime

data class CrearHorarioMedicoRequest(
    val consultorioId: Long,
    val diaSemana: String,
    val horaInicio: String,
    val horaFin: String
)

data class CrearHorarioAdminRequest(
    val profesionalId: Long,
    val diaSemana: String,
    val horaInicio: String,
    val horaFin: String
)

@RestController
@RequestMapping("/api/horarios")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5500"], allowCredentials = "true")
class HorarioTrabajoController(private val horarioService: HorarioTrabajoService) {

    private fun sesion(session: HttpSession): Pair<Long, String>? {
        val id = session.getAttribute("usuarioId") as? Long ?: return null
        val rol = session.getAttribute("usuarioRol") as? String ?: return null
        return id to rol
    }

    @GetMapping("/mios")
    fun listarMios(session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (s.second != "MEDICO") return ResponseEntity.status(403).body(mapOf("error" to "Solo médicos"))
        return try {
            ResponseEntity.ok(horarioService.listarDeMedico(s.first).map { horarioService.toMap(it) })
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/profesional/{id}")
    fun listarDeProfesional(@PathVariable id: Long, session: HttpSession): ResponseEntity<Any> {
        sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return ResponseEntity.ok(horarioService.listarDeProfesional(id).map { horarioService.toMap(it) })
    }

    @GetMapping("/medicos-del-consultorio")
    fun medicosDelConsultorio(session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (s.second != "ADMINISTRATIVO") return ResponseEntity.status(403).body(mapOf("error" to "Solo administrativos"))
        return try {
            ResponseEntity.ok(horarioService.listarMedicosDeAdmin(s.first))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/medico")
    fun crearComoMedico(@RequestBody req: CrearHorarioMedicoRequest, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (s.second != "MEDICO") return ResponseEntity.status(403).body(mapOf("error" to "Solo médicos"))
        return try {
            val h = horarioService.crearComoMedico(
                s.first, req.consultorioId, req.diaSemana,
                LocalTime.parse(req.horaInicio), LocalTime.parse(req.horaFin)
            )
            ResponseEntity.ok(horarioService.toMap(h))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/admin")
    fun crearComoAdmin(@RequestBody req: CrearHorarioAdminRequest, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (s.second != "ADMINISTRATIVO") return ResponseEntity.status(403).body(mapOf("error" to "Solo administrativos"))
        return try {
            val h = horarioService.crearComoAdmin(
                s.first, req.profesionalId, req.diaSemana,
                LocalTime.parse(req.horaInicio), LocalTime.parse(req.horaFin)
            )
            ResponseEntity.ok(horarioService.toMap(h))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Long, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return try {
            when (s.second) {
                "MEDICO" -> horarioService.eliminarComoMedico(s.first, id)
                "ADMINISTRATIVO" -> horarioService.eliminarComoAdmin(s.first, id)
                else -> return ResponseEntity.status(403).body(mapOf("error" to "Rol no autorizado"))
            }
            ResponseEntity.ok(mapOf("mensaje" to "Horario eliminado"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
