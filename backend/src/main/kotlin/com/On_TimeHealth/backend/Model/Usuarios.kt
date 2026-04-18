package com.On_TimeHealth.backend.Model

import jakarta.persistence.*

@Entity
@Table(name = "Usuarios")
class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "nombre", nullable = false)
    var nombre: String? = null

    @Column(name = "apellido", nullable = false)
    var apellido: String? = null

    @Column(name = "email", nullable = false, unique = true)
    var email: String? = null

    @Column(name = "password", nullable = false)
    var password: String? = null

    @Column(name = "rol", nullable = false)
    var rol: String? = "PACIENTE"
}