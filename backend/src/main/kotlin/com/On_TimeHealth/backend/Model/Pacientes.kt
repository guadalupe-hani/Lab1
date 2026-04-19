package com.On_TimeHealth.backend.Model

import jakarta.persistence.*

@Entity
@Table(name = "Pacientes")
class Pacientes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    var usuario: Usuarios? = null

    @Column(name = "dni", nullable = false, unique = true)
    var dni: String? = null

    @Column(name = "telefono")
    var telefono: String? = null

    @Column(name = "obra_social")
    var obraSocial: String? = null

    @Column(name = "plan")
    var plan: String? = null
}
