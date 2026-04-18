package com.On_TimeHealth.backend.Repository

import com.On_TimeHealth.backend.Model.Usuarios
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UsuarioRepository : JpaRepository<Usuarios, Long> {
    fun findByEmail(email: String): Usuarios?
    fun existsByEmail(email: String): Boolean
}
