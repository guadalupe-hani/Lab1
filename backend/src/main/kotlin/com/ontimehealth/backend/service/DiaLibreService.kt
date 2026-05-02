package com.ontimehealth.backend.service

import com.ontimehealth.backend.model.DiasLibres
import com.ontimehealth.backend.repository.*
import com.ontimehealth.backend.repository.DiaLibreRepository
import com.ontimehealth.backend.repository.HorarioTrabajoRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DiaLibreService(
    private val diaLibreRepository: DiaLibreRepository,
    private val profesionalRepository: ProfesionalRepository,
    private val horarioRepository: HorarioTrabajoRepository,
    private val administrativoRepository: AdministrativoRepository
) {

    @Transactional
    fun crearComoMedico(usuarioId: Long, fecha: LocalDate, motivo: String?): DiasLibres {
        val profesional = profesionalRepository.findByUsuarioId(usuarioId)
            ?: throw IllegalArgumentException("Profesional no encontrado")
        return crear(profesional.id!!, fecha, motivo)
    }

    @Transactional
    fun crearComoAdmin(adminUsuarioId: Long, profesionalId: Long, fecha: LocalDate, motivo: String?): DiasLibres {
        val consultorioAdmin = obtenerConsultorioAdmin(adminUsuarioId)
        if (!horarioRepository.existsByProfesionalIdAndConsultorioId(profesionalId, consultorioAdmin)) {
            throw IllegalArgumentException("Ese profesional no atiende en tu consultorio")
        }
        return crear(profesionalId, fecha, motivo)
    }

    private fun crear(profesionalId: Long, fecha: LocalDate, motivo: String?): DiasLibres {
        if (diaLibreRepository.existsByProfesionalIdAndFecha(profesionalId, fecha)) {
            throw IllegalArgumentException("Ese profesional ya tiene ese día marcado como libre")
        }
        val profesional = profesionalRepository.findById(profesionalId).orElseThrow {
            IllegalArgumentException("Profesional no encontrado")
        }
        val diaLibre = DiasLibres().apply {
            this.profesional = profesional
            this.fecha = fecha
            this.motivo = motivo
        }
        return diaLibreRepository.save(diaLibre)
    }

    fun listarDeMedico(usuarioId: Long): List<DiasLibres> {
        val profesional = profesionalRepository.findByUsuarioId(usuarioId)
            ?: throw IllegalArgumentException("Profesional no encontrado")
        return diaLibreRepository.findByProfesionalIdOrderByFechaAsc(profesional.id!!)
    }

    fun listarDeProfesional(profesionalId: Long): List<DiasLibres> {
        return diaLibreRepository.findByProfesionalIdOrderByFechaAsc(profesionalId)
    }

    @Transactional
    fun eliminarComoMedico(usuarioId: Long, diaLibreId: Long) {
        val profesional = profesionalRepository.findByUsuarioId(usuarioId)
            ?: throw IllegalArgumentException("Profesional no encontrado")
        val diaLibre = diaLibreRepository.findById(diaLibreId).orElseThrow {
            IllegalArgumentException("Día libre no encontrado")
        }
        if (diaLibre.profesional?.id != profesional.id) {
            throw IllegalArgumentException("Este día libre no te pertenece")
        }
        diaLibreRepository.delete(diaLibre)
    }

    @Transactional
    fun eliminarComoAdmin(adminUsuarioId: Long, diaLibreId: Long) {
        val consultorioAdmin = obtenerConsultorioAdmin(adminUsuarioId)
        val diaLibre = diaLibreRepository.findById(diaLibreId).orElseThrow {
            IllegalArgumentException("Día libre no encontrado")
        }
        val profId = diaLibre.profesional?.id
            ?: throw IllegalArgumentException("Día libre sin profesional")
        if (!horarioRepository.existsByProfesionalIdAndConsultorioId(profId, consultorioAdmin)) {
            throw IllegalArgumentException("Ese profesional no atiende en tu consultorio")
        }
        diaLibreRepository.delete(diaLibre)
    }

    private fun obtenerConsultorioAdmin(adminUsuarioId: Long): Long {
        val admin = administrativoRepository.findByUsuarioId(adminUsuarioId)
            ?: throw IllegalArgumentException("Administrativo no encontrado")
        return admin.consultorio?.id
            ?: throw IllegalArgumentException("Tu cuenta no tiene consultorio asignado")
    }

    fun toMap(d: DiasLibres): Map<String, Any?> = mapOf(
        "id" to d.id,
        "profesionalId" to d.profesional?.id,
        "profesionalNombre" to "${d.profesional?.usuario?.nombre ?: ""} ${d.profesional?.usuario?.apellido ?: ""}".trim(),
        "fecha" to d.fecha?.toString(),
        "motivo" to d.motivo
    )
}
