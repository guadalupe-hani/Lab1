package com.On_TimeHealth.backend.Repository

import com.On_TimeHealth.backend.Model.Recetas
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecetaRepository : JpaRepository<Recetas, Long> {
    fun findByPacienteIdOrderByFechaDesc(pacienteId: Long): List<Recetas>
    fun findByProfesionalIdOrderByFechaDesc(profesionalId: Long): List<Recetas>
}
