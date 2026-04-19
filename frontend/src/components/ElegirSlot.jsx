import { useEffect, useState } from 'react'
import { api } from '../api'

export default function ElegirSlot({ user, profesional, onDone, onCancel }) {
  const [slots, setSlots] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [dniPaciente, setDniPaciente] = useState('')
  const [agendando, setAgendando] = useState(false)

  useEffect(() => {
    api.disponibilidad(profesional.id)
      .then(setSlots)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [profesional.id])

  const agendar = async (slot) => {
    if (user.rol === 'ADMINISTRATIVO' && !dniPaciente.trim()) {
      alert('Primero ingresá el DNI del paciente')
      return
    }
    if (!confirm(`¿Confirmás el turno el ${slot.fecha} a las ${slot.hora.slice(0, 5)}?`)) return
    setAgendando(true)
    try {
      if (user.rol === 'PACIENTE') {
        await api.agendarTurnoPaciente({
          profesionalId: profesional.id,
          fecha: slot.fecha,
          hora: slot.hora
        })
      } else {
        await api.agendarTurnoAdmin({
          dniPaciente,
          profesionalId: profesional.id,
          fecha: slot.fecha,
          hora: slot.hora
        })
      }
      alert('Turno agendado')
      onDone()
    } catch (err) {
      alert(err.message)
    } finally {
      setAgendando(false)
    }
  }

  // Agrupar slots por fecha
  const porFecha = slots.reduce((acc, s) => {
    acc[s.fecha] = acc[s.fecha] || []
    acc[s.fecha].push(s)
    return acc
  }, {})

  return (
    <div className="card">
      <div className="card-header">
        <h2>Turnos disponibles — Dr/a. {profesional.nombre}</h2>
        <button className="link" onClick={onCancel}>← Volver</button>
      </div>

      {user.rol === 'ADMINISTRATIVO' && (
        <div className="admin-dni">
          <label>DNI del paciente:</label>
          <input
            placeholder="DNI"
            value={dniPaciente}
            onChange={(e) => setDniPaciente(e.target.value)}
          />
        </div>
      )}

      {loading && <p>Cargando disponibilidad...</p>}
      {error && <p className="error">{error}</p>}
      {!loading && slots.length === 0 && (
        <p>No hay turnos disponibles en los próximos 30 días.</p>
      )}

      <div className="disponibilidad">
        {Object.entries(porFecha).map(([fecha, slotsDia]) => (
          <div key={fecha} className="dia-block">
            <h4>{fecha}</h4>
            <div className="slots-grid">
              {slotsDia.map((s, i) => (
                <button
                  key={i}
                  className="slot-btn"
                  disabled={agendando}
                  onClick={() => agendar(s)}
                  title={s.consultorioNombre}
                >
                  {s.hora.slice(0, 5)}
                  <span className="slot-consultorio">{s.consultorioNombre}</span>
                </button>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
