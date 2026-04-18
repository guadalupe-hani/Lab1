package com.On_TimeHealth.backend.Controller

import com.On_TimeHealth.backend.Service.UsuarioService
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class RegistroRequest(val nombre: String, val apellido: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = ["http://localhost:3000", "http://127.0.0.1:5500"], allowCredentials = "true")
class UsuarioController(private val usuarioService: UsuarioService) {

    @PostMapping("/registrar")
    fun registrar(@RequestBody req: RegistroRequest): ResponseEntity<Any> {
        return try {
            val usuario = usuarioService.registrar(req.nombre, req.apellido, req.email, req.password)
            ResponseEntity.ok(mapOf("mensaje" to "Usuario creado", "id" to usuario.id, "email" to usuario.email))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest, session: HttpSession): ResponseEntity<Any> {
        return try {
            val usuario = usuarioService.login(req.email, req.password)
            session.setAttribute("usuarioId", usuario.id)
            session.setAttribute("usuarioEmail", usuario.email)
            ResponseEntity.ok(mapOf("mensaje" to "Login exitoso", "id" to usuario.id, "nombre" to usuario.nombre, "email" to usuario.email))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(401).body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/logout")
    fun logout(session: HttpSession): ResponseEntity<Any> {
        session.invalidate()
        return ResponseEntity.ok(mapOf("mensaje" to "Sesión cerrada"))
    }

    @DeleteMapping("/eliminar")
    fun eliminar(session: HttpSession): ResponseEntity<Any> {
        val id = session.getAttribute("usuarioId") as? Long
            ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        return try {
            usuarioService.eliminar(id)
            session.invalidate()
            ResponseEntity.ok(mapOf("mensaje" to "Cuenta eliminada"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/sesion")
    fun sesionActual(session: HttpSession): ResponseEntity<Any> {
        val id = session.getAttribute("usuarioId")
            ?: return ResponseEntity.status(401).body(mapOf("error" to "No hay sesión activa"))
        val email = session.getAttribute("usuarioEmail")
        return ResponseEntity.ok(mapOf("id" to id, "email" to email))
    }
}
