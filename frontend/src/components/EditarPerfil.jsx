import { useEffect, useState } from 'react'
import { api } from '../api'

export default function EditarPerfil({ user, onDone, onCancel, onLogout }) {
  const [form, setForm] = useState(null)
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')

  useEffect(() => {
    api.perfil()
      .then((data) => setForm({
        nombre: data.nombre || '',
        apellido: data.apellido || '',
        email: data.email || '',
        password: '',
        telefono: data.telefono || '',
        obraSocial: data.obraSocial || '',
        plan: data.plan || '',
        especialidadId: data.especialidadId ?? '',
        consultorioId: data.consultorioId ?? '',
      }))
      .catch((err) => setError(err.message))
  }, [])

  if (error && !form) return <div className="card"><p className="error">{error}</p></div>
  if (!form) return <div className="card"><p>Cargando...</p></div>

  const update = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const handleDelete = async () => {
    if (!confirm('¿Seguro que querés eliminar tu cuenta? Esta acción es irreversible.')) return
    try {
      await api.eliminar()
      onLogout && onLogout()
    } catch (err) {
      setError(err.message)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(''); setOk('')
    try {
      const body = {
        nombre: form.nombre,
        apellido: form.apellido,
        email: form.email,
        password: form.password || null,
      }
      if (user.rol === 'PACIENTE') {
        body.telefono = form.telefono
        body.obraSocial = form.obraSocial
        body.plan = form.plan
      } else if (user.rol === 'MEDICO') {
        body.especialidadId = form.especialidadId ? Number(form.especialidadId) : null
      } else if (user.rol === 'ADMINISTRATIVO') {
        body.consultorioId = form.consultorioId ? Number(form.consultorioId) : null
      }
      const updated = await api.editar(body)
      setOk('Perfil actualizado')
      setTimeout(() => onDone(updated), 800)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="card">
      <h2>Editar perfil</h2>
      <form onSubmit={handleSubmit}>
        <input placeholder="Nombre" value={form.nombre} onChange={update('nombre')} required />
        <input placeholder="Apellido" value={form.apellido} onChange={update('apellido')} required />
        <input type="email" placeholder="Email" value={form.email} onChange={update('email')} required />
        <input type="password" placeholder="Nueva contraseña (dejar vacío para no cambiar)" value={form.password} onChange={update('password')} />

        {user.rol === 'PACIENTE' && (
          <>
            <input placeholder="Teléfono" value={form.telefono} onChange={update('telefono')} />
            <input placeholder="Obra social" value={form.obraSocial} onChange={update('obraSocial')} />
            <input placeholder="Plan" value={form.plan} onChange={update('plan')} />
          </>
        )}
        {user.rol === 'MEDICO' && (
          <input placeholder="ID de especialidad" value={form.especialidadId} onChange={update('especialidadId')} />
        )}
        {user.rol === 'ADMINISTRATIVO' && (
          <input placeholder="ID de consultorio" value={form.consultorioId} onChange={update('consultorioId')} />
        )}

        <div className="actions">
          <button type="submit">Guardar</button>
          <button type="button" className="link" onClick={onCancel}>Cancelar</button>
        </div>
      </form>
      {error && <p className="error">{error}</p>}
      {ok && <p className="ok">{ok}</p>}

      <hr style={{ margin: '24px 0 16px', border: 0, borderTop: '1px solid var(--border-soft)' }} />
      <div style={{ textAlign: 'center' }}>
        <button className="link danger" onClick={handleDelete}>Eliminar cuenta</button>
      </div>
    </div>
  )
}
