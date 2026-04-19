package com.On_TimeHealth.backend.Service

import com.On_TimeHealth.backend.Model.Medicamentos
import com.On_TimeHealth.backend.Repository.MedicamentoRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class MedicamentoService(private val medicamentoRepository: MedicamentoRepository) {

    fun listarTodos(): List<Medicamentos> = medicamentoRepository.findAllByOrderByNombreAsc()
    fun listarActivos(): List<Medicamentos> = medicamentoRepository.findByActivoTrueOrderByNombreAsc()

    fun obtener(id: Long): Medicamentos = medicamentoRepository.findById(id).orElseThrow {
        IllegalArgumentException("Medicamento no encontrado")
    }

    @Transactional
    fun crear(nombre: String, descripcion: String?, presentacion: String?): Medicamentos {
        if (nombre.isBlank()) throw IllegalArgumentException("El nombre es obligatorio")
        if (medicamentoRepository.existsByNombreIgnoreCase(nombre.trim())) {
            throw IllegalArgumentException("Ya existe un medicamento con ese nombre")
        }
        val m = Medicamentos().apply {
            this.nombre = nombre.trim()
            this.descripcion = descripcion
            this.presentacion = presentacion
            this.activo = true
        }
        return medicamentoRepository.save(m)
    }

    @Transactional
    fun editar(id: Long, nombre: String?, descripcion: String?, presentacion: String?, activo: Boolean?): Medicamentos {
        val m = obtener(id)
        if (!nombre.isNullOrBlank() && !nombre.equals(m.nombre, ignoreCase = true)) {
            if (medicamentoRepository.existsByNombreIgnoreCase(nombre.trim())) {
                throw IllegalArgumentException("Ya existe un medicamento con ese nombre")
            }
            m.nombre = nombre.trim()
        }
        if (descripcion != null) m.descripcion = descripcion
        if (presentacion != null) m.presentacion = presentacion
        if (activo != null) m.activo = activo
        return medicamentoRepository.save(m)
    }

    @Transactional
    fun eliminar(id: Long) {
        // Soft delete: marcar como inactivo
        val m = obtener(id)
        m.activo = false
        medicamentoRepository.save(m)
    }

    fun toMap(m: Medicamentos): Map<String, Any?> = mapOf(
        "id" to m.id,
        "nombre" to m.nombre,
        "descripcion" to m.descripcion,
        "presentacion" to m.presentacion,
        "activo" to m.activo
    )
}
