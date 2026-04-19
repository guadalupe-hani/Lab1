package com.On_TimeHealth.backend.Controller

import com.On_TimeHealth.backend.Service.UsuarioService
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class RegistroPacienteRequest(
    val nombre: String, val apellido: String, val email: String, val password: String,
    val dni: String, val telefono: String?, val obraSocial: String?, val plan: String?
)

data class RegistroMedicoRequest(
    val nombre: String, val apellido: String, val email: String, val password: String,
    val matricula: String, val especialidadId: Long?
)

data class RegistroAdministrativoRequest(
    val nombre: String, val apellido: String, val email: String, val password: String,
    val consultorioId: Long?
)

data class LoginRequest(val email: String, val password: String)

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5500"], allowCredentials = "true")
class UsuarioController(private val usuarioService: UsuarioService) {

    @PostMapping("/registrar/paciente")
    fun registrarPaciente(@RequestBody req: RegistroPacienteRequest): ResponseEntity<Any> {
        return try {
            val u = usuarioService.registrarPaciente(
                req.nombre, req.apellido, req.email, req.password,
                req.dni, req.telefono, req.obraSocial, req.plan
            )
            ResponseEntity.ok(mapOf("mensaje" to "Paciente creado", "id" to u.id, "email" to u.email))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/registrar/medico")
    fun registrarMedico(@RequestBody req: RegistroMedicoRequest): ResponseEntity<Any> {
        return try {
            val u = usuarioService.registrarMedico(
                req.nombre, req.apellido, req.email, req.password,
                req.matricula, req.especialidadId
            )
            ResponseEntity.ok(mapOf("mensaje" to "Médico creado", "id" to u.id, "email" to u.email))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/registrar/administrativo")
    fun registrarAdministrativo(@RequestBody req: RegistroAdministrativoRequest): ResponseEntity<Any> {
        return try {
            val u = usuarioService.registrarAdministrativo(
                req.nombre, req.apellido, req.email, req.password,
                req.consultorioId
            )
            ResponseEntity.ok(mapOf("mensaje" to "Administrativo creado", "id" to u.id, "email" to u.email))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest, session: HttpSession): ResponseEntity<Any> {
        return try {
            val u = usuarioService.login(req.email, req.password)
            session.setAttribute("usuarioId", u.id)
            session.setAttribute("usuarioEmail", u.email)
            session.setAttribute("usuarioRol", u.rol)
            ResponseEntity.ok(mapOf(
                "mensaje" to "Login exitoso",
                "id" to u.id, "nombre" to u.nombre, "apellido" to u.apellido,
                "email" to u.email, "rol" to u.rol
            ))
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
        return ResponseEntity.ok(mapOf(
            "id" to id,
            "email" to session.getAttribute("usuarioEmail"),
            "rol" to session.getAttribute("usuarioRol")
        ))
    }
}
