package com.ontimehealth.backend.repository

import com.ontimehealth.backend.model.Notificaciones
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificacionRepository : JpaRepository<Notificaciones, Long> {
    fun findByUsuarioIdOrderByFechaCreacionDesc(usuarioId: Long): List<Notificaciones>
    fun countByUsuarioIdAndLeidaFalse(usuarioId: Long): Long
}