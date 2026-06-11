import { useEffect, useState } from 'react'
import { api } from '../api'
import ConfirmModal from './ConfirmModal'

export default function ElegirSlot({ user, profesional, onDone, onCancel }) {
  const [slots, setSlots] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [dniPaciente, setDniPaciente] = useState('')
  const [agendando, setAgendando] = useState(false)
  const [slotSeleccionado, setSlotSeleccionado] = useState(null)
  const [mostrarExito, setMostrarExito] = useState(false)
  const [busquedaPaciente, setBusquedaPaciente] = useState('')
  const [sugerencias, setSugerencias] = useState([])
  const [pacienteSel, setPacienteSel] = useState(null)

  useEffect(() => {
    api.disponibilidad(profesional.id)
      .then(setSlots)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [profesional.id])

  const handleSeleccionarSlot = (slot) => {
    if (user.rol === 'ADMINISTRATIVO' && !dniPaciente.trim()) {
      alert('Primero ingresá el DNI del paciente')
      return
    }
    setSlotSeleccionado(slot)
  }

  const confirmarTurno = async () => {
    if (!slotSeleccionado) return

    setAgendando(true)
    const slot = slotSeleccionado
    setSlotSeleccionado(null)

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
      //alert('Turno agendado exitosamente')
      //onDone()
      setMostrarExito(true)
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
          <div className="admin-dni" style={{ flexDirection: 'column', alignItems: 'stretch', gap: 8 }}>
            <label>Buscar paciente (nombre, apellido o DNI):</label>
            <div style={{ position: 'relative' }}>
              <input
                  placeholder="Escribí para buscar..."
                  value={busquedaPaciente}
                  onChange={(e) => {
                    setBusquedaPaciente(e.target.value)
                    setPacienteSel(null)
                    setDniPaciente('')
                    if (e.target.value.length >= 2) {
                      api.buscarPacientes(e.target.value).then(setSugerencias).catch(() => {})
                    } else {
                      setSugerencias([])
                    }
                  }}
              />
              {sugerencias.length > 0 && !pacienteSel && (
                  <ul style={{
                    position: 'absolute', top: '100%', left: 0, right: 0,
                    background: '#fff', border: '1px solid var(--border)',
                    borderRadius: 8, zIndex: 10, listStyle: 'none',
                    margin: 0, padding: 0, maxHeight: 200, overflowY: 'auto',
                    boxShadow: 'var(--shadow)'
                  }}>
                    {sugerencias.map((p) => (
                        <li
                            key={p.id}
                            style={{ padding: '10px 14px', cursor: 'pointer', fontSize: 14 }}
                            onMouseEnter={(e) => e.currentTarget.style.background = 'var(--info)'}
                            onMouseLeave={(e) => e.currentTarget.style.background = '#fff'}
                            onClick={() => {
                              setPacienteSel(p)
                              setBusquedaPaciente(`${p.nombre} (DNI: ${p.dni})`)
                              setDniPaciente(p.dni)
                              setSugerencias([])
                            }}
                        >
                          {p.nombre} — DNI: {p.dni}
                        </li>
                    ))}
                  </ul>
              )}
            </div>
            {pacienteSel && (
                <span style={{ fontSize: 13, color: 'var(--primary)' }}>
        ✓ Paciente seleccionado: {pacienteSel.nombre}
      </span>
            )}
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
                  onClick={() => handleSeleccionarSlot(s)}
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
      <ConfirmModal
          open={slotSeleccionado !== null}
          title="Confirmar turno"
          message={`¿Confirmás el turno el ${slotSeleccionado?.fecha} a las ${slotSeleccionado?.hora?.slice(0, 5)}?`}
          confirmText="Confirmar"
          cancelText="Cancelar"
          danger={false} // no es accion de peligro como borrar
          onConfirm={confirmarTurno}
          onClose={() => setSlotSeleccionado(null)}
      />
      <ConfirmModal
          open={mostrarExito}
          title="¡Turno agendado!"
          message="El turno fue agendado exitosamente."
          confirmText="Aceptar"
          hideCancel={true}
          onConfirm={() => {
            setMostrarExito(false)
            onDone()
          }}
          onClose={() => {
            setMostrarExito(false)
            onDone()
          }}
      />
    </div>
  )
}
