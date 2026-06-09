package com.ontimehealth.backend.controller

import com.ontimehealth.backend.repository.ConsultorioRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/consultorios")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5500"], allowCredentials = "true")
class ConsultorioController(private val consultorioRepository: ConsultorioRepository) {

    @GetMapping("")
    fun listar(): ResponseEntity<Any> {
        val lista = consultorioRepository.findAll().map {
            mapOf(
                "id" to it.id,
                "nombre" to it.nombre,
                "direccion" to it.direccion,
                "telefono" to it.telefono
            )
        }
        return ResponseEntity.ok(lista)
    }
}
