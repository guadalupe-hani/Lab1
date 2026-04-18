package com.On_TimeHealth.backend.Model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "Pacientes")
class Pacientes {
    @Id
    @Column(name = "DNI", nullable=false)
    var dni: Int? = null;

    @Column(name= "Nombre", nullable=false)
    var nombre: String? = null;

    @Column(name= "Apellido", nullable=false)
    var apellido: String? = null;

    @Column(name= "Telefono", nullable=false)
    var telefono: Int? = null;
}