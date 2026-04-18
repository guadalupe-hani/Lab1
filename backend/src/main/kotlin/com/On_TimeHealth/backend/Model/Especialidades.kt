package com.On_TimeHealth.backend.Model

import jakarta.persistence.*

@Entity
@Table(name = "Especialidades")
class Especialidades {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}