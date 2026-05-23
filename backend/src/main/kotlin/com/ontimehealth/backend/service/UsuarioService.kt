package com.ontimehealth.backend.service

import com.ontimehealth.backend.model.*
import com.ontimehealth.backend.repository.*
import com.ontimehealth.backend.model.Pacientes
import com.ontimehealth.backend.model.Profesionales
import com.ontimehealth.backend.model.Usuarios
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val pacienteRepository: PacienteRepository,
    private val profesionalRepository: ProfesionalRepository,
    private val administrativoRepository: AdministrativoRepository,
    private val especialidadRepository: EspecialidadRepository,
    private val consultorioRepository: ConsultorioRepository,

    private val turnoRepository: TurnoRepository,
    private val recetaRepository: RecetaRepository,
    private val horarioRepository: HorarioTrabajoRepository,
    private val diaLibreRepository: DiaLibreRepository
) {

    @Transactional
    fun registrarPaciente(
        nombre: String, apellido: String, email: String, password: String,
        dni: String, telefono: String?, obraSocial: String?, plan: String?
    ): Usuarios {
        validarEmailLibre(email)
        if (pacienteRepository.existsByDni(dni)) {
            throw IllegalArgumentException("Ya existe un paciente con ese DNI")
        }
        val usuario = crearUsuario(nombre, apellido, email, password, "PACIENTE")
        val paciente = Pacientes().apply {
            this.usuario = usuario
            this.dni = dni
            this.telefono = telefono
            this.obraSocial = obraSocial
            this.plan = plan
        }
        pacienteRepository.save(paciente)
        return usuario
    }

    @Transactional
    fun registrarMedico(
        nombre: String, apellido: String, email: String, password: String,
        matricula: String, especialidadId: Long?
    ): Usuarios {
        validarEmailLibre(email)
        if (profesionalRepository.existsByMatricula(matricula)) {
            throw IllegalArgumentException("Ya existe un profesional con esa matrícula")
        }
        val usuario = crearUsuario(nombre, apellido, email, password, "MEDICO")
        val especialidad = especialidadId?.let {
            especialidadRepository.findById(it).orElseThrow {
                IllegalArgumentException("Especialidad no encontrada")
            }
        }
        val profesional = Profesionales().apply {
            this.usuario = usuario
            this.matricula = matricula
            this.especialidad = especialidad
        }
        profesionalRepository.save(profesional)
        return usuario
    }

    @Transactional
    fun registrarAdministrativo(
        nombre: String, apellido: String, email: String, password: String,
        consultorioId: Long?
    ): Usuarios {
        validarEmailLibre(email)
        val usuario = crearUsuario(nombre, apellido, email, password, "ADMINISTRATIVO")
        val consultorio = consultorioId?.let {
            consultorioRepository.findById(it).orElseThrow {
                IllegalArgumentException("Consultorio no encontrado")
            }
        }
        val admin = Administrativos().apply {
            this.usuario = usuario
            this.consultorio = consultorio
        }
        administrativoRepository.save(admin)
        return usuario
    }

    fun login(email: String, password: String): Usuarios {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Email o contraseña incorrectos")
        if (usuario.password != password) {
            throw IllegalArgumentException("Email o contraseña incorrectos")
        }
        return usuario
    }

    fun obtenerPerfil(id: Long): Map<String, Any?> {
        val u = usuarioRepository.findById(id).orElseThrow {
            IllegalArgumentException("Usuario no encontrado")
        }
        val base = mutableMapOf<String, Any?>(
            "id" to u.id, "nombre" to u.nombre, "apellido" to u.apellido,
            "email" to u.email, "rol" to u.rol
        )
        when (u.rol) {
            "PACIENTE" -> pacienteRepository.findByUsuarioId(id)?.let {
                base["dni"] = it.dni
                base["telefono"] = it.telefono
                base["obraSocial"] = it.obraSocial
                base["plan"] = it.plan
            }
            "MEDICO" -> profesionalRepository.findByUsuarioId(id)?.let {
                base["matricula"] = it.matricula
                base["especialidadId"] = it.especialidad?.id
            }
            "ADMINISTRATIVO" -> administrativoRepository.findByUsuarioId(id)?.let {
                base["consultorioId"] = it.consultorio?.id
            }
        }
        return base
    }

    @Transactional
    fun editar(
        id: Long,
        nombre: String?, apellido: String?, email: String?, password: String?,
        telefono: String?, obraSocial: String?, plan: String?,
        especialidadId: Long?, consultorioId: Long?
    ): Usuarios {
        val u = usuarioRepository.findById(id).orElseThrow {
            IllegalArgumentException("Usuario no encontrado")
        }
        if (!email.isNullOrBlank() && email != u.email) {
            if (usuarioRepository.existsByEmail(email)) {
                throw IllegalArgumentException("Ya existe un usuario con ese email")
            }
            u.email = email
        }
        if (!nombre.isNullOrBlank()) u.nombre = nombre
        if (!apellido.isNullOrBlank()) u.apellido = apellido
        if (!password.isNullOrBlank()) u.password = password
        usuarioRepository.save(u)
        when (u.rol) {
            "PACIENTE" -> pacienteRepository.findByUsuarioId(id)?.let { p ->
                if (telefono != null) p.telefono = telefono
                if (obraSocial != null) p.obraSocial = obraSocial
                if (plan != null) p.plan = plan
                pacienteRepository.save(p)
            }
            "MEDICO" -> profesionalRepository.findByUsuarioId(id)?.let { pro ->
                if (especialidadId != null) {
                    pro.especialidad = especialidadRepository.findById(especialidadId).orElseThrow {
                        IllegalArgumentException("Especialidad no encontrada")
                    }
                }
                profesionalRepository.save(pro)
            }
            "ADMINISTRATIVO" -> administrativoRepository.findByUsuarioId(id)?.let { a ->
                if (consultorioId != null) {
                    a.consultorio = consultorioRepository.findById(consultorioId).orElseThrow {
                        IllegalArgumentException("Consultorio no encontrado")
                    }
                }
                administrativoRepository.save(a)
            }
        }
        return u
    }

    @Transactional
    fun eliminar(id: Long) {
        val usuario = usuarioRepository.findById(id).orElseThrow {
            IllegalArgumentException("Usuario no encontrado")
        }

        when (usuario.rol) {
            "PACIENTE" -> {
                val paciente = pacienteRepository.findByUsuarioId(id)
                if (paciente != null) {
                    // 1. Borramos sus recetas
                    val recetas = recetaRepository.findByPacienteIdOrderByFechaDesc(paciente.id!!)
                    recetaRepository.deleteAll(recetas)

                    // 2. Borramos sus turnos
                    val turnos = turnoRepository.findByPacienteIdOrderByFechaDescHoraDesc(paciente.id!!)
                    turnoRepository.deleteAll(turnos)

                    // 3. Borramos el perfil de paciente
                    pacienteRepository.delete(paciente)
                }
            }
            "MEDICO" -> {
                val profesional = profesionalRepository.findByUsuarioId(id)
                if (profesional != null) {
                    val recetas = recetaRepository.findByProfesionalIdOrderByFechaDesc(profesional.id!!)
                    recetaRepository.deleteAll(recetas)

                    val turnos = turnoRepository.findByProfesionalIdOrderByFechaDescHoraDesc(profesional.id!!)
                    turnoRepository.deleteAll(turnos)

                    val horarios = horarioRepository.findByProfesionalId(profesional.id!!)
                    horarioRepository.deleteAll(horarios)

                    val diasLibres = diaLibreRepository.findByProfesionalIdOrderByFechaAsc(profesional.id!!)
                    diaLibreRepository.deleteAll(diasLibres)

                    profesionalRepository.delete(profesional)
                }
            }
            "ADMINISTRATIVO" -> {
                // Los administrativos solo gestionan, no tienen turnos propios
                val admin = administrativoRepository.findByUsuarioId(id)
                if (admin != null) {
                    administrativoRepository.delete(admin)
                }
            }
        }

        // Finalmente, borramos el usuario base
        usuarioRepository.deleteById(id)
    }

    private fun validarEmailLibre(email: String) {
        if (usuarioRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Ya existe un usuario con ese email")
        }
    }

    private fun crearUsuario(nombre: String, apellido: String, email: String, password: String, rol: String): Usuarios {
        val usuario = Usuarios().apply {
            this.nombre = nombre
            this.apellido = apellido
            this.email = email
            this.password = password
            this.rol = rol
        }
        return usuarioRepository.save(usuario)
    }
}
