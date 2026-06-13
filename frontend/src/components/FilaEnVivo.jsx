import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import { api } from '../api'

const ESTADO_LABEL = {
  ESPERANDO: 'Esperando',
  EN_ATENCION: 'En atención',
  ATENDIDO: 'Atendido',
  AUSENTE: 'Ausente',
}

// Rango de offset (minutos) que se muestra en la barra. Por fuera de este
// rango la barra queda llena (adelantado) o vacía (atrasado).
const BARRA_MIN = -30
const BARRA_MAX = 120

function porcentajeBarra(offsetMinutos) {
  const pct = ((BARRA_MAX - offsetMinutos) / (BARRA_MAX - BARRA_MIN)) * 100
  return Math.min(100, Math.max(0, pct))
}

function formatHora(hora) {
  return hora ? hora.slice(0, 5) : ''
}

export default function FilaEnVivo({ user }) {
  const [fila, setFila] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [accionando, setAccionando] = useState(false)
  const clientRef = useRef(null)

  const cargar = () => {
    api.filaEnVivo()
      .then((data) => { setFila(data); setError('') })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    cargar()
  }, [])

  // Suscripción en vivo: cuando la fila del profesional cambia, recargamos
  useEffect(() => {
    const profesionalId = fila?.profesionalId
    if (!profesionalId) return

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/fila/${profesionalId}`, () => cargar())
      },
    })
    client.activate()
    clientRef.current = client
    return () => { client.deactivate() }
  }, [fila?.profesionalId])

  const reportarRetrasoMedico = async (minutos) => {
    setAccionando(true)
    try {
      const data = await api.reportarRetrasoMedico(minutos)
      setFila((prev) => ({ ...prev, ...data }))
    } catch (e) {
      alert(e.message)
    } finally {
      setAccionando(false)
    }
  }

  const marcarEstado = async (turnoId, estado) => {
    setAccionando(true)
    try {
      const data = await api.marcarEstadoPaciente(turnoId, estado)
      setFila((prev) => ({ ...prev, ...data }))
    } catch (e) {
      alert(e.message)
    } finally {
      setAccionando(false)
    }
  }

  if (loading) return <div className="card"><p>Cargando...</p></div>
  if (error) return <div className="card"><p className="error">{error}</p></div>

  const turnos = fila?.turnos || []

  if (turnos.length === 0) {
    return (
      <div className="card">
        <div className="card-header"><h2>Fila en vivo</h2></div>
        <p>No hay turnos en la fila por ahora.</p>
      </div>
    )
  }

  const offset = fila.offsetMinutos || 0
  const fillPct = porcentajeBarra(offset)
  const esperaTexto = offset === 0
    ? 'En horario'
    : offset > 0
      ? `Espera estimada: +${offset} min`
      : `Vas ${Math.abs(offset)} min adelantado`

  return (
    <div className="card">
      <div className="card-header"><h2>Fila en vivo</h2></div>
      <div className="fila-bar-wrap">
        <div className="fila-bar-track">
          <div className="fila-bar-fill" style={{ width: `${fillPct}%` }} />
        </div>
        <div className="fila-bar-label">{esperaTexto}</div>
      </div>

      {user.rol === 'MEDICO' && (
        <div className="fila-actions">
          <span>Avisar atraso:</span>
          <button className="secondary" disabled={accionando} onClick={() => reportarRetrasoMedico(5)}>+5 min</button>
          <button className="secondary" disabled={accionando} onClick={() => reportarRetrasoMedico(10)}>+10 min</button>
          <button className="secondary" disabled={accionando} onClick={() => reportarRetrasoMedico(15)}>+15 min</button>
          <span>Voy más rápido:</span>
          <button className="secondary" disabled={accionando} onClick={() => reportarRetrasoMedico(-5)}>-5 min</button>
          <button className="secondary" disabled={accionando} onClick={() => reportarRetrasoMedico(-10)}>-10 min</button>
        </div>
      )}

      {user.rol === 'MEDICO' && (
        <ul className="turnos-lista">
          {turnos.map((t) => (
            <li key={t.turnoId} className="turno-item fila-item">
              <div className="turno-main">
                <div className="turno-fecha">
                  {formatHora(t.hora)}
                  {formatHora(t.hora) !== formatHora(t.horaEstimada) && <> → {formatHora(t.horaEstimada)}</>}
                  {t.posicion && <span className="fila-posicion"> · #{t.posicion} en la fila</span>}
                </div>
                <div className="turno-info">{t.pacienteNombre}{t.pacienteDni ? ` (DNI ${t.pacienteDni})` : ''}</div>
              </div>
              <div className="turno-actions">
                <span className={`fila-badge estado-${(t.estadoPaciente || '').toLowerCase()}`}>
                  {ESTADO_LABEL[t.estadoPaciente] || t.estadoPaciente}
                </span>
                {t.estadoPaciente === 'ESPERANDO' && (
                  <>
                    <button disabled={accionando} onClick={() => marcarEstado(t.turnoId, 'EN_ATENCION')}>En atención</button>
                    <button className="link danger" disabled={accionando} onClick={() => marcarEstado(t.turnoId, 'AUSENTE')}>Ausente</button>
                  </>
                )}
                {t.estadoPaciente === 'EN_ATENCION' && (
                  <button disabled={accionando} onClick={() => marcarEstado(t.turnoId, 'ATENDIDO')}>Atendido</button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
