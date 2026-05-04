import { useState } from 'react'
import { api } from '../api'

const TELEFONO_REGEX = /^[+]?[\d\s\-().]{7,20}$/

function validateBase(form) {
  const errors = {}
  if (!form.nombre.trim()) errors.nombre = 'El nombre es requerido'
  else if (form.nombre.trim().length < 2) errors.nombre = 'Mínimo 2 caracteres'
  else if (form.nombre.trim().length > 50) errors.nombre = 'Máximo 50 caracteres'

  if (!form.apellido.trim()) errors.apellido = 'El apellido es requerido'
  else if (form.apellido.trim().length < 2) errors.apellido = 'Mínimo 2 caracteres'
  else if (form.apellido.trim().length > 50) errors.apellido = 'Máximo 50 caracteres'

  if (!form.email.trim()) errors.email = 'El email es requerido'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) errors.email = 'Email inválido'

  if (!form.password) errors.password = 'La contraseña es requerida'
  else if (form.password.length < 6) errors.password = 'Mínimo 6 caracteres'
  else if (form.password.length > 100) errors.password = 'Máximo 100 caracteres'

  return errors
}

function validatePaciente(form) {
  const errors = {}
  if (!form.dni.trim()) errors.dni = 'El DNI es requerido'
  else if (!/^\d{7,8}$/.test(form.dni.trim())) errors.dni = 'DNI inválido (7 u 8 dígitos)'

  if (form.telefono && !TELEFONO_REGEX.test(form.telefono.trim())) {
    errors.telefono = 'Teléfono inválido'
  }
  if (form.obraSocial && form.obraSocial.trim().length > 100) {
    errors.obraSocial = 'Máximo 100 caracteres'
  }
  if (form.plan && form.plan.trim().length > 50) {
    errors.plan = 'Máximo 50 caracteres'
  }
  return errors
}

function validateMedico(form) {
  const errors = {}
  if (!form.matricula.trim()) errors.matricula = 'La matrícula es requerida'
  else if (form.matricula.trim().length < 3) errors.matricula = 'Mínimo 3 caracteres'
  else if (form.matricula.trim().length > 20) errors.matricula = 'Máximo 20 caracteres'
  return errors
}

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
  const [fieldErrors, setFieldErrors] = useState({})

  const update = (k) => (e) => {
    setForm({ ...form, [k]: e.target.value })
    if (fieldErrors[k]) setFieldErrors({ ...fieldErrors, [k]: undefined })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setOk('')

    let errors = validateBase(form)
    if (rol === 'PACIENTE') errors = { ...errors, ...validatePaciente(form) }
    if (rol === 'MEDICO') errors = { ...errors, ...validateMedico(form) }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }
    setFieldErrors({})

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
      setTimeout(onSwitch, 1500)
    } catch (err) {
      setError(err.message)
    }
  }

  const Field = ({ id, label, error: err, children }) => (
      <div className="field">
        <label htmlFor={id}>{label}</label>
        {children}
        {err && <span className="field-error">{err}</span>}
      </div>
  )

  return (
      <div className="card">
        <h2>Crear cuenta</h2>
        <div className="roles">
          <label><input type="radio" checked={rol === 'PACIENTE'} onChange={() => setRol('PACIENTE')} /> Paciente</label>
          <label><input type="radio" checked={rol === 'MEDICO'} onChange={() => setRol('MEDICO')} /> Médico</label>
          <label><input type="radio" checked={rol === 'ADMINISTRATIVO'} onChange={() => setRol('ADMINISTRATIVO')} /> Administrativo</label>
        </div>
        <form onSubmit={handleSubmit}>
          <Field id="reg-nombre" label="Nombre" error={fieldErrors.nombre}>
            <input id="reg-nombre" placeholder="Tu nombre" value={form.nombre} onChange={update('nombre')} />
          </Field>
          <Field id="reg-apellido" label="Apellido" error={fieldErrors.apellido}>
            <input id="reg-apellido" placeholder="Tu apellido" value={form.apellido} onChange={update('apellido')} />
          </Field>
          <Field id="reg-email" label="Email" error={fieldErrors.email}>
            <input id="reg-email" type="email" placeholder="ejemplo@correo.com" value={form.email} onChange={update('email')} />
          </Field>
          <Field id="reg-password" label="Contraseña" error={fieldErrors.password}>
            <input id="reg-password" type="password" placeholder="Mínimo 6 caracteres" value={form.password} onChange={update('password')} />
          </Field>

          {rol === 'PACIENTE' && (
              <>
                <Field id="reg-dni" label="DNI" error={fieldErrors.dni}>
                  <input id="reg-dni" placeholder="Sin puntos ni espacios" value={form.dni} onChange={update('dni')} />
                </Field>
                <Field id="reg-telefono" label="Teléfono (opcional)" error={fieldErrors.telefono}>
                  <input id="reg-telefono" placeholder="Ej: +54 11 1234-5678" value={form.telefono} onChange={update('telefono')} />
                </Field>
                <Field id="reg-obrasocial" label="Obra social (opcional)" error={fieldErrors.obraSocial}>
                  <input id="reg-obrasocial" placeholder="Nombre de la obra social" value={form.obraSocial} onChange={update('obraSocial')} />
                </Field>
                <Field id="reg-plan" label="Plan (opcional)" error={fieldErrors.plan}>
                  <input id="reg-plan" placeholder="Ej: 210, básico, etc." value={form.plan} onChange={update('plan')} />
                </Field>
              </>
          )}

          {rol === 'MEDICO' && (
              <>
                <Field id="reg-matricula" label="Matrícula" error={fieldErrors.matricula}>
                  <input id="reg-matricula" placeholder="Número de matrícula" value={form.matricula} onChange={update('matricula')} />
                </Field>
                <Field id="reg-especialidad" label="ID de especialidad (opcional)" error={fieldErrors.especialidadId}>
                  <input id="reg-especialidad" placeholder="Ej: 1" value={form.especialidadId} onChange={update('especialidadId')} />
                </Field>
              </>
          )}

          {rol === 'ADMINISTRATIVO' && (
              <Field id="reg-consultorio" label="ID de consultorio (opcional)" error={fieldErrors.consultorioId}>
                <input id="reg-consultorio" placeholder="Ej: 1" value={form.consultorioId} onChange={update('consultorioId')} />
              </Field>
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