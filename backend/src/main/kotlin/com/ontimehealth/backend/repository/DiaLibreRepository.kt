package com.ontimehealth.backend.repository

import com.ontimehealth.backend.model.DiasLibres
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DiaLibreRepository : JpaRepository<DiasLibres, Long> {
    fun findByProfesionalIdOrderByFechaAsc(profesionalId: Long): List<DiasLibres>
    fun findByProfesionalIdAndFecha(profesionalId: Long, fecha: LocalDate): DiasLibres?
    fun existsByProfesionalIdAndFecha(profesionalId: Long, fecha: LocalDate): Boolean
}
