const API_URL = 'http://localhost:8080/api/usuarios'

async function request(path, options = {}) {
  const res = await fetch(`${API_URL}${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(data.error || 'Error en la solicitud')
  return data
}

export const api = {
  registrarPaciente: (body) => request('/registrar/paciente', { method: 'POST', body: JSON.stringify(body) }),
  registrarMedico: (body) => request('/registrar/medico', { method: 'POST', body: JSON.stringify(body) }),
  registrarAdministrativo: (body) => request('/registrar/administrativo', { method: 'POST', body: JSON.stringify(body) }),
  login: (body) => request('/login', { method: 'POST', body: JSON.stringify(body) }),
  logout: () => request('/logout', { method: 'POST' }),
  eliminar: () => request('/eliminar', { method: 'DELETE' }),
  sesion: () => request('/sesion'),
}
