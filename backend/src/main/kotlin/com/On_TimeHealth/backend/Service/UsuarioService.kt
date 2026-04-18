package com.On_TimeHealth.backend.Service

import com.On_TimeHealth.backend.Model.Usuarios
import com.On_TimeHealth.backend.Repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class UsuarioService(private val usuarioRepository: UsuarioRepository) {

    fun registrar(nombre: String, apellido: String, email: String, password: String): Usuarios {
        if (usuarioRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Ya existe un usuario con ese email")
        }
        val usuario = Usuarios().apply {
            this.nombre = nombre
            this.apellido = apellido
            this.email = email
            this.password = password
            this.rol = "PACIENTE"
        }
        return usuarioRepository.save(usuario)
    }

    fun login(email: String, password: String): Usuarios {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Email o contraseña incorrectos")
        if (usuario.password != password) {
            throw IllegalArgumentException("Email o contraseña incorrectos")
        }
        return usuario
    }

    fun eliminar(id: Long) {
        if (!usuarioRepository.existsById(id)) {
            throw IllegalArgumentException("Usuario no encontrado")
        }
        usuarioRepository.deleteById(id)
    }
}
