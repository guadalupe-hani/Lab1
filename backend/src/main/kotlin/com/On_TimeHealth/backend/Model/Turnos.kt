package com.On_TimeHealth.backend.Model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name="Turnos")
class Turnos {

    @Id
    @Column(name="ID_turno", nullable = false)
    var idTurno: Int? = null

    @Column(name="Fecha", nullable = false)
    var fecha: Date? = null

    @Column(name="profesional", nullable = false)
    var profesional: Long? = null

    @Column(name="Paciente", nullable = false)
    var paciente: Int? = null

}