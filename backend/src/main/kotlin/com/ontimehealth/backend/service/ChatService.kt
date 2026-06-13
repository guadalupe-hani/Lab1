package com.ontimehealth.backend.service

import com.ontimehealth.backend.model.MensajeChat
import com.ontimehealth.backend.repository.ChatRepository
import com.ontimehealth.backend.repository.RecetaRepository
import com.ontimehealth.backend.repository.TurnoRepository
import com.ontimehealth.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val usuarioRepository: UsuarioRepository,
    private val turnoRepository: TurnoRepository,
    private val recetaRepository: RecetaRepository
) {

    fun enviarMensaje(
        emisorId: Long,
        receptorId: Long,
        contenido: String,
        turnoId: Long? = null,
        recetaId: Long? = null
    ): MensajeChat {
        val emisor = usuarioRepository.findById(emisorId)
            .orElseThrow { RuntimeException("Emisor no encontrado") }
        val receptor = usuarioRepository.findById(receptorId)
            .orElseThrow { RuntimeException("Receptor no encontrado") }

        val mensaje = MensajeChat()
        mensaje.emisor = emisor
        mensaje.receptor = receptor
        mensaje.contenido = contenido
        mensaje.fechaEnvio = LocalDateTime.now()
        mensaje.leido = false

        if (turnoId != null) {
            mensaje.turno = turnoRepository.findById(turnoId).orElse(null)
        }
        if (recetaId != null) {
            mensaje.receta = recetaRepository.findById(recetaId).orElse(null)
        }

        return chatRepository.save(mensaje)
    }

    fun obtenerConversacion(usuarioId: Long, otroUsuarioId: Long): List<Map<String, Any?>> {
        return chatRepository.findConversacion(usuarioId, otroUsuarioId).map { toMap(it) }
    }

    fun marcarLeidos(receptorId: Long, emisorId: Long) {
        val noLeidos = chatRepository.findNoLeidos(receptorId)
            .filter { it.emisor?.id == emisorId }
        noLeidos.forEach { it.leido = true }
        chatRepository.saveAll(noLeidos)
    }

    fun toMap(m: MensajeChat): Map<String, Any?> = mapOf(
        "id" to m.id,
        "emisorId" to m.emisor?.id,
        "emisorNombre" to "${m.emisor?.nombre} ${m.emisor?.apellido}",
        "receptorId" to m.receptor?.id,
        "contenido" to m.contenido,
        "fechaEnvio" to m.fechaEnvio.toString(),
        "leido" to m.leido,
        "turnoId" to m.turno?.id,
        "turnoInfo" to m.turno?.let { "Turno: ${it.fecha} a las ${it.hora?.toString()?.substring(0, 5)}" },
        "recetaId" to m.receta?.id,
        "recetaInfo" to m.receta?.let { "Receta del ${it.fecha}" }
    )

    fun contarNoLeidos(usuarioId: Long): Long =
        chatRepository.countByReceptorIdAndLeidoFalse(usuarioId)

    fun noLeidosPorEmisor(usuarioId: Long): Map<String, Long> =
        chatRepository.countNoLeidosPorEmisor(usuarioId)
            .associate { row -> (row[0] as Long).toString() to (row[1] as Long) }
}