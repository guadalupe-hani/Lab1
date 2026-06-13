package com.ontimehealth.backend.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "fila_profesional_dia", uniqueConstraints = [UniqueConstraint(columnNames = ["profesional_id", "fecha"])])
class FilaProfesionalDia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    var profesional: Profesionales? = null

    @Column(name = "fecha", nullable = false)
    var fecha: LocalDate? = null

    @Column(name = "offset_minutos", nullable = false)
    var offsetMinutos: Int = 0
}
