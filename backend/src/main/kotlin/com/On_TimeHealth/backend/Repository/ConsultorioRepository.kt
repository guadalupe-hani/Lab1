package com.On_TimeHealth.backend.Repository

import com.On_TimeHealth.backend.Model.Consultorios
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConsultorioRepository : JpaRepository<Consultorios, Long>
