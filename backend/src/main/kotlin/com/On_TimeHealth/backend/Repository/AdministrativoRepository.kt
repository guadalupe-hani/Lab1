package com.On_TimeHealth.backend.Repository

import com.On_TimeHealth.backend.Model.Administrativos
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AdministrativoRepository : JpaRepository<Administrativos, Long> {
    fun findByUsuarioId(usuarioId: Long): Administrativos?
    fun deleteByUsuarioId(usuarioId: Long)
}
