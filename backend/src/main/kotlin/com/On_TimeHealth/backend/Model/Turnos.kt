package com.On_TimeHealth.backend.Model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "Turnos")
class Turnos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "fecha", nullable = false)
    var fecha: LocalDate? = null

    @Column(name = "hora", nullable = false)
    var hora: LocalTime? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    var paciente: Pacientes? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    var profesional: Profesionales? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultorio_id", nullable = false)
    var consultorio: Consultorios? = null

    @Column(name = "estado", nullable = false)
    var estado: String? = "PROGRAMADO"

    @Column(name = "estado_paciente")
    var estadoPaciente: String? = null

    @Column(name = "orden_en_fila")
    var ordenEnFila: Int? = null
}
