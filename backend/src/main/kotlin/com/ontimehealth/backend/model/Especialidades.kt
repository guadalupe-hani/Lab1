package com.ontimehealth.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "Especialidades")
class Especialidades {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "nombre", nullable = false, unique = true)
    var nombre: String? = null
}
