package com.On_TimeHealth.backend.Repository

import com.On_TimeHealth.backend.Model.Profesionales
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfesionalRepository : JpaRepository<Profesionales, Long> {
    fun findByUsuarioId(usuarioId: Long): Profesionales?
    fun existsByMatricula(matricula: String): Boolean
    fun deleteByUsuarioId(usuarioId: Long)
}
