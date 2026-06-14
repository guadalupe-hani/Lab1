package com.ontimehealth.backend.controller

import com.ontimehealth.backend.repository.PacienteRepository
import com.ontimehealth.backend.repository.RecetaRepository
import com.ontimehealth.backend.repository.TurnoRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/historial")
@CrossOrigin(origins = ["http://localhost:5173"], allowCredentials = "true")
class HistorialController(
    private val pacienteRepository: PacienteRepository,
    private val turnoRepository: TurnoRepository,
    private val recetaRepository: RecetaRepository
) {

    @GetMapping("/mio")
    fun miHistorial(@SessionAttribute("usuarioId") usuarioId: Long): ResponseEntity<Any> {
        val paciente = pacienteRepository.findByUsuarioId(usuarioId)
            ?: return ResponseEntity.status(403).body(mapOf("error" to "No sos paciente"))
        return ResponseEntity.ok(armarHistorial(paciente.usuario?.id!!))
    }

    @GetMapping("/paciente/{pacienteId}")
    fun historialDePaciente(
        @PathVariable pacienteId: Long,
        @SessionAttribute("usuarioRol") usuarioRol: String
    ): ResponseEntity<Any> {
        if (usuarioRol != "MEDICO") {
            return ResponseEntity.status(403).body(mapOf("error" to "No autorizado"))
        }
        val paciente = pacienteRepository.findById(pacienteId).orElse(null)
            ?: return ResponseEntity.status(404).body(mapOf("error" to "Paciente no encontrado"))
        return ResponseEntity.ok(armarHistorial(paciente.usuario?.id!!))
    }

    private fun armarHistorial(usuarioId: Long): Map<String, Any?> {
        val paciente = pacienteRepository.findByUsuarioId(usuarioId)!!

        val turnos = turnoRepository.findByPacienteIdOrderByFechaDescHoraDesc(paciente.id!!)
            .map { t -> mapOf(
                "id" to t.id,
                "fecha" to t.fecha.toString(),
                "hora" to t.hora?.toString()?.substring(0, 5),
                "medico" to "Dr/a. ${t.profesional?.usuario?.nombre} ${t.profesional?.usuario?.apellido}",
                "consultorio" to t.consultorio?.nombre,
                "estado" to t.estado
            )}

        val recetas = recetaRepository.findByPacienteIdOrderByFechaDesc(paciente.id!!)
            .map { r -> mapOf(
                "id" to r.id,
                "fecha" to r.fecha.toString(),
                "medico" to "Dr/a. ${r.profesional?.usuario?.nombre} ${r.profesional?.usuario?.apellido}",
                "contenido" to r.contenido,
                "items" to r.items.map { i -> mapOf(
                    "medicamento" to i.medicamento?.nombre,
                    "dosis" to i.dosis,
                    "duracion" to i.duracion,
                    "indicaciones" to i.indicaciones
                )}
            )}

        return mapOf(
            "paciente" to mapOf(
                "nombre" to paciente.usuario?.nombre,
                "apellido" to paciente.usuario?.apellido,
                "dni" to paciente.dni,
                "obraSocial" to paciente.obraSocial,
                "plan" to paciente.plan,
                "telefono" to paciente.telefono
            ),
            "turnos" to turnos,
            "recetas" to recetas
        )
    }
}