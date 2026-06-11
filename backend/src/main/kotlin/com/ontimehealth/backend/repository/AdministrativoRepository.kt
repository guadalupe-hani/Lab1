package com.ontimehealth.backend.repository

import com.ontimehealth.backend.model.Administrativos
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AdministrativoRepository : JpaRepository<Administrativos, Long> {
    fun findByUsuarioId(usuarioId: Long): Administrativos?
    fun deleteByUsuarioId(usuarioId: Long)
    fun findByConsultorioId(consultorioId: Long): List<Administrativos>
}
