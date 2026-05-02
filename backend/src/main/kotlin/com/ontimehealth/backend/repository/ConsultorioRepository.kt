package com.ontimehealth.backend.repository

import com.ontimehealth.backend.model.Consultorios
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConsultorioRepository : JpaRepository<Consultorios, Long>
