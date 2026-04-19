package com.On_TimeHealth.backend.Model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "Avisos")
class Avisos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false)
    var turno: Turnos? = null

    @Column(name = "tipo", nullable = false)
    var tipo: String? = null

    @Column(name = "mensaje")
    var mensaje: String? = null

    @Column(name = "fecha_creacion", nullable = false)
    var fechaCreacion: LocalDateTime? = LocalDateTime.now()
}
