package com.ontimehealth.backend.repository

import com.ontimehealth.backend.model.Turnos
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TurnoRepository : JpaRepository<Turnos, Long> {
    fun findByProfesionalIdAndFechaBetween(profesionalId: Long, desde: LocalDate, hasta: LocalDate): List<Turnos>
    fun findByProfesionalIdOrderByFechaDescHoraDesc(profesionalId: Long): List<Turnos>
    fun findByPacienteIdOrderByFechaDescHoraDesc(pacienteId: Long): List<Turnos>
    fun findByConsultorioIdOrderByFechaDescHoraDesc(consultorioId: Long): List<Turnos>
}
