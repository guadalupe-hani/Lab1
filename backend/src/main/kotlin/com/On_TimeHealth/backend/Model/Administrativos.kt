package com.On_TimeHealth.backend.Model

import jakarta.persistence.*

@Entity
@Table(name = "Administrativos")
class Administrativos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    var usuario: Usuarios? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultorio_id")
    var consultorio: Consultorios? = null
}
