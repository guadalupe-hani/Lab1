package com.ontimehealth.backend.repository

import com.ontimehealth.backend.model.MensajeChat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatRepository : JpaRepository<MensajeChat, Long> {

    @Query("""
        SELECT m FROM MensajeChat m 
        WHERE (m.emisor.id = :u1 AND m.receptor.id = :u2) 
           OR (m.emisor.id = :u2 AND m.receptor.id = :u1) 
        ORDER BY m.fechaEnvio ASC
    """)
    fun findConversacion(@Param("u1") u1: Long, @Param("u2") u2: Long): List<MensajeChat>

    @Query("""
        SELECT m FROM MensajeChat m 
        WHERE m.receptor.id = :usuarioId AND m.leido = false
    """)
    fun findNoLeidos(@Param("usuarioId") usuarioId: Long): List<MensajeChat>

    fun countByReceptorIdAndLeidoFalse(receptorId: Long): Long

    @Query("""
        SELECT m.emisor.id, COUNT(m) FROM MensajeChat m 
        WHERE m.receptor.id = :receptorId AND m.leido = false 
        GROUP BY m.emisor.id
    """)
    fun countNoLeidosPorEmisor(@Param("receptorId") receptorId: Long): List<Array<Any>>
}