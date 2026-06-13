package com.ontimehealth.backend.repository

import com.ontimehealth.backend.model.FilaProfesionalDia
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface FilaProfesionalDiaRepository : JpaRepository<FilaProfesionalDia, Long> {
    fun findByProfesionalIdAndFecha(profesionalId: Long, fecha: LocalDate): FilaProfesionalDia?
}
