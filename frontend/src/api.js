const BASE_URL = 'http://localhost:8080/api'

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(data.error || 'Error en la solicitud')
  return data
}

export const api = {
  // Usuarios
  registrarPaciente: (body) => request('/usuarios/registrar/paciente', { method: 'POST', body: JSON.stringify(body) }),
  registrarMedico: (body) => request('/usuarios/registrar/medico', { method: 'POST', body: JSON.stringify(body) }),
  registrarAdministrativo: (body) => request('/usuarios/registrar/administrativo', { method: 'POST', body: JSON.stringify(body) }),
  login: (body) => request('/usuarios/login', { method: 'POST', body: JSON.stringify(body) }),
  logout: () => request('/usuarios/logout', { method: 'POST' }),
  eliminar: () => request('/usuarios/eliminar', { method: 'DELETE' }),
  sesion: () => request('/usuarios/sesion'),
  perfil: () => request('/usuarios/perfil'),
  editar: (body) => request('/usuarios/editar', { method: 'PUT', body: JSON.stringify(body) }),
  buscarPacientes: (q) => request(`/usuarios/pacientes/buscar${q ? '?q=' + q : ''}`),

  // Recetas
  recetasMias: () => request('/recetas/mias'),
  recetaDetalle: (id) => request(`/recetas/${id}`),
  crearReceta: (body) => request('/recetas', { method: 'POST', body: JSON.stringify(body) }),
  eliminarReceta: (id) => request(`/recetas/${id}`, { method: 'DELETE' }),
  recetaPdf: async (id) => {
    const res = await fetch(`${BASE_URL}/recetas/${id}/pdf`, { credentials: 'include' })
    if (!res.ok) throw new Error('No se pudo generar el PDF')
    return res.blob()
  },

  // Consultorios
  consultorios: () => request('/consultorios'),

  // Horarios de trabajo
  horariosMios: () => request('/horarios/mios'),
  horariosDeProfesional: (id) => request(`/horarios/profesional/${id}`),
  medicosDelConsultorio: () => request('/horarios/medicos-del-consultorio'),
  crearHorarioMedico: (body) => request('/horarios/medico', { method: 'POST', body: JSON.stringify(body) }),
  crearHorarioAdmin: (body) => request('/horarios/admin', { method: 'POST', body: JSON.stringify(body) }),
  eliminarHorario: (id) => request(`/horarios/${id}`, { method: 'DELETE' }),

  // Días libres
  diasLibresMios: () => request('/dias-libres/mios'),
  diasLibresDeProfesional: (id) => request(`/dias-libres/profesional/${id}`),
  crearDiaLibreMedico: (body) => request('/dias-libres/medico', { method: 'POST', body: JSON.stringify(body) }),
  crearDiaLibreAdmin: (body) => request('/dias-libres/admin', { method: 'POST', body: JSON.stringify(body) }),
  eliminarDiaLibre: (id) => request(`/dias-libres/${id}`, { method: 'DELETE' }),

  // Especialidades
  especialidades: () => request('/especialidades'),

  // Profesionales (búsqueda)
  buscarProfesionales: ({ especialidadId, nombre } = {}) => {
    const params = new URLSearchParams()
    if (especialidadId) params.set('especialidadId', especialidadId)
    if (nombre) params.set('nombre', nombre)
    const q = params.toString()
    return request(`/profesionales/buscar${q ? '?' + q : ''}`)
  },

  // Turnos
  turnosMios: () => request('/turnos/mios'),
  disponibilidad: (profesionalId, desde, hasta) => {
    const params = new URLSearchParams()
    if (desde) params.set('desde', desde)
    if (hasta) params.set('hasta', hasta)
    const q = params.toString()
    return request(`/turnos/disponibilidad/${profesionalId}${q ? '?' + q : ''}`)
  },
  agendarTurnoPaciente: (body) => request('/turnos/paciente', { method: 'POST', body: JSON.stringify(body) }),
  agendarTurnoAdmin: (body) => request('/turnos/admin', { method: 'POST', body: JSON.stringify(body) }),
  cancelarTurno: (id, motivo) => request(`/turnos/${id}/cancelar`, { method: 'PUT', body: JSON.stringify({ motivo: motivo || null }) }),
  crearPreferenciaPago: (turnoId) => request(`/pagos/turno/${turnoId}/preferencia`, { method: 'POST' }),
  confirmarPago: (turnoId, status) => request(`/pagos/turno/${turnoId}/confirmar?status=${status}`, { method: 'PUT' }),

  // Fila en vivo
  filaEnVivo: () => request('/turnos/fila'),
  reportarRetrasoPaciente: (turnoId, minutos) => request(`/turnos/${turnoId}/retraso`, { method: 'PUT', body: JSON.stringify({ minutos }) }),
  reportarRetrasoMedico: (minutos) => request('/turnos/medico/retraso', { method: 'PUT', body: JSON.stringify({ minutos }) }),
  marcarEstadoPaciente: (turnoId, estado) => request(`/turnos/${turnoId}/estado-paciente`, { method: 'PUT', body: JSON.stringify({ estado }) }),
  estadisticasMedico: (profesionalId) => request(`/estadisticas/medico/${profesionalId}`),
  estadisticasMedicoPdf: async (profesionalId) => {
    const res = await fetch(`${BASE_URL}/estadisticas/medico/${profesionalId}/pdf`, { credentials: 'include' })
    if (!res.ok) throw new Error('No se pudo generar el PDF')
    return res.blob()
  },
  salaEspera: () => request('/turnos/sala-espera'),

  // Medicamentos
  medicamentos: () => request('/medicamentos'),
  medicamentosActivos: () => request('/medicamentos/activos'),
  crearMedicamento: (body) => request('/medicamentos', { method: 'POST', body: JSON.stringify(body) }),
  editarMedicamento: (id, body) => request(`/medicamentos/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  eliminarMedicamento: (id) => request(`/medicamentos/${id}`, { method: 'DELETE' }),

  // Notificaciones
  notificaciones: () => request('/notificaciones'),
  noLeidas: () => request('/notificaciones/no-leidas'),
  marcarLeida: (id) => request(`/notificaciones/${id}/leer`, { method: 'PUT' }),
  marcarTodasLeidas: () => request('/notificaciones/leer-todas', { method: 'PUT' }),

  // Chat
  chatContactos: () => request('/chat/contactos'),
  chatConversacion: (otroUsuarioId) => request(`/chat/conversacion/${otroUsuarioId}`),
  chatNoLeidos: () => request('/chat/no-leidos'),
  chatNoLeidosPorEmisor: () => request('/chat/no-leidos-por-emisor'),

  // Historial médico
  miHistorial: () => request('/historial/mio'),
  historialPaciente: (usuarioId) => request(`/historial/paciente/${usuarioId}`),
}
