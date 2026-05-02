package com.ontimehealth.backend.repository

import com.ontimehealth.backend.model.HorariosTrabajo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HorarioTrabajoRepository : JpaRepository<HorariosTrabajo, Long> {
    fun findByProfesionalId(profesionalId: Long): List<HorariosTrabajo>
    fun findByProfesionalIdAndConsultorioId(profesionalId: Long, consultorioId: Long): List<HorariosTrabajo>
    fun findByConsultorioId(consultorioId: Long): List<HorariosTrabajo>
    fun existsByProfesionalIdAndConsultorioId(profesionalId: Long, consultorioId: Long): Boolean
}
