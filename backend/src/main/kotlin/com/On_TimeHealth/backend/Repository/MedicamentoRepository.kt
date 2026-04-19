package com.On_TimeHealth.backend.Repository

import com.On_TimeHealth.backend.Model.Medicamentos
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MedicamentoRepository : JpaRepository<Medicamentos, Long> {
    fun findByActivoTrueOrderByNombreAsc(): List<Medicamentos>
    fun findAllByOrderByNombreAsc(): List<Medicamentos>
    fun existsByNombreIgnoreCase(nombre: String): Boolean
    fun findByNombreIgnoreCase(nombre: String): Medicamentos?
}
