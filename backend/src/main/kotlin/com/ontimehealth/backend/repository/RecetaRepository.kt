package com.ontimehealth.backend.repository

import com.ontimehealth.backend.model.Recetas
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecetaRepository : JpaRepository<Recetas, Long> {
    fun findByPacienteIdOrderByFechaDesc(pacienteId: Long): List<Recetas>
    fun findByProfesionalIdOrderByFechaDesc(profesionalId: Long): List<Recetas>
}
