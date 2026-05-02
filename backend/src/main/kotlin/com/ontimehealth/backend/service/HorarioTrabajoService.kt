package com.ontimehealth.backend.service

import com.ontimehealth.backend.model.HorariosTrabajo
import com.ontimehealth.backend.repository.*
import com.ontimehealth.backend.repository.HorarioTrabajoRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalTime

@Service
class HorarioTrabajoService(
    private val horarioRepository: HorarioTrabajoRepository,
    private val profesionalRepository: ProfesionalRepository,
    private val consultorioRepository: ConsultorioRepository,
    private val administrativoRepository: AdministrativoRepository
) {

    @Transactional
    fun crearComoMedico(
        usuarioId: Long,
        consultorioId: Long,
        diaSemana: String,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ): HorariosTrabajo {
        val profesional = profesionalRepository.findByUsuarioId(usuarioId)
            ?: throw IllegalArgumentException("Profesional no encontrado")
        return crear(profesional.id!!, consultorioId, diaSemana, horaInicio, horaFin)
    }

    @Transactional
    fun crearComoAdmin(
        adminUsuarioId: Long,
        profesionalId: Long,
        diaSemana: String,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ): HorariosTrabajo {
        val consultorioAdmin = obtenerConsultorioAdmin(adminUsuarioId)
        return crear(profesionalId, consultorioAdmin, diaSemana, horaInicio, horaFin)
    }

    private fun crear(
        profesionalId: Long, consultorioId: Long, diaSemana: String,
        horaInicio: LocalTime, horaFin: LocalTime
    ): HorariosTrabajo {
        validarDia(diaSemana)
        if (!horaInicio.isBefore(horaFin)) {
            throw IllegalArgumentException("La hora de inicio debe ser menor a la de fin")
        }
        val profesional = profesionalRepository.findById(profesionalId).orElseThrow {
            IllegalArgumentException("Profesional no encontrado")
        }
        val consultorio = consultorioRepository.findById(consultorioId).orElseThrow {
            IllegalArgumentException("Consultorio no encontrado")
        }
        val horario = HorariosTrabajo().apply {
            this.profesional = profesional
            this.consultorio = consultorio
            this.diaSemana = diaSemana.uppercase()
            this.horaInicio = horaInicio
            this.horaFin = horaFin
        }
        return horarioRepository.save(horario)
    }

    fun listarDeMedico(usuarioId: Long): List<HorariosTrabajo> {
        val profesional = profesionalRepository.findByUsuarioId(usuarioId)
            ?: throw IllegalArgumentException("Profesional no encontrado")
        return horarioRepository.findByProfesionalId(profesional.id!!)
    }

    fun listarDeProfesional(profesionalId: Long): List<HorariosTrabajo> {
        return horarioRepository.findByProfesionalId(profesionalId)
    }

    @Transactional
    fun eliminarComoMedico(usuarioId: Long, horarioId: Long) {
        val profesional = profesionalRepository.findByUsuarioId(usuarioId)
            ?: throw IllegalArgumentException("Profesional no encontrado")
        val horario = horarioRepository.findById(horarioId).orElseThrow {
            IllegalArgumentException("Horario no encontrado")
        }
        if (horario.profesional?.id != profesional.id) {
            throw IllegalArgumentException("Este horario no te pertenece")
        }
        horarioRepository.delete(horario)
    }

    @Transactional
    fun eliminarComoAdmin(adminUsuarioId: Long, horarioId: Long) {
        val consultorioAdmin = obtenerConsultorioAdmin(adminUsuarioId)
        val horario = horarioRepository.findById(horarioId).orElseThrow {
            IllegalArgumentException("Horario no encontrado")
        }
        if (horario.consultorio?.id != consultorioAdmin) {
            throw IllegalArgumentException("Solo podés gestionar horarios de tu consultorio")
        }
        horarioRepository.delete(horario)
    }

    private fun obtenerConsultorioAdmin(adminUsuarioId: Long): Long {
        val admin = administrativoRepository.findByUsuarioId(adminUsuarioId)
            ?: throw IllegalArgumentException("Administrativo no encontrado")
        return admin.consultorio?.id
            ?: throw IllegalArgumentException("Tu cuenta no tiene consultorio asignado")
    }

    private fun validarDia(dia: String) {
        val validos = setOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO")
        if (dia.uppercase() !in validos) {
            throw IllegalArgumentException("Día inválido. Usá LUNES..DOMINGO")
        }
    }

    fun listarMedicosDeAdmin(adminUsuarioId: Long): List<Map<String, Any?>> {
        val consultorioId = obtenerConsultorioAdmin(adminUsuarioId)
        val horarios = horarioRepository.findByConsultorioId(consultorioId)
        val profesionalesUnicos = horarios.mapNotNull { it.profesional }
            .distinctBy { it.id }
        return profesionalesUnicos.map { p ->
            mapOf(
                "id" to p.id,
                "nombre" to "${p.usuario?.nombre ?: ""} ${p.usuario?.apellido ?: ""}".trim(),
                "matricula" to p.matricula,
                "especialidad" to p.especialidad?.nombre
            )
        }
    }

    fun toMap(h: HorariosTrabajo): Map<String, Any?> = mapOf(
        "id" to h.id,
        "profesionalId" to h.profesional?.id,
        "profesionalNombre" to "${h.profesional?.usuario?.nombre ?: ""} ${h.profesional?.usuario?.apellido ?: ""}".trim(),
        "consultorioId" to h.consultorio?.id,
        "consultorioNombre" to h.consultorio?.nombre,
        "diaSemana" to h.diaSemana,
        "horaInicio" to h.horaInicio?.toString(),
        "horaFin" to h.horaFin?.toString()
    )
}
