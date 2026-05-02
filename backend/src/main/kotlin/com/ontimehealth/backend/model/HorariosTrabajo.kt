package com.ontimehealth.backend.model

import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "HorariosTrabajo")
class HorariosTrabajo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    var profesional: Profesionales? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultorio_id", nullable = false)
    var consultorio: Consultorios? = null

    @Column(name = "dia_semana", nullable = false)
    var diaSemana: String? = null

    @Column(name = "hora_inicio", nullable = false)
    var horaInicio: LocalTime? = null

    @Column(name = "hora_fin", nullable = false)
    var horaFin: LocalTime? = null
}
