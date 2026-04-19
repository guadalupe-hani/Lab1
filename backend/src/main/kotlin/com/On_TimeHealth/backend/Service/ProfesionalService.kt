package com.On_TimeHealth.backend.Service

import com.On_TimeHealth.backend.Repository.HorarioTrabajoRepository
import com.On_TimeHealth.backend.Repository.ProfesionalRepository
import org.springframework.stereotype.Service

@Service
class ProfesionalService(
    private val profesionalRepository: ProfesionalRepository,
    private val horarioRepository: HorarioTrabajoRepository
) {

    fun buscar(especialidadId: Long?, nombre: String?): List<Map<String, Any?>> {
        val todos = profesionalRepository.findAll()
        val filtrados = todos.filter { p ->
            val matchEsp = especialidadId == null || p.especialidad?.id == especialidadId
            val nombreCompleto = "${p.usuario?.nombre ?: ""} ${p.usuario?.apellido ?: ""}".lowercase()
            val matchNombre = nombre.isNullOrBlank() || nombreCompleto.contains(nombre.lowercase())
            matchEsp && matchNombre
        }
        return filtrados.map { p ->
            val horarios = horarioRepository.findByProfesionalId(p.id!!)
            val consultorios = horarios.mapNotNull { it.consultorio }
                .distinctBy { it.id }
                .map { mapOf("id" to it.id, "nombre" to it.nombre, "direccion" to it.direccion) }
            mapOf(
                "id" to p.id,
                "nombre" to "${p.usuario?.nombre ?: ""} ${p.usuario?.apellido ?: ""}".trim(),
                "matricula" to p.matricula,
                "especialidad" to p.especialidad?.nombre,
                "consultorios" to consultorios,
                "horarios" to horarios.map {
                    mapOf(
                        "diaSemana" to it.diaSemana,
                        "horaInicio" to it.horaInicio?.toString(),
                        "horaFin" to it.horaFin?.toString(),
                        "consultorioNombre" to it.consultorio?.nombre
                    )
                }
            )
        }
    }
}
