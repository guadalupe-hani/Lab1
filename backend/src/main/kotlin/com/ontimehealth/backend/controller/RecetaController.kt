package com.ontimehealth.backend.controller

import com.ontimehealth.backend.service.ItemRecetaData
import com.ontimehealth.backend.service.RecetaService
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class ItemRecetaRequest(
    val medicamentoId: Long,
    val dosis: String,
    val duracion: String?,
    val indicaciones: String?
)

data class CrearRecetaRequest(
    val dniPaciente: String,
    val items: List<ItemRecetaRequest>,
    val indicacionesGenerales: String?
)

@RestController
@RequestMapping("/api/recetas")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5500"], allowCredentials = "true")
class RecetaController(private val recetaService: RecetaService) {

    private fun sesion(session: HttpSession): Pair<Long, String>? {
        val id = session.getAttribute("usuarioId") as? Long ?: return null
        val rol = session.getAttribute("usuarioRol") as? String ?: return null
        return id to rol
    }

    @PostMapping("")
    fun crear(@RequestBody req: CrearRecetaRequest, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (s.second != "MEDICO") return ResponseEntity.status(403).body(mapOf("error" to "Solo un médico puede crear recetas"))
        return try {
            val items = req.items.map { ItemRecetaData(it.medicamentoId, it.dosis, it.duracion, it.indicaciones) }
            val r = recetaService.crear(s.first, req.dniPaciente, items, req.indicacionesGenerales)
            ResponseEntity.ok(recetaService.toMap(r))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/mias")
    fun listarMias(session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return try {
            val lista = when (s.second) {
                "PACIENTE" -> recetaService.listarDePaciente(s.first)
                "MEDICO" -> recetaService.listarDeMedico(s.first)
                else -> return ResponseEntity.status(403).body(mapOf("error" to "Rol no autorizado"))
            }
            ResponseEntity.ok(lista.map { recetaService.toMap(it) })
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/{id}")
    fun obtener(@PathVariable id: Long, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return try {
            val r = recetaService.obtener(id, s.first, s.second)
            ResponseEntity.ok(recetaService.toMap(r))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Long, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (s.second != "MEDICO") return ResponseEntity.status(403).body(mapOf("error" to "Solo un médico puede eliminar recetas"))
        return try {
            recetaService.eliminar(id, s.first)
            ResponseEntity.ok(mapOf("mensaje" to "Receta eliminada"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
