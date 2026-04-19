import { useState } from 'react'
import { api } from '../api'

export default function Registro({ onSuccess, onSwitch }) {
  const [rol, setRol] = useState('PACIENTE')
  const [form, setForm] = useState({
    nombre: '', apellido: '', email: '', password: '',
    dni: '', telefono: '', obraSocial: '', plan: '',
    matricula: '', especialidadId: '',
    consultorioId: '',
  })
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')

  const update = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(''); setOk('')
    try {
      const base = { nombre: form.nombre, apellido: form.apellido, email: form.email, password: form.password }
      if (rol === 'PACIENTE') {
        await api.registrarPaciente({ ...base, dni: form.dni, telefono: form.telefono, obraSocial: form.obraSocial, plan: form.plan })
      } else if (rol === 'MEDICO') {
        await api.registrarMedico({ ...base, matricula: form.matricula, especialidadId: form.especialidadId ? Number(form.especialidadId) : null })
      } else {
        await api.registrarAdministrativo({ ...base, consultorioId: form.consultorioId ? Number(form.consultorioId) : null })
      }
      setOk('Cuenta creada. Ya podés iniciar sesión.')
      setTimeout(onSwitch, 1000)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="card">
      <h2>Crear cuenta</h2>
      <div className="roles">
        <label><input type="radio" checked={rol === 'PACIENTE'} onChange={() => setRol('PACIENTE')} /> Paciente</label>
        <label><input type="radio" checked={rol === 'MEDICO'} onChange={() => setRol('MEDICO')} /> Médico</label>
        <label><input type="radio" checked={rol === 'ADMINISTRATIVO'} onChange={() => setRol('ADMINISTRATIVO')} /> Administrativo</label>
      </div>
      <form onSubmit={handleSubmit}>
        <input placeholder="Nombre" value={form.nombre} onChange={update('nombre')} required />
        <input placeholder="Apellido" value={form.apellido} onChange={update('apellido')} required />
        <input type="email" placeholder="Email" value={form.email} onChange={update('email')} required />
        <input type="password" placeholder="Contraseña" value={form.password} onChange={update('password')} required />

        {rol === 'PACIENTE' && (
          <>
            <input placeholder="DNI" value={form.dni} onChange={update('dni')} required />
            <input placeholder="Teléfono" value={form.telefono} onChange={update('telefono')} />
            <input placeholder="Obra social" value={form.obraSocial} onChange={update('obraSocial')} />
            <input placeholder="Plan" value={form.plan} onChange={update('plan')} />
          </>
        )}
        {rol === 'MEDICO' && (
          <>
            <input placeholder="Matrícula" value={form.matricula} onChange={update('matricula')} required />
            <input placeholder="ID de especialidad (opcional)" value={form.especialidadId} onChange={update('especialidadId')} />
          </>
        )}
        {rol === 'ADMINISTRATIVO' && (
          <input placeholder="ID de consultorio (opcional)" value={form.consultorioId} onChange={update('consultorioId')} />
        )}

        <button type="submit">Registrarse</button>
      </form>
      {error && <p className="error">{error}</p>}
      {ok && <p className="ok">{ok}</p>}
      <p className="switch">
        ¿Ya tenés cuenta? <button className="link" onClick={onSwitch}>Iniciar sesión</button>
      </p>
    </div>
  )
}
