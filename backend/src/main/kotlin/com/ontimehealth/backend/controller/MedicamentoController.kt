package com.ontimehealth.backend.controller

import com.ontimehealth.backend.service.MedicamentoService
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class CrearMedicamentoRequest(val nombre: String, val descripcion: String?, val presentacion: String?)
data class EditarMedicamentoRequest(val nombre: String?, val descripcion: String?, val presentacion: String?, val activo: Boolean?)

@RestController
@RequestMapping("/api/medicamentos")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5500"], allowCredentials = "true")
class MedicamentoController(private val medicamentoService: MedicamentoService) {

    private fun sesion(session: HttpSession): Pair<Long, String>? {
        val id = session.getAttribute("usuarioId") as? Long ?: return null
        val rol = session.getAttribute("usuarioRol") as? String ?: return null
        return id to rol
    }

    private fun puedeEditar(rol: String) = rol == "MEDICO" || rol == "ADMINISTRATIVO"

    @GetMapping("")
    fun listar(session: HttpSession): ResponseEntity<Any> {
        sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return ResponseEntity.ok(medicamentoService.listarTodos().map { medicamentoService.toMap(it) })
    }

    @GetMapping("/activos")
    fun listarActivos(session: HttpSession): ResponseEntity<Any> {
        sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return ResponseEntity.ok(medicamentoService.listarActivos().map { medicamentoService.toMap(it) })
    }

    @PostMapping("")
    fun crear(@RequestBody req: CrearMedicamentoRequest, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (!puedeEditar(s.second)) return ResponseEntity.status(403).body(mapOf("error" to "Solo médicos o administrativos"))
        return try {
            val m = medicamentoService.crear(req.nombre, req.descripcion, req.presentacion)
            ResponseEntity.ok(medicamentoService.toMap(m))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}")
    fun editar(@PathVariable id: Long, @RequestBody req: EditarMedicamentoRequest, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (!puedeEditar(s.second)) return ResponseEntity.status(403).body(mapOf("error" to "Solo médicos o administrativos"))
        return try {
            val m = medicamentoService.editar(id, req.nombre, req.descripcion, req.presentacion, req.activo)
            ResponseEntity.ok(medicamentoService.toMap(m))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Long, session: HttpSession): ResponseEntity<Any> {
        val s = sesion(session) ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        if (!puedeEditar(s.second)) return ResponseEntity.status(403).body(mapOf("error" to "Solo médicos o administrativos"))
        return try {
            medicamentoService.eliminar(id)
            ResponseEntity.ok(mapOf("mensaje" to "Medicamento dado de baja"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
