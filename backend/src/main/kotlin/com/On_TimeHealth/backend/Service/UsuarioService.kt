package com.On_TimeHealth.backend.Service

import com.On_TimeHealth.backend.Model.*
import com.On_TimeHealth.backend.Repository.*
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val pacienteRepository: PacienteRepository,
    private val profesionalRepository: ProfesionalRepository,
    private val administrativoRepository: AdministrativoRepository,
    private val especialidadRepository: EspecialidadRepository,
    private val consultorioRepository: ConsultorioRepository
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

    @Transactional
    fun eliminar(id: Long) {
        val usuario = usuarioRepository.findById(id).orElseThrow {
            IllegalArgumentException("Usuario no encontrado")
        }
        when (usuario.rol) {
            "PACIENTE" -> pacienteRepository.deleteByUsuarioId(id)
            "MEDICO" -> profesionalRepository.deleteByUsuarioId(id)
            "ADMINISTRATIVO" -> administrativoRepository.deleteByUsuarioId(id)
        }
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
