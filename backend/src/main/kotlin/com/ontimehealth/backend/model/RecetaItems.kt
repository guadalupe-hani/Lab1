package com.ontimehealth.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "RecetaItems")
class RecetaItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    var receta: Recetas? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    var medicamento: Medicamentos? = null

    @Column(name = "dosis", nullable = false)
    var dosis: String? = null

    @Column(name = "duracion")
    var duracion: String? = null

    @Column(name = "indicaciones", length = 500)
    var indicaciones: String? = null
}
