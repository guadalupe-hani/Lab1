package com.On_TimeHealth.backend.Service

import com.On_TimeHealth.backend.Model.Turnos
import com.On_TimeHealth.backend.Repository.*
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Service
class TurnoService(
    private val turnoRepository: TurnoRepository,
    private val pacienteRepository: PacienteRepository,
    private val profesionalRepository: ProfesionalRepository,
    private val consultorioRepository: ConsultorioRepository,
    private val administrativoRepository: AdministrativoRepository,
    private val horarioRepository: HorarioTrabajoRepository,
    private val diaLibreRepository: DiaLibreRepository
) {

    private val SLOT_MIN = 30L

    // ==================== DISPONIBILIDAD ====================

    fun calcularDisponibilidad(profesionalId: Long, desde: LocalDate, hasta: LocalDate): List<Map<String, Any?>> {
        val horarios = horarioRepository.findByProfesionalId(profesionalId)
        if (horarios.isEmpty()) return emptyList()

        val diasLibres = diaLibreRepository.findByProfesionalIdOrderByFechaAsc(profesionalId)
            .mapNotNull { it.fecha }.toSet()

        val turnosExistentes = turnoRepository.findByProfesionalIdAndFechaBetween(profesionalId, desde, hasta)
            .filter { it.estado == "PROGRAMADO" }
            .map { (it.fecha to it.hora) }
            .toSet()

        val result = mutableListOf<Map<String, Any?>>()
        var fecha = desde
        while (!fecha.isAfter(hasta)) {
            if (fecha !in diasLibres && !fecha.isBefore(LocalDate.now())) {
                val diaNombre = diaSemanaNombre(fecha.dayOfWeek)
                val bloquesDelDia = horarios.filter { it.diaSemana == diaNombre }
                for (bloque in bloquesDelDia) {
                    var slot = bloque.horaInicio ?: continue
                    val fin = bloque.horaFin ?: continue
                    while (slot.isBefore(fin)) {
                        val ocupado = (fecha to slot) in turnosExistentes
                        val esHoy = fecha == LocalDate.now()
                        val pasado = esHoy && slot.isBefore(LocalTime.now())
                        if (!ocupado && !pasado) {
                            result.add(mapOf(
                                "fecha" to fecha.toString(),
                                "hora" to slot.toString(),
                                "consultorioId" to bloque.consultorio?.id,
                                "consultorioNombre" to bloque.consultorio?.nombre
                            ))
                        }
                        slot = slot.plusMinutes(SLOT_MIN)
                    }
                }
            }
            fecha = fecha.plusDays(1)
        }
        return result
    }

    // ==================== AGENDAR ====================

    @Transactional
    fun agendarComoPaciente(
        pacienteUsuarioId: Long, profesionalId: Long, fecha: LocalDate, hora: LocalTime
    ): Turnos {
        val paciente = pacienteRepository.findByUsuarioId(pacienteUsuarioId)
            ?: throw IllegalArgumentException("Paciente no encontrado")
        return agendar(paciente.id!!, profesionalId, fecha, hora)
    }

    @Transactional
    fun agendarComoAdmin(
        adminUsuarioId: Long, dniPaciente: String, profesionalId: Long, fecha: LocalDate, hora: LocalTime
    ): Turnos {
        val consultorioAdmin = obtenerConsultorioAdmin(adminUsuarioId)
        if (!horarioRepository.existsByProfesionalIdAndConsultorioId(profesionalId, consultorioAdmin)) {
            throw IllegalArgumentException("Ese profesional no atiende en tu consultorio")
        }
        val paciente = pacienteRepository.findByDni(dniPaciente)
            ?: throw IllegalArgumentException("No existe un paciente con ese DNI")
        return agendar(paciente.id!!, profesionalId, fecha, hora)
    }

    private fun agendar(pacienteId: Long, profesionalId: Long, fecha: LocalDate, hora: LocalTime): Turnos {
        if (fecha.isBefore(LocalDate.now())) {
            throw IllegalArgumentException("No se pueden agendar turnos en fechas pasadas")
        }
        // Validar disponibilidad — el slot tiene que estar en los calculados
        val slots = calcularDisponibilidad(profesionalId, fecha, fecha)
        val match = slots.firstOrNull { it["fecha"] == fecha.toString() && it["hora"] == hora.toString() }
            ?: throw IllegalArgumentException("Ese horario no está disponible")
        val consultorioId = match["consultorioId"] as? Long
            ?: throw IllegalArgumentException("No se pudo determinar el consultorio")

        val paciente = pacienteRepository.findById(pacienteId).orElseThrow {
            IllegalArgumentException("Paciente no encontrado")
        }
        val profesional = profesionalRepository.findById(profesionalId).orElseThrow {
            IllegalArgumentException("Profesional no encontrado")
        }
        val consultorio = consultorioRepository.findById(consultorioId).orElseThrow {
            IllegalArgumentException("Consultorio no encontrado")
        }
        val turno = Turnos().apply {
            this.paciente = paciente
            this.profesional = profesional
            this.consultorio = consultorio
            this.fecha = fecha
            this.hora = hora
            this.estado = "PROGRAMADO"
        }
        return turnoRepository.save(turno)
    }

    // ==================== CANCELAR ====================

    @Transactional
    fun cancelar(usuarioId: Long, rol: String, turnoId: Long, motivo: String?): Turnos {
        val turno = turnoRepository.findById(turnoId).orElseThrow {
            IllegalArgumentException("Turno no encontrado")
        }
        if (turno.estado != "PROGRAMADO") {
            throw IllegalArgumentException("Este turno ya no está activo")
        }
        when (rol) {
            "PACIENTE" -> {
                val paciente = pacienteRepository.findByUsuarioId(usuarioId)
                if (turno.paciente?.id != paciente?.id) {
                    throw IllegalArgumentException("Este turno no te pertenece")
                }
            }
            "MEDICO" -> {
                val profesional = profesionalRepository.findByUsuarioId(usuarioId)
                if (turno.profesional?.id != profesional?.id) {
                    throw IllegalArgumentException("Este turno no te pertenece")
                }
            }
            "ADMINISTRATIVO" -> {
                val consultorioAdmin = obtenerConsultorioAdmin(usuarioId)
                if (turno.consultorio?.id != consultorioAdmin) {
                    throw IllegalArgumentException("Este turno no es de tu consultorio")
                }
            }
            else -> throw IllegalArgumentException("Rol no autorizado")
        }
        turno.estado = "CANCELADO"
        turno.canceladoPor = rol
        turno.motivoCancelacion = motivo
        return turnoRepository.save(turno)
    }

    // ==================== LISTAR ====================

    fun listarDePaciente(usuarioId: Long): List<Turnos> {
        val paciente = pacienteRepository.findByUsuarioId(usuarioId)
            ?: throw IllegalArgumentException("Paciente no encontrado")
        return turnoRepository.findByPacienteIdOrderByFechaDescHoraDesc(paciente.id!!)
    }

    fun listarDeMedico(usuarioId: Long): List<Turnos> {
        val profesional = profesionalRepository.findByUsuarioId(usuarioId)
            ?: throw IllegalArgumentException("Profesional no encontrado")
        return turnoRepository.findByProfesionalIdOrderByFechaDescHoraDesc(profesional.id!!)
    }

    fun listarDeAdmin(adminUsuarioId: Long): List<Turnos> {
        val consultorioId = obtenerConsultorioAdmin(adminUsuarioId)
        return turnoRepository.findByConsultorioIdOrderByFechaDescHoraDesc(consultorioId)
    }

    // ==================== HELPERS ====================

    private fun obtenerConsultorioAdmin(adminUsuarioId: Long): Long {
        val admin = administrativoRepository.findByUsuarioId(adminUsuarioId)
            ?: throw IllegalArgumentException("Administrativo no encontrado")
        return admin.consultorio?.id
            ?: throw IllegalArgumentException("Tu cuenta no tiene consultorio asignado")
    }

    private fun diaSemanaNombre(d: DayOfWeek): String = when (d) {
        DayOfWeek.MONDAY -> "LUNES"
        DayOfWeek.TUESDAY -> "MARTES"
        DayOfWeek.WEDNESDAY -> "MIERCOLES"
        DayOfWeek.THURSDAY -> "JUEVES"
        DayOfWeek.FRIDAY -> "VIERNES"
        DayOfWeek.SATURDAY -> "SABADO"
        DayOfWeek.SUNDAY -> "DOMINGO"
    }

    fun toMap(t: Turnos): Map<String, Any?> = mapOf(
        "id" to t.id,
        "fecha" to t.fecha?.toString(),
        "hora" to t.hora?.toString(),
        "estado" to t.estado,
        "canceladoPor" to t.canceladoPor,
        "motivoCancelacion" to t.motivoCancelacion,
        "pacienteId" to t.paciente?.id,
        "pacienteNombre" to "${t.paciente?.usuario?.nombre ?: ""} ${t.paciente?.usuario?.apellido ?: ""}".trim(),
        "pacienteDni" to t.paciente?.dni,
        "profesionalId" to t.profesional?.id,
        "profesionalNombre" to "${t.profesional?.usuario?.nombre ?: ""} ${t.profesional?.usuario?.apellido ?: ""}".trim(),
        "profesionalEspecialidad" to t.profesional?.especialidad?.nombre,
        "consultorioId" to t.consultorio?.id,
        "consultorioNombre" to t.consultorio?.nombre,
        "consultorioDireccion" to t.consultorio?.direccion
    )
}
