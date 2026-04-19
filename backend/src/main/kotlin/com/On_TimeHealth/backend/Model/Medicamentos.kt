package com.On_TimeHealth.backend.Model

import jakarta.persistence.*

@Entity
@Table(name = "Medicamentos")
class Medicamentos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "nombre", nullable = false, unique = true)
    var nombre: String? = null

    @Column(name = "descripcion", length = 500)
    var descripcion: String? = null

    @Column(name = "presentacion")
    var presentacion: String? = null

    @Column(name = "activo", nullable = false)
    var activo: Boolean = true
}
