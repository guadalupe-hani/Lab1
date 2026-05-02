package com.ontimehealth.backend.repository

import com.ontimehealth.backend.model.Pacientes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PacienteRepository : JpaRepository<Pacientes, Long> {
    fun findByUsuarioId(usuarioId: Long): Pacientes?
    fun findByDni(dni: String): Pacientes?
    fun existsByDni(dni: String): Boolean
    fun deleteByUsuarioId(usuarioId: Long)
}
