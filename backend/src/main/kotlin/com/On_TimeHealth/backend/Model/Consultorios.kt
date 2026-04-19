package com.On_TimeHealth.backend.Model

import jakarta.persistence.*

@Entity
@Table(name = "Consultorios")
class Consultorios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "nombre", nullable = false)
    var nombre: String? = null

    @Column(name = "direccion", nullable = false)
    var direccion: String? = null

    @Column(name = "telefono")
    var telefono: String? = null
}
