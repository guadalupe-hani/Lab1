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

  // Recetas
  recetasMias: () => request('/recetas/mias'),
  recetaDetalle: (id) => request(`/recetas/${id}`),
  crearReceta: (body) => request('/recetas', { method: 'POST', body: JSON.stringify(body) }),
  eliminarReceta: (id) => request(`/recetas/${id}`, { method: 'DELETE' }),

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

  // Medicamentos
  medicamentos: () => request('/medicamentos'),
  medicamentosActivos: () => request('/medicamentos/activos'),
  crearMedicamento: (body) => request('/medicamentos', { method: 'POST', body: JSON.stringify(body) }),
  editarMedicamento: (id, body) => request(`/medicamentos/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  eliminarMedicamento: (id) => request(`/medicamentos/${id}`, { method: 'DELETE' }),
}
