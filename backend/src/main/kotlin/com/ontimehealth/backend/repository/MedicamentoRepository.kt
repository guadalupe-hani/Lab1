package com.ontimehealth.backend.repository

import com.ontimehealth.backend.model.Medicamentos
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MedicamentoRepository : JpaRepository<Medicamentos, Long> {
    fun findByActivoTrueOrderByNombreAsc(): List<Medicamentos>
    fun findAllByOrderByNombreAsc(): List<Medicamentos>
    fun existsByNombreIgnoreCase(nombre: String): Boolean
    fun findByNombreIgnoreCase(nombre: String): Medicamentos?
}
