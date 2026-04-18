package com.On_TimeHealth.backend.Model

import jakarta.persistence.*

@Entity
@Table(name = "Consultorios")
class Consultorios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}