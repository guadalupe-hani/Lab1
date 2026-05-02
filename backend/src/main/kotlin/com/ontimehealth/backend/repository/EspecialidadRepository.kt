package com.ontimehealth.backend.repository

import com.ontimehealth.backend.model.Especialidades
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EspecialidadRepository : JpaRepository<Especialidades, Long> {
    fun findByNombre(nombre: String): Especialidades?
}
