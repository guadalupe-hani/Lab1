package com.ontimehealth.backend.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "Profesionales")
class Profesionales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    var usuario: Usuarios? = null

    @Column(name = "matricula", nullable = false, unique = true)
    var matricula: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id")
    var especialidad: Especialidades? = null

    @Column(name = "precio_consulta")
    var precioConsulta: BigDecimal? = null
}
