package com.On_TimeHealth.backend.Model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Profesionales {

    @Id
    @Column(name = "matricula",nullable = false)
    var matricula: Long? = null


}