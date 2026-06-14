package com.ontimehealth.backend.service

import com.ontimehealth.backend.repository.ProfesionalRepository
import com.ontimehealth.backend.repository.TurnoRepository
import org.springframework.stereotype.Service

@Service
class EstadisticasService(
    private val profesionalRepository: ProfesionalRepository,
    private val turnoRepository: TurnoRepository
) {
    fun obtenerEstadisticas(rol: String, profesionalId: Long): Map<String, Any?> {
        if (rol != "ADMINISTRATIVO") throw IllegalArgumentException("Solo administrativos")
        val profesional = profesionalRepository.findById(profesionalId).orElse(null)
            ?: throw IllegalArgumentException("Médico no encontrado")
        val turnos = turnoRepository.findByProfesionalIdOrderByFechaDescHoraDesc(profesionalId)

        return mapOf(
            "medico" to mapOf(
                "id" to profesional.id,
                "nombre" to "${profesional.usuario?.nombre ?: ""} ${profesional.usuario?.apellido ?: ""}".trim(),
                "matricula" to (profesional.matricula ?: "-"),
                "especialidad" to (profesional.especialidad?.nombre ?: "-")
            ),
            "resumen" to mapOf(
                "total" to turnos.size,
                "atendidos" to turnos.count { it.estadoPaciente == "ATENDIDO" },
                "ausentes" to turnos.count { it.estadoPaciente == "AUSENTE" },
                "programados" to turnos.count { it.estado == "PROGRAMADO" },
                "canceladosMedico" to turnos.count { it.estado == "CANCELADO" && it.canceladoPor == "MEDICO" },
                "canceladosPaciente" to turnos.count { it.estado == "CANCELADO" && it.canceladoPor == "PACIENTE" },
                "canceladosAdmin" to turnos.count { it.estado == "CANCELADO" && it.canceladoPor == "ADMINISTRATIVO" }
            ),
            "turnos" to turnos.map { t ->
                mapOf(
                    "id" to t.id,
                    "fecha" to t.fecha?.toString(),
                    "hora" to t.hora?.toString(),
                    "pacienteNombre" to "${t.paciente?.usuario?.nombre ?: ""} ${t.paciente?.usuario?.apellido ?: ""}".trim(),
                    "estado" to (t.estado ?: ""),
                    "estadoPaciente" to (t.estadoPaciente ?: ""),
                    "canceladoPor" to (t.canceladoPor ?: "")
                )
            }
        )
    }
}