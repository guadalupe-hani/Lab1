package com.On_TimeHealth.backend.Model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "Comentarios")
class Comentarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false)
    var turno: Turnos? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    var usuario: Usuarios? = null

    @Column(name = "contenido", nullable = false, length = 1000)
    var contenido: String? = null

    @Column(name = "fecha_creacion", nullable = false)
    var fechaCreacion: LocalDateTime? = LocalDateTime.now()
}
