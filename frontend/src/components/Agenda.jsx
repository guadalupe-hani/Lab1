import { useEffect, useState } from 'react'
import { api } from '../api'

const DIAS = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO']

export default function Agenda({ user }) {
  // Para admin: seleccionar médico
  const [medicos, setMedicos] = useState([])
  const [medicoSel, setMedicoSel] = useState('')
  const [errorMedicos, setErrorMedicos] = useState('')

  useEffect(() => {
    if (user.rol === 'ADMINISTRATIVO') {
      api.medicosDelConsultorio()
        .then(setMedicos)
        .catch((err) => setErrorMedicos(err.message))
    }
  }, [user.rol])

  if (user.rol === 'ADMINISTRATIVO') {
    return (
      <div>
        <div className="card">
          <h2>Mi agenda (admin)</h2>
          {errorMedicos && <p className="error">{errorMedicos}</p>}
          {!errorMedicos && medicos.length === 0 && (
            <p>No hay médicos asignados a tu consultorio todavía. Cuando agregues horarios para un médico, va a aparecer acá.</p>
          )}
          {medicos.length > 0 && (
            <>
              <label>Elegí un médico:</label>
              <select value={medicoSel} onChange={(e) => setMedicoSel(e.target.value)}>
                <option value="">-- Seleccionar --</option>
                {medicos.map((m) => (
                  <option key={m.id} value={m.id}>
                    {m.nombre} ({m.especialidad || 'sin especialidad'})
                  </option>
                ))}
              </select>
            </>
          )}
          <p style={{ marginTop: 12 }}>
            <button className="link" onClick={() => { /* placeholder: agregar nuevo médico al consultorio */ }}>
              {/* no-op for now */}
            </button>
          </p>
        </div>
        {medicoSel && (
          <>
            <HorariosSection user={user} profesionalId={Number(medicoSel)} />
            <DiasLibresSection user={user} profesionalId={Number(medicoSel)} />
          </>
        )}
        {!medicoSel && medicos.length === 0 && (
          <AdminAgregarMedico onDone={() => api.medicosDelConsultorio().then(setMedicos)} />
        )}
      </div>
    )
  }

  // MEDICO
  return (
    <div>
      <HorariosSection user={user} />
      <DiasLibresSection user={user} />
    </div>
  )
}

// Permite al admin crear el primer horario de un médico (así queda "asignado" al consultorio)
function AdminAgregarMedico({ onDone }) {
  const [profesionalId, setProfesionalId] = useState('')
  const [diaSemana, setDiaSemana] = useState('LUNES')
  const [horaInicio, setHoraInicio] = useState('')
  const [horaFin, setHoraFin] = useState('')
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(''); setOk('')
    try {
      await api.crearHorarioAdmin({
        profesionalId: Number(profesionalId),
        diaSemana, horaInicio, horaFin
      })
      setOk('Horario creado. El médico ahora aparece en tu consultorio.')
      setTimeout(onDone, 1000)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="card">
      <h3>Asignar un médico a tu consultorio</h3>
      <p className="hint">Creá el primer horario de trabajo de un médico en este consultorio indicando su ID.</p>
      <form onSubmit={handleSubmit}>
        <input placeholder="ID del profesional" value={profesionalId} onChange={(e) => setProfesionalId(e.target.value)} required />
        <select value={diaSemana} onChange={(e) => setDiaSemana(e.target.value)}>
          {DIAS.map((d) => <option key={d}>{d}</option>)}
        </select>
        <input type="time" placeholder="Hora inicio" value={horaInicio} onChange={(e) => setHoraInicio(e.target.value)} required />
        <input type="time" placeholder="Hora fin" value={horaFin} onChange={(e) => setHoraFin(e.target.value)} required />
        <button type="submit">Crear horario</button>
      </form>
      {error && <p className="error">{error}</p>}
      {ok && <p className="ok">{ok}</p>}
    </div>
  )
}

function HorariosSection({ user, profesionalId }) {
  const [lista, setLista] = useState([])
  const [consultorios, setConsultorios] = useState([])
  const [error, setError] = useState('')
  const [mostrandoForm, setMostrandoForm] = useState(false)

  const cargar = () => {
    const promise = user.rol === 'MEDICO'
      ? api.horariosMios()
      : api.horariosDeProfesional(profesionalId)
    promise.then(setLista).catch((e) => setError(e.message))
  }

  useEffect(() => {
    cargar()
    if (user.rol === 'MEDICO') {
      api.consultorios().then(setConsultorios).catch(() => {})
    }
  }, [user.rol, profesionalId])

  const handleEliminar = async (id) => {
    if (!confirm('¿Eliminar este horario?')) return
    try {
      await api.eliminarHorario(id)
      cargar()
    } catch (e) { setError(e.message) }
  }

  return (
    <div className="card">
      <div className="card-header">
        <h3>Horarios de trabajo</h3>
        <button onClick={() => setMostrandoForm(!mostrandoForm)}>
          {mostrandoForm ? 'Cerrar' : '+ Agregar horario'}
        </button>
      </div>
      {error && <p className="error">{error}</p>}
      {lista.length === 0 && <p>No hay horarios cargados.</p>}
      <ul className="horarios-lista">
        {lista.map((h) => (
          <li key={h.id} className="horario-item">
            <div>
              <strong>{h.diaSemana}</strong> — {h.horaInicio?.slice(0, 5)} a {h.horaFin?.slice(0, 5)}
              <div className="horario-meta">{h.consultorioNombre}</div>
            </div>
            <button className="link danger" onClick={() => handleEliminar(h.id)}>Eliminar</button>
          </li>
        ))}
      </ul>
      {mostrandoForm && (
        <HorarioForm
          user={user}
          profesionalId={profesionalId}
          consultorios={consultorios}
          onDone={() => { cargar(); setMostrandoForm(false) }}
        />
      )}
    </div>
  )
}

function HorarioForm({ user, profesionalId, consultorios, onDone }) {
  const [consultorioId, setConsultorioId] = useState('')
  const [diaSemana, setDiaSemana] = useState('LUNES')
  const [horaInicio, setHoraInicio] = useState('')
  const [horaFin, setHoraFin] = useState('')
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      if (user.rol === 'MEDICO') {
        await api.crearHorarioMedico({
          consultorioId: Number(consultorioId), diaSemana, horaInicio, horaFin
        })
      } else {
        await api.crearHorarioAdmin({
          profesionalId, diaSemana, horaInicio, horaFin
        })
      }
      onDone()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="form-inline">
      {user.rol === 'MEDICO' && (
        <select value={consultorioId} onChange={(e) => setConsultorioId(e.target.value)} required>
          <option value="">-- Consultorio --</option>
          {consultorios.map((c) => <option key={c.id} value={c.id}>{c.nombre}</option>)}
        </select>
      )}
      <select value={diaSemana} onChange={(e) => setDiaSemana(e.target.value)}>
        {DIAS.map((d) => <option key={d}>{d}</option>)}
      </select>
      <input type="time" value={horaInicio} onChange={(e) => setHoraInicio(e.target.value)} required />
      <input type="time" value={horaFin} onChange={(e) => setHoraFin(e.target.value)} required />
      <button type="submit">Guardar</button>
      {error && <p className="error">{error}</p>}
    </form>
  )
}

function DiasLibresSection({ user, profesionalId }) {
  const [lista, setLista] = useState([])
  const [error, setError] = useState('')
  const [mostrandoForm, setMostrandoForm] = useState(false)

  const cargar = () => {
    const promise = user.rol === 'MEDICO'
      ? api.diasLibresMios()
      : api.diasLibresDeProfesional(profesionalId)
    promise.then(setLista).catch((e) => setError(e.message))
  }

  useEffect(() => { cargar() }, [user.rol, profesionalId])

  const handleEliminar = async (id) => {
    if (!confirm('¿Eliminar este día libre?')) return
    try {
      await api.eliminarDiaLibre(id)
      cargar()
    } catch (e) { setError(e.message) }
  }

  return (
    <div className="card">
      <div className="card-header">
        <h3>Días libres</h3>
        <button onClick={() => setMostrandoForm(!mostrandoForm)}>
          {mostrandoForm ? 'Cerrar' : '+ Agregar día libre'}
        </button>
      </div>
      {error && <p className="error">{error}</p>}
      {lista.length === 0 && <p>No hay días libres cargados.</p>}
      <ul className="horarios-lista">
        {lista.map((d) => (
          <li key={d.id} className="horario-item">
            <div>
              <strong>{d.fecha}</strong>
              {d.motivo && <div className="horario-meta">{d.motivo}</div>}
            </div>
            <button className="link danger" onClick={() => handleEliminar(d.id)}>Eliminar</button>
          </li>
        ))}
      </ul>
      {mostrandoForm && (
        <DiaLibreForm
          user={user}
          profesionalId={profesionalId}
          onDone={() => { cargar(); setMostrandoForm(false) }}
        />
      )}
    </div>
  )
}

function DiaLibreForm({ user, profesionalId, onDone }) {
  const [fecha, setFecha] = useState('')
  const [motivo, setMotivo] = useState('')
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      if (user.rol === 'MEDICO') {
        await api.crearDiaLibreMedico({ fecha, motivo: motivo || null })
      } else {
        await api.crearDiaLibreAdmin({ profesionalId, fecha, motivo: motivo || null })
      }
      onDone()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="form-inline">
      <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} required />
      <input placeholder="Motivo (opcional)" value={motivo} onChange={(e) => setMotivo(e.target.value)} />
      <button type="submit">Guardar</button>
      {error && <p className="error">{error}</p>}
    </form>
  )
}
