package com.ontimehealth.backend.controller

import com.ontimehealth.backend.repository.PacienteRepository
import com.ontimehealth.backend.repository.ProfesionalRepository
import com.ontimehealth.backend.repository.TurnoRepository
import com.ontimehealth.backend.service.ChatService
import com.ontimehealth.backend.service.NotificacionService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["http://localhost:5173"], allowCredentials = "true")
class ChatController(
    private val chatService: ChatService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val turnoRepository: TurnoRepository,
    private val profesionalRepository: ProfesionalRepository,
    private val pacienteRepository: PacienteRepository,
    private val notificacionService: NotificacionService
) {

    @MessageMapping("/chat/enviar")
    fun recibirMensaje(
        @Payload payload: Map<String, Any>,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val attrs = headerAccessor.sessionAttributes ?: return
        val emisorId = (attrs["usuarioId"] as? Long) ?: return

        val receptorId = (payload["receptorId"] as? Int)?.toLong() ?: return
        val contenido = payload["contenido"] as? String ?: return
        val turnoId = (payload["turnoId"] as? Int)?.toLong()
        val recetaId = (payload["recetaId"] as? Int)?.toLong()

        val mensaje = chatService.enviarMensaje(emisorId, receptorId, contenido, turnoId, recetaId)
        val mensajeMap = chatService.toMap(mensaje)

        messagingTemplate.convertAndSendToUser(receptorId.toString(), "/queue/mensajes", mensajeMap)
        messagingTemplate.convertAndSendToUser(emisorId.toString(), "/queue/mensajes", mensajeMap)

        // Notificación in-app al receptor (sin email)
        val nombreEmisor = "${mensaje.emisor?.nombre} ${mensaje.emisor?.apellido}"
        notificacionService.crear(receptorId, "MENSAJE_CHAT", "Nuevo mensaje de $nombreEmisor")
    }

    @GetMapping("/api/chat/conversacion/{otroUsuarioId}")
    fun obtenerConversacion(
        @PathVariable otroUsuarioId: Long,
        @SessionAttribute("usuarioId") usuarioId: Long
    ): List<Map<String, Any?>> {
        chatService.marcarLeidos(usuarioId, otroUsuarioId)
        return chatService.obtenerConversacion(usuarioId, otroUsuarioId)
    }

    @GetMapping("/api/chat/contactos")
    fun obtenerContactos(
        @SessionAttribute("usuarioId") usuarioId: Long,
        @SessionAttribute("usuarioRol") usuarioRol: String
    ): List<Map<String, Any?>> {
        return when (usuarioRol) {
            "PACIENTE" -> {
                val paciente = pacienteRepository.findByUsuarioId(usuarioId) ?: return emptyList()
                turnoRepository.findByPacienteIdOrderByFechaDescHoraDesc(paciente.id!!)
                    .mapNotNull { it.profesional?.usuario }
                    .distinctBy { it.id }
                    .map { mapOf("id" to it.id, "nombre" to "Dr/a. ${it.nombre} ${it.apellido}") }
            }
            "MEDICO" -> {
                val profesional = profesionalRepository.findByUsuarioId(usuarioId) ?: return emptyList()
                turnoRepository.findByProfesionalIdOrderByFechaDescHoraDesc(profesional.id!!)
                    .mapNotNull { it.paciente?.usuario }
                    .distinctBy { it.id }
                    .map { mapOf("id" to it.id, "nombre" to "${it.nombre} ${it.apellido}") }
            }
            else -> emptyList()
        }
    }

    @GetMapping("/api/chat/no-leidos")
    fun contarNoLeidos(@SessionAttribute("usuarioId") usuarioId: Long): Map<String, Any> =
        mapOf("count" to chatService.contarNoLeidos(usuarioId))

    @GetMapping("/api/chat/no-leidos-por-emisor")
    fun noLeidosPorEmisor(@SessionAttribute("usuarioId") usuarioId: Long): Map<String, Long> =
        chatService.noLeidosPorEmisor(usuarioId)
}