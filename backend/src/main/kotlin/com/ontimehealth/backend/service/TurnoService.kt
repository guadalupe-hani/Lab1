package com.ontimehealth.backend.service

import com.ontimehealth.backend.model.FilaProfesionalDia
import com.ontimehealth.backend.model.Turnos
import com.ontimehealth.backend.repository.*
import com.ontimehealth.backend.repository.DiaLibreRepository
import com.ontimehealth.backend.repository.FilaProfesionalDiaRepository
import com.ontimehealth.backend.repository.HorarioTrabajoRepository
import com.ontimehealth.backend.repository.TurnoRepository
import jakarta.transaction.Transactional
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.Duration
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
    private val diaLibreRepository: DiaLibreRepository,
    private val filaRepository: FilaProfesionalDiaRepository,
    private val notificacionService: NotificacionService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    private val SLOT_MIN = 30L
    private val OFFSET_MIN = -120
    private val OFFSET_MAX = 240
    private val ESTADOS_ACTIVOS_FILA = setOf("ESPERANDO", "EN_ATENCION")
    private val ESTADOS_PACIENTE_VALIDOS = setOf("ESPERANDO", "EN_ATENCION", "ATENDIDO", "AUSENTE")

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
        val turnoGuardado = turnoRepository.save(turno)

// Notificar paciente
        paciente.usuario?.id?.let { pid ->
            notificacionService.crear(pid, "TURNO_AGENDADO",
                "Se agendó tu turno con Dr/a. ${profesional.usuario?.nombre} ${profesional.usuario?.apellido} para el $fecha a las ${hora.toString().substring(0, 5)}")
        }
// Notificar médico
        profesional.usuario?.id?.let { mid ->
            notificacionService.crear(mid, "TURNO_AGENDADO",
                "Nuevo turno: ${paciente.usuario?.nombre} ${paciente.usuario?.apellido} el $fecha a las ${hora.toString().substring(0, 5)}")
        }
// Notificar admins del consultorio
        administrativoRepository.findByConsultorioId(consultorioId).forEach { admin ->
            admin.usuario?.id?.let { aid ->
                notificacionService.crear(aid, "TURNO_AGENDADO",
                    "Nuevo turno: ${paciente.usuario?.nombre} ${paciente.usuario?.apellido} con Dr/a. ${profesional.usuario?.nombre} ${profesional.usuario?.apellido} el $fecha")
            }
        }

        return turnoGuardado
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

        val turnoGuardado = turnoRepository.save(turno)

        val pacienteUser = turno.paciente?.usuario
        val medicoUser = turno.profesional?.usuario
        val consultorioId = turno.consultorio?.id
        val cancelador = turno.canceladoPor
        val motivo = if (!turno.motivoCancelacion.isNullOrBlank()) ": ${turno.motivoCancelacion}" else ""

        if (cancelador != "PACIENTE") {
            pacienteUser?.id?.let { pid ->
                val quien = if (cancelador == "MEDICO") "Dr/a. ${medicoUser?.nombre} ${medicoUser?.apellido}" else "el consultorio"
                notificacionService.crear(pid, "TURNO_CANCELADO",
                    "Tu turno del ${turno.fecha} a las ${turno.hora.toString().substring(0, 5)} fue cancelado por $quien$motivo")
            }
        }
        if (cancelador != "MEDICO") {
            medicoUser?.id?.let { mid ->
                val quien = if (cancelador == "PACIENTE") "${pacienteUser?.nombre} ${pacienteUser?.apellido}" else "el consultorio"
                notificacionService.crear(mid, "TURNO_CANCELADO",
                    "El turno del ${turno.fecha} a las ${turno.hora.toString().substring(0, 5)} fue cancelado por $quien")
            }
        }
        if (cancelador != "ADMINISTRATIVO" && consultorioId != null) {
            administrativoRepository.findByConsultorioId(consultorioId).forEach { admin ->
                admin.usuario?.id?.let { aid ->
                    notificacionService.crear(aid, "TURNO_CANCELADO",
                        "Turno cancelado: ${pacienteUser?.nombre} ${pacienteUser?.apellido} con Dr/a. ${medicoUser?.nombre} ${medicoUser?.apellido} el ${turno.fecha}$motivo")
                }
            }
        }

        if (turno.fecha == LocalDate.now()) {
            turno.profesional?.id?.let { profesionalId ->
                // Si el turno cancelado todavía estaba activo en la fila, liberar ese slot
                // reduce la espera estimada del resto.
                if ((turno.estadoPaciente ?: "ESPERANDO") in ESTADOS_ACTIVOS_FILA) {
                    ajustarOffset(profesionalId, -SLOT_MIN.toInt())
                }
                broadcastFila(profesionalId)
            }
        }

        return turnoGuardado
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
        "consultorioDireccion" to t.consultorio?.direccion,
        "estadoPaciente" to t.estadoPaciente,
        "estadoPago" to t.estadoPago
    )

    // ==================== FILA EN VIVO ====================

    /** Devuelve la fila de hoy de un profesional, ya calculada (posición, hora estimada, etc). */
    fun obtenerFila(profesionalId: Long, rol: String, usuarioId: Long): Map<String, Any?> {
        val hoy = LocalDate.now()
        val turnos = turnoRepository.findByProfesionalIdAndFechaAndEstadoOrderByHoraAsc(profesionalId, hoy, "PROGRAMADO")
        val offset = filaRepository.findByProfesionalIdAndFecha(profesionalId, hoy)?.offsetMinutos ?: 0

        var posicion = 0
        val items = turnos.map { t ->
            val estadoPaciente = t.estadoPaciente ?: "ESPERANDO"
            val activo = estadoPaciente in ESTADOS_ACTIVOS_FILA
            if (activo) posicion++
            val esTuyo = rol == "PACIENTE" && t.paciente?.usuario?.id == usuarioId

            val item = mutableMapOf<String, Any?>(
                "turnoId" to t.id,
                "hora" to t.hora?.toString(),
                "horaEstimada" to t.hora?.plusMinutes(offset.toLong())?.toString(),
                "estadoPaciente" to estadoPaciente,
                "posicion" to if (activo) posicion else null
            )
            if (rol != "PACIENTE" || esTuyo) {
                item["pacienteId"] = t.paciente?.id
                item["pacienteNombre"] = "${t.paciente?.usuario?.nombre ?: ""} ${t.paciente?.usuario?.apellido ?: ""}".trim()
                item["pacienteDni"] = t.paciente?.dni
            }
            if (rol == "PACIENTE") {
                item["esTuyo"] = esTuyo
            }
            item
        }

        val profesionalUsuarioId = profesionalRepository.findById(profesionalId).orElse(null)?.usuario?.id

        return mapOf(
            "profesionalId" to profesionalId,
            "profesionalUsuarioId" to profesionalUsuarioId,
            "fecha" to hoy.toString(),
            "offsetMinutos" to offset,
            "turnos" to items
        )
    }

    /** Fila del usuario logueado: la propia agenda de hoy (médico) o la fila del turno de hoy (paciente). */
    fun obtenerFilaPropia(usuarioId: Long, rol: String): Map<String, Any?> {
        return when (rol) {
            "MEDICO" -> {
                val profesional = profesionalRepository.findByUsuarioId(usuarioId)
                    ?: throw IllegalArgumentException("Profesional no encontrado")
                val fila = obtenerFila(profesional.id!!, rol, usuarioId)
                fila + mapOf("tieneFilaHoy" to (fila["turnos"] as List<*>).isNotEmpty())
            }
            "PACIENTE" -> {
                val paciente = pacienteRepository.findByUsuarioId(usuarioId)
                    ?: throw IllegalArgumentException("Paciente no encontrado")
                val turnoHoy = turnoRepository.findByPacienteIdAndFechaAndEstado(paciente.id!!, LocalDate.now(), "PROGRAMADO").firstOrNull()
                    ?: return mapOf("tieneFilaHoy" to false, "turnos" to emptyList<Any>())
                val fila = obtenerFila(turnoHoy.profesional!!.id!!, rol, usuarioId)
                fila + mapOf("tieneFilaHoy" to true)
            }
            else -> throw IllegalArgumentException("Rol no autorizado")
        }
    }

    @Transactional
    fun reportarRetrasoPaciente(usuarioId: Long, rol: String, turnoId: Long, minutos: Int): Map<String, Any?> {
        if (rol != "PACIENTE") throw IllegalArgumentException("Solo pacientes")
        if (minutos < 1 || minutos > 60) throw IllegalArgumentException("El retraso debe ser entre 1 y 60 minutos")

        val turno = turnoRepository.findById(turnoId).orElseThrow { IllegalArgumentException("Turno no encontrado") }
        val paciente = pacienteRepository.findByUsuarioId(usuarioId)
        if (turno.paciente?.id != paciente?.id) throw IllegalArgumentException("Este turno no te pertenece")
        if (turno.fecha != LocalDate.now()) throw IllegalArgumentException("Este turno no es de hoy")
        if (turno.estado != "PROGRAMADO") throw IllegalArgumentException("Este turno ya no está activo")

        val profesionalId = turno.profesional?.id ?: throw IllegalArgumentException("Turno sin profesional")
        ajustarOffset(profesionalId, minutos)
        broadcastFila(profesionalId)
        return obtenerFila(profesionalId, rol, usuarioId)
    }

    @Transactional
    fun reportarRetrasoMedico(usuarioId: Long, rol: String, minutos: Int): Map<String, Any?> {
        if (rol != "MEDICO") throw IllegalArgumentException("Solo médicos")
        if (minutos == 0 || minutos < -60 || minutos > 120) throw IllegalArgumentException("El ajuste debe ser entre -60 y 120 minutos")

        val profesional = profesionalRepository.findByUsuarioId(usuarioId)
            ?: throw IllegalArgumentException("Profesional no encontrado")
        val profesionalId = profesional.id!!
        ajustarOffset(profesionalId, minutos)
        broadcastFila(profesionalId)
        return obtenerFila(profesionalId, rol, usuarioId)
    }

    @Transactional
    fun marcarEstadoPaciente(usuarioId: Long, rol: String, turnoId: Long, estado: String): Map<String, Any?> {
        if (estado !in ESTADOS_PACIENTE_VALIDOS) throw IllegalArgumentException("Estado inválido")

        val turno = turnoRepository.findById(turnoId).orElseThrow { IllegalArgumentException("Turno no encontrado") }
        if (turno.fecha != LocalDate.now()) throw IllegalArgumentException("Este turno no es de hoy")
        if (turno.estado != "PROGRAMADO") throw IllegalArgumentException("Este turno ya no está activo")

        when (rol) {
            "MEDICO" -> {
                val profesional = profesionalRepository.findByUsuarioId(usuarioId)
                if (turno.profesional?.id != profesional?.id) throw IllegalArgumentException("Este turno no te pertenece")
            }
            "ADMINISTRATIVO" -> {
                val consultorioAdmin = obtenerConsultorioAdmin(usuarioId)
                if (turno.consultorio?.id != consultorioAdmin) throw IllegalArgumentException("Este turno no es de tu consultorio")
            }
            else -> throw IllegalArgumentException("Rol no autorizado")
        }

        val profesionalId = turno.profesional?.id ?: throw IllegalArgumentException("Turno sin profesional")

        // Si el médico marca a alguien como atendido, ajustamos el retraso acumulado
        // según si lo atendió antes o después de la hora estimada.
        if (estado == "ATENDIDO") {
            val offsetActual = filaRepository.findByProfesionalIdAndFecha(profesionalId, LocalDate.now())?.offsetMinutos ?: 0
            val horaEstimada = turno.hora!!.plusMinutes(offsetActual.toLong())
            val diferencia = Duration.between(horaEstimada, LocalTime.now()).toMinutes().toInt()
            if (diferencia != 0) ajustarOffset(profesionalId, diferencia)
        }

        turno.estadoPaciente = estado
        turnoRepository.save(turno)

        broadcastFila(profesionalId)
        return obtenerFila(profesionalId, rol, usuarioId)
    }

    private fun ajustarOffset(profesionalId: Long, deltaMinutos: Int) {
        val hoy = LocalDate.now()
        val fila = filaRepository.findByProfesionalIdAndFecha(profesionalId, hoy)
            ?: FilaProfesionalDia().apply {
                profesional = profesionalRepository.findById(profesionalId).orElseThrow {
                    IllegalArgumentException("Profesional no encontrado")
                }
                fecha = hoy
                offsetMinutos = 0
            }
        fila.offsetMinutos = (fila.offsetMinutos + deltaMinutos).coerceIn(OFFSET_MIN, OFFSET_MAX)
        filaRepository.save(fila)
    }

    private fun broadcastFila(profesionalId: Long) {
        val payload: Any = mapOf("profesionalId" to profesionalId, "ts" to System.currentTimeMillis())
        messagingTemplate.convertAndSend("/topic/fila/$profesionalId", payload)
    }

    fun obtenerSalaEspera(usuarioId: Long, rol: String): Map<String, Any?> {
        if (rol != "ADMINISTRATIVO") throw IllegalArgumentException("Solo administrativos")
        val consultorioId = obtenerConsultorioAdmin(usuarioId)
        val hoy = LocalDate.now()
        val turnos = turnoRepository.findByConsultorioIdAndFechaAndEstadoOrderByHoraAsc(consultorioId, hoy, "PROGRAMADO")

        val offsetsPorProfesional = mutableMapOf<Long, Int>()
        fun offset(profId: Long) = offsetsPorProfesional.getOrPut(profId) {
            filaRepository.findByProfesionalIdAndFecha(profId, hoy)?.offsetMinutos ?: 0
        }

        fun turnoMap(t: Turnos): Map<String, Any?> {
            val profId = t.profesional?.id ?: 0L
            return mapOf(
                "turnoId" to t.id,
                "hora" to t.hora?.toString(),
                "horaEstimada" to t.hora?.plusMinutes(offset(profId).toLong())?.toString(),
                "estadoPaciente" to (t.estadoPaciente ?: "ESPERANDO"),
                "pacienteNombre" to "${t.paciente?.usuario?.nombre ?: ""} ${t.paciente?.usuario?.apellido ?: ""}".trim(),
                "medicoNombre" to "${t.profesional?.usuario?.nombre ?: ""} ${t.profesional?.usuario?.apellido ?: ""}".trim()
            )
        }

        val todos = turnos.map(::turnoMap)
        val enAtencion = todos.filter { it["estadoPaciente"] == "EN_ATENCION" }
        val esperando = todos.filter { it["estadoPaciente"] == "ESPERANDO" }
        val consultorio = consultorioRepository.findById(consultorioId).orElse(null)

        return mapOf(
            "fecha" to hoy.toString(),
            "consultorio" to consultorio?.nombre,
            "enAtencion" to enAtencion,
            "esperando" to esperando,
            "todos" to todos
        )
    }
}
