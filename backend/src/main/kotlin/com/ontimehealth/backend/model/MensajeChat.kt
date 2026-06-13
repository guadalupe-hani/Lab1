package com.ontimehealth.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "mensajes_chat")
class MensajeChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emisor_id", nullable = false)
    var emisor: Usuarios? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receptor_id", nullable = false)
    var receptor: Usuarios? = null

    @Column(name = "contenido", nullable = false, length = 1000)
    var contenido: String? = null

    @Column(name = "fecha_envio", nullable = false)
    var fechaEnvio: LocalDateTime = LocalDateTime.now()

    @Column(name = "leido", nullable = false)
    var leido: Boolean = false

    // Referencia opcional a un turno
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id")
    var turno: Turnos? = null

    // Referencia opcional a una receta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id")
    var receta: Recetas? = null
}