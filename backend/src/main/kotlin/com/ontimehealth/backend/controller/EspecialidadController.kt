package com.ontimehealth.backend.controller

import com.ontimehealth.backend.repository.EspecialidadRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/especialidades")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5500"], allowCredentials = "true")
class EspecialidadController(private val especialidadRepository: EspecialidadRepository) {

    @GetMapping("")
    fun listar(): ResponseEntity<Any> {
        val lista = especialidadRepository.findAll().map {
            mapOf("id" to it.id, "nombre" to it.nombre)
        }
        return ResponseEntity.ok(lista)
    }
}
