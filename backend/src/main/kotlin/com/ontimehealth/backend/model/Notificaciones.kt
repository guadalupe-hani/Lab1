package com.ontimehealth.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notificaciones")
class Notificaciones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    var usuario: Usuarios? = null

    @Column(name = "tipo", nullable = false)
    var tipo: String? = null

    @Column(name = "mensaje", nullable = false, length = 500)
    var mensaje: String? = null

    @Column(name = "leida", nullable = false)
    var leida: Boolean = false

    @Column(name = "fecha_creacion", nullable = false)
    var fechaCreacion: LocalDateTime = LocalDateTime.now()
}