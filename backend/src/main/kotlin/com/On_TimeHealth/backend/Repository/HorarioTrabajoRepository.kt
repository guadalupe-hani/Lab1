package com.On_TimeHealth.backend.Repository

import com.On_TimeHealth.backend.Model.HorariosTrabajo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HorarioTrabajoRepository : JpaRepository<HorariosTrabajo, Long> {
    fun findByProfesionalId(profesionalId: Long): List<HorariosTrabajo>
    fun findByProfesionalIdAndConsultorioId(profesionalId: Long, consultorioId: Long): List<HorariosTrabajo>
    fun findByConsultorioId(consultorioId: Long): List<HorariosTrabajo>
    fun existsByProfesionalIdAndConsultorioId(profesionalId: Long, consultorioId: Long): Boolean
}
