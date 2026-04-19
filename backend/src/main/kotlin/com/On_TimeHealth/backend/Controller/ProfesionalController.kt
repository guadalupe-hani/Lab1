package com.On_TimeHealth.backend.Controller

import com.On_TimeHealth.backend.Service.ProfesionalService
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/profesionales")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5500"], allowCredentials = "true")
class ProfesionalController(private val profesionalService: ProfesionalService) {

    @GetMapping("/buscar")
    fun buscar(
        @RequestParam(required = false) especialidadId: Long?,
        @RequestParam(required = false) nombre: String?,
        session: HttpSession
    ): ResponseEntity<Any> {
        val id = session.getAttribute("usuarioId") ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return ResponseEntity.ok(profesionalService.buscar(especialidadId, nombre))
    }
}
