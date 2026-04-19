package com.On_TimeHealth.backend.Repository

import com.On_TimeHealth.backend.Model.Especialidades
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EspecialidadRepository : JpaRepository<Especialidades, Long> {
    fun findByNombre(nombre: String): Especialidades?
}
