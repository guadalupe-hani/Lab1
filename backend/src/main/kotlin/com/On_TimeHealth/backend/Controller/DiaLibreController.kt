package com.On_TimeHealth.backend.Controller

import com.On_TimeHealth.backend.Service.DiaLibreService
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

data class CrearDiaLibreMedicoRequest(val fecha: String, val motivo: String?)
data class CrearDiaLibreAdminRequest(val profesionalId: Long, val fecha: String, val motivo: String?)

@RestController
@RequestMapping("/api/dias-libres")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5500"], allowCredentials = "true")
class DiaLibreController(private val diaLibreService: DiaLibreService) {

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
            ResponseEntity.ok(diaLibreService.listarDeMedico(s.first).map { diaLibreService.toMap(it) })
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/profesional/{id}")
    fun listarDeProfesional(@PathVariable id: Long, session: HttpSession): ResponseEntity<Any> {
        sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return ResponseEntity.ok(diaLibreService.listarDeProfesional(id).map { diaLibreService.toMap(it) })
    }

    @PostMapping("/medico")
    fun crearComoMedico(@RequestBody req: CrearDiaLibreMedicoRequest, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (s.second != "MEDICO") return ResponseEntity.status(403).body(mapOf("error" to "Solo médicos"))
        return try {
            val d = diaLibreService.crearComoMedico(s.first, LocalDate.parse(req.fecha), req.motivo)
            ResponseEntity.ok(diaLibreService.toMap(d))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/admin")
    fun crearComoAdmin(@RequestBody req: CrearDiaLibreAdminRequest, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (s.second != "ADMINISTRATIVO") return ResponseEntity.status(403).body(mapOf("error" to "Solo administrativos"))
        return try {
            val d = diaLibreService.crearComoAdmin(s.first, req.profesionalId, LocalDate.parse(req.fecha), req.motivo)
            ResponseEntity.ok(diaLibreService.toMap(d))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Long, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return try {
            when (s.second) {
                "MEDICO" -> diaLibreService.eliminarComoMedico(s.first, id)
                "ADMINISTRATIVO" -> diaLibreService.eliminarComoAdmin(s.first, id)
                else -> return ResponseEntity.status(403).body(mapOf("error" to "Rol no autorizado"))
            }
            ResponseEntity.ok(mapOf("mensaje" to "Día libre eliminado"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
