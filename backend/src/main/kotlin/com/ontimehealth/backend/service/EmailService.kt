package com.ontimehealth.backend.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender
) {

    @Async
    fun enviar(destinatario: String, asunto: String, cuerpo: String) {
        try {
            val mensaje = SimpleMailMessage()
            mensaje.setTo(destinatario)
            mensaje.setSubject(asunto)
            mensaje.setText(cuerpo)
            mailSender.send(mensaje)
        } catch (e: Exception) {
            println("Error al enviar email a $destinatario: ${e.message}")
        }
    }
}
