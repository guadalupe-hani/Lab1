import { useEffect, useState } from 'react'
import { api } from '../api'
import BuscarMedico from './BuscarMedico'
import ElegirSlot from './ElegirSlot'

export default function Turnos({ user }) {
  const [lista, setLista] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [vista, setVista] = useState('lista') // 'lista' | 'buscar' | 'slots'
  const [profesionalSel, setProfesionalSel] = useState(null) // { id, nombre }

  const cargar = () => {
    setLoading(true)
    api.turnosMios()
      .then((data) => { setLista(data); setError('') })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => { cargar() }, [])

  const handleCancelar = async (id) => {
    const motivo = user.rol === 'MEDICO' || user.rol === 'ADMINISTRATIVO'
      ? prompt('Motivo de la cancelación (opcional):')
      : null
    if (motivo === null && (user.rol === 'MEDICO' || user.rol === 'ADMINISTRATIVO')) return
    if (!confirm('¿Confirmás la cancelación del turno?')) return
    try {
      await api.cancelarTurno(id, motivo || '')
      cargar()
    } catch (e) { alert(e.message) }
  }

  const handleReagendar = (turno) => {
    setProfesionalSel({ id: turno.profesionalId, nombre: turno.profesionalNombre })
    setVista('slots')
  }

  if (vista === 'buscar') {
    return <BuscarMedico
      user={user}
      onElegir={(p) => { setProfesionalSel(p); setVista('slots') }}
      onCancel={() => setVista('lista')}
    />
  }
  if (vista === 'slots' && profesionalSel) {
    return <ElegirSlot
      user={user}
      profesional={profesionalSel}
      onDone={() => { setVista('lista'); cargar() }}
      onCancel={() => { setVista('lista'); setProfesionalSel(null) }}
    />
  }

  const mensajeEstado = (t) => {
    if (t.estado === 'PROGRAMADO') return null
    if (t.estado === 'CANCELADO') {
      if (user.rol === 'PACIENTE') {
        if (t.canceladoPor === 'PACIENTE') return 'Has cancelado este turno'
        return 'Tu turno ha sido cancelado' + (t.motivoCancelacion ? ` — "${t.motivoCancelacion}"` : '')
      }
      return `Cancelado por ${t.canceladoPor?.toLowerCase() || 'alguien'}` + (t.motivoCancelacion ? ` — "${t.motivoCancelacion}"` : '')
    }
    return t.estado
  }

  const titulo = {
    PACIENTE: 'Mis turnos',
    MEDICO: 'Turnos conmigo',
    ADMINISTRATIVO: 'Turnos del consultorio'
  }[user.rol] || 'Turnos'

  return (
    <div className="card">
      <div className="card-header">
        <h2>{titulo}</h2>
        {(user.rol === 'PACIENTE' || user.rol === 'ADMINISTRATIVO') && (
          <button onClick={() => setVista('buscar')}>+ Agendar turno</button>
        )}
      </div>
      {loading && <p>Cargando...</p>}
      {error && <p className="error">{error}</p>}
      {!loading && lista.length === 0 && <p>No tenés turnos.</p>}
      <ul className="turnos-lista">
        {lista.map((t) => {
          const msg = mensajeEstado(t)
          const canceladoPorOtro = t.estado === 'CANCELADO' && t.canceladoPor !== 'PACIENTE'
          return (
            <li key={t.id} className={`turno-item estado-${t.estado?.toLowerCase()}`}>
              <div className="turno-main">
                <div className="turno-fecha">
                  <strong>{t.fecha}</strong> a las <strong>{t.hora?.slice(0, 5)}</strong>
                </div>
                <div className="turno-info">
                  {user.rol === 'PACIENTE' && <>Dr/a. {t.profesionalNombre} — {t.profesionalEspecialidad || 'sin especialidad'}</>}
                  {user.rol === 'MEDICO' && <>Paciente: {t.pacienteNombre} (DNI {t.pacienteDni})</>}
                  {user.rol === 'ADMINISTRATIVO' && <>{t.profesionalNombre} con {t.pacienteNombre}</>}
                </div>
                <div className="turno-consultorio">
                  📍 {t.consultorioNombre} — {t.consultorioDireccion}
                </div>
                {msg && <div className="turno-estado-msg">{msg}</div>}
              </div>
              <div className="turno-actions">
                {t.estado === 'PROGRAMADO' && (
                  <button className="link danger" onClick={() => handleCancelar(t.id)}>Cancelar</button>
                )}
                {t.estado === 'CANCELADO' && user.rol === 'PACIENTE' && canceladoPorOtro && (
                  <button onClick={() => handleReagendar(t)}>Reagendar</button>
                )}
              </div>
            </li>
          )
        })}
      </ul>
    </div>
  )
}
