package com.ontimehealth.backend.service

import com.ontimehealth.backend.model.RecetaItems
import com.ontimehealth.backend.model.Recetas
import com.ontimehealth.backend.repository.*
import com.ontimehealth.backend.repository.MedicamentoRepository
import com.ontimehealth.backend.repository.RecetaRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate

data class ItemRecetaData(
    val medicamentoId: Long,
    val dosis: String,
    val duracion: String?,
    val indicaciones: String?
)

@Service
class RecetaService(
    private val recetaRepository: RecetaRepository,
    private val pacienteRepository: PacienteRepository,
    private val profesionalRepository: ProfesionalRepository,
    private val medicamentoRepository: MedicamentoRepository
) {

    @Transactional
    fun crear(
        profesionalUsuarioId: Long,
        dniPaciente: String,
        items: List<ItemRecetaData>,
        indicacionesGenerales: String?
    ): Recetas {
        val profesional = profesionalRepository.findByUsuarioId(profesionalUsuarioId)
            ?: throw IllegalArgumentException("Profesional no encontrado")
        val paciente = pacienteRepository.findByDni(dniPaciente)
            ?: throw IllegalArgumentException("No existe un paciente con ese DNI")
        if (items.isEmpty()) throw IllegalArgumentException("La receta debe tener al menos un medicamento")

        val medicoUser = profesional.usuario
            ?: throw IllegalStateException("Profesional sin usuario asociado")
        val firma = "Dr/a. ${medicoUser.nombre} ${medicoUser.apellido} - Matrícula ${profesional.matricula}"

        val receta = Recetas().apply {
            this.paciente = paciente
            this.profesional = profesional
            this.fecha = LocalDate.now()
            this.obraSocial = paciente.obraSocial
            this.contenido = indicacionesGenerales
            this.firma = firma
        }

        items.forEach { item ->
            if (item.dosis.isBlank()) throw IllegalArgumentException("Cada medicamento debe tener una dosis")
            val med = medicamentoRepository.findById(item.medicamentoId).orElseThrow {
                IllegalArgumentException("Medicamento con id ${item.medicamentoId} no encontrado")
            }
            if (!med.activo) throw IllegalArgumentException("El medicamento '${med.nombre}' ya no está disponible")
            val recetaItem = RecetaItems().apply {
                this.receta = receta
                this.medicamento = med
                this.dosis = item.dosis
                this.duracion = item.duracion
                this.indicaciones = item.indicaciones
            }
            receta.items.add(recetaItem)
        }

        return recetaRepository.save(receta)
    }

    fun listarDePaciente(usuarioId: Long): List<Recetas> {
        val paciente = pacienteRepository.findByUsuarioId(usuarioId)
            ?: throw IllegalArgumentException("Paciente no encontrado")
        return recetaRepository.findByPacienteIdOrderByFechaDesc(paciente.id!!)
    }

    fun listarDeMedico(usuarioId: Long): List<Recetas> {
        val profesional = profesionalRepository.findByUsuarioId(usuarioId)
            ?: throw IllegalArgumentException("Profesional no encontrado")
        return recetaRepository.findByProfesionalIdOrderByFechaDesc(profesional.id!!)
    }

    fun obtener(id: Long, usuarioId: Long, rol: String): Recetas {
        val receta = recetaRepository.findById(id).orElseThrow {
            IllegalArgumentException("Receta no encontrada")
        }
        when (rol) {
            "PACIENTE" -> {
                val paciente = pacienteRepository.findByUsuarioId(usuarioId)
                if (receta.paciente?.id != paciente?.id) {
                    throw IllegalArgumentException("No tenés permiso para ver esta receta")
                }
            }
            "MEDICO" -> {
                val profesional = profesionalRepository.findByUsuarioId(usuarioId)
                if (receta.profesional?.id != profesional?.id) {
                    throw IllegalArgumentException("No tenés permiso para ver esta receta")
                }
            }
            else -> throw IllegalArgumentException("Rol no autorizado")
        }
        return receta
    }

    @Transactional
    fun eliminar(id: Long, profesionalUsuarioId: Long) {
        val receta = recetaRepository.findById(id).orElseThrow {
            IllegalArgumentException("Receta no encontrada")
        }
        val profesional = profesionalRepository.findByUsuarioId(profesionalUsuarioId)
            ?: throw IllegalArgumentException("Profesional no encontrado")
        if (receta.profesional?.id != profesional.id) {
            throw IllegalArgumentException("Solo el médico que la emitió puede eliminarla")
        }
        recetaRepository.delete(receta)
    }

    fun toMap(r: Recetas): Map<String, Any?> = mapOf(
        "id" to r.id,
        "fecha" to r.fecha?.toString(),
        "obraSocial" to r.obraSocial,
        "contenido" to r.contenido,
        "firma" to r.firma,
        "pacienteNombre" to "${r.paciente?.usuario?.nombre ?: ""} ${r.paciente?.usuario?.apellido ?: ""}".trim(),
        "pacienteDni" to r.paciente?.dni,
        "medicoNombre" to "${r.profesional?.usuario?.nombre ?: ""} ${r.profesional?.usuario?.apellido ?: ""}".trim(),
        "medicoMatricula" to r.profesional?.matricula,
        "items" to r.items.map { item ->
            mapOf(
                "id" to item.id,
                "medicamentoId" to item.medicamento?.id,
                "medicamentoNombre" to item.medicamento?.nombre,
                "medicamentoPresentacion" to item.medicamento?.presentacion,
                "dosis" to item.dosis,
                "duracion" to item.duracion,
                "indicaciones" to item.indicaciones
            )
        }
    )
}
