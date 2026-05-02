package com.ontimehealth.backend.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "Recetas")
class Recetas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id")
    var turno: Turnos? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    var paciente: Pacientes? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    var profesional: Profesionales? = null

    @Column(name = "fecha", nullable = false)
    var fecha: LocalDate? = null

    @Column(name = "obra_social")
    var obraSocial: String? = null

    @Column(name = "contenido", length = 2000)
    var contenido: String? = null

    @Column(name = "firma")
    var firma: String? = null

    @OneToMany(mappedBy = "receta", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var items: MutableList<RecetaItems> = mutableListOf()
}
