package com.ontimehealth.backend.service

import com.ontimehealth.backend.model.Notificaciones
import com.ontimehealth.backend.repository.NotificacionRepository
import com.ontimehealth.backend.repository.UsuarioRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class NotificacionService(
    private val notificacionRepository: NotificacionRepository,
    private val usuarioRepository: UsuarioRepository,
    private val emailService: EmailService
) {

    @Transactional
    fun crear(usuarioId: Long, tipo: String, mensaje: String) {
        val usuario = usuarioRepository.findById(usuarioId).orElse(null) ?: return
        val n = Notificaciones().apply {
            this.usuario = usuario
            this.tipo = tipo
            this.mensaje = mensaje
        }
        notificacionRepository.save(n)

        val asunto = when (tipo) {
            "TURNO_AGENDADO" -> "Turno agendado - On-Time Health"
            "TURNO_CANCELADO" -> "Turno cancelado - On-Time Health"
            "RECETA_EMITIDA" -> "Nueva receta emitida - On-Time Health"
            "MENSAJE_CHAT" -> null
            else -> "Notificación - On-Time Health"
        }
        if (asunto != null) {
            usuario.email?.let { emailService.enviar(it, asunto, mensaje) }
        }
    }

    fun listar(usuarioId: Long): List<Map<String, Any?>> =
        notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
            .map { toMap(it) }

    fun contarNoLeidas(usuarioId: Long): Long =
        notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId)

    @Transactional
    fun marcarLeida(id: Long, usuarioId: Long) {
        val n = notificacionRepository.findById(id).orElse(null) ?: return
        if (n.usuario?.id == usuarioId) {
            n.leida = true
            notificacionRepository.save(n)
        }
    }

    @Transactional
    fun marcarTodasLeidas(usuarioId: Long) {
        val pendientes = notificacionRepository
            .findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
            .filter { !it.leida }
        pendientes.forEach { it.leida = true }
        notificacionRepository.saveAll(pendientes)
    }

    private fun toMap(n: Notificaciones): Map<String, Any?> = mapOf(
        "id" to n.id,
        "tipo" to n.tipo,
        "mensaje" to n.mensaje,
        "leida" to n.leida,
        "fechaCreacion" to n.fechaCreacion.toString()
    )
}