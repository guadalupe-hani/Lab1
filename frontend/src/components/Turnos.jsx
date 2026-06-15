import { useEffect, useState } from 'react'
import { api } from '../api'
import BuscarMedico from './BuscarMedico'
import ElegirSlot from './ElegirSlot'
import ConfirmModal from './ConfirmModal'
import PromptModal from './PromptModal'

function googleCalendarUrl(t) {
  const fecha = t.fecha?.replace(/-/g, '') ?? ''
  const [h, m] = (t.hora?.slice(0, 5) ?? '00:00').split(':').map(Number)
  const pad = (n) => String(n).padStart(2, '0')
  const inicio = `${fecha}T${pad(h)}${pad(m)}00`
  const finMin = m + 30
  const finH = h + Math.floor(finMin / 60)
  const fin = `${fecha}T${pad(finH % 24)}${pad(finMin % 60)}00`
  const params = new URLSearchParams({
    action: 'TEMPLATE',
    text: `Turno médico — Dr/a. ${t.profesionalNombre ?? ''}`,
    dates: `${inicio}/${fin}`,
    details: `Especialidad: ${t.profesionalEspecialidad || 'sin especificar'}`,
    location: `${t.consultorioNombre ?? ''} — ${t.consultorioDireccion ?? ''}`,
  })
  return `https://calendar.google.com/calendar/render?${params.toString()}`
}

export default function Turnos({ user }) {
  const [lista, setLista] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [vista, setVista] = useState('lista') // 'lista' | 'buscar' | 'slots'
  const [profesionalSel, setProfesionalSel] = useState(null) // { id, nombre }
  const [turnoACancelar, setTurnoACancelar] = useState(null)
  const [idTurnoParaMotivo, setIdTurnoParaMotivo] = useState(null)
  const [pagando, setPagando] = useState(null)
  const [mensajePago, setMensajePago] = useState(null) // 'success' | 'pending' | 'failure' | null

  const cargar = () => {
    api.turnosMios()
        .then((data) => { setLista(data); setError('') })
        .catch((err) => setError(err.message))
        .finally(() => setLoading(false))
  }

  useEffect(() => {
    cargar()

    const params = new URLSearchParams(window.location.search)
    const pago = params.get('pago')
    if (pago) {
      setMensajePago(pago)
      const turnoId = params.get('external_reference')
      const status = params.get('status') || params.get('collection_status')
      if (turnoId && status) {
        api.confirmarPago(turnoId, status)
          .then(() => cargar())
          .catch(() => {})
      }
      window.history.replaceState({}, '', window.location.pathname)
    }
  }, [])

  const handleClickCancelar = (id) => {
    // Si es médico o admin, primero mostramos el modal del motivo
    if (user.rol === 'MEDICO' || user.rol === 'ADMINISTRATIVO') {
      setIdTurnoParaMotivo(id)
    } else {
      // Si es paciente, va directo al modal de confirmación final
      setTurnoACancelar({ id, motivo: '' })
    }
  }

  const alCompletarMotivo = (motivoEscrito) => {
    const id = idTurnoParaMotivo
    setIdTurnoParaMotivo(null)

    setTurnoACancelar({ id, motivo: motivoEscrito || '' })
  }

  const confirmarCancelacion = async () => {
    if (!turnoACancelar) return

    const { id, motivo } = turnoACancelar
    setTurnoACancelar(null)
    setLoading(true)
    try {
      await api.cancelarTurno(id, motivo)
      cargar()
    } catch (e) {
      alert(e.message)
      setLoading(false)
    }
  }

  const handleReagendar = (turno) => {
    setProfesionalSel({ id: turno.profesionalId, nombre: turno.profesionalNombre })
    setVista('slots')
  }

  const handlePagar = async (turnoId) => {
    setPagando(turnoId)
    try {
      const { initPoint } = await api.crearPreferenciaPago(turnoId)
      window.location.href = initPoint
    } catch (e) {
      alert(e.message)
      setPagando(null)
    }
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
      {mensajePago && (
        <div className={`pago-banner pago-banner-${mensajePago}`}>
          <span>
            {mensajePago === 'success' && '✅ ¡Pago realizado con éxito!'}
            {mensajePago === 'pending' && '⏳ Tu pago está pendiente de confirmación.'}
            {mensajePago === 'failure' && '❌ El pago no se pudo completar. Podés intentarlo nuevamente.'}
          </span>
          <button onClick={() => setMensajePago(null)}>✕</button>
        </div>
      )}
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
                    <>
                      {user.rol === 'PACIENTE' && (
                          <a
                              href={googleCalendarUrl(t)}
                              target="_blank"
                              rel="noreferrer"
                              className="btn-gcal"
                          >
                            + Google Calendar
                          </a>
                      )}
                      <button className="link danger" onClick={() => handleClickCancelar(t.id)}>Cancelar</button>
                    </>
                )}
                {user.rol === 'PACIENTE' && t.estadoPaciente === 'ATENDIDO' && t.estadoPago !== 'APROBADO' && (
                    <button onClick={() => handlePagar(t.id)} disabled={pagando === t.id}>
                      {pagando === t.id ? 'Redirigiendo...' : 'Pagar consulta'}
                    </button>
                )}
                {t.estadoPago === 'APROBADO' && (
                    <span className="turno-estado-msg">✅  Pago aprobado</span>
                )}
                {t.estado === 'CANCELADO' && user.rol === 'PACIENTE' && canceladoPorOtro && (
                  <button onClick={() => handleReagendar(t)}>Reagendar</button>
                )}
              </div>
            </li>
          )
        })}
      </ul>
      {idTurnoParaMotivo !== null && (
          <PromptModal
              open={true} // Siempre le pasamos true porque el control lo tiene la línea de arriba
              title="Motivo de cancelación"
              message="Por favor, indicá el motivo por el cual se cancela el turno."
              confirmText="Siguiente"
              onConfirm={alCompletarMotivo}
              onClose={() => setIdTurnoParaMotivo(null)}
          />
      )}
      <ConfirmModal
          open={turnoACancelar !== null}
          title="Cancelar turno"
          message="¿Seguro que querés cancelar este turno? Esta acción no se puede deshacer."
          confirmText="Sí, cancelar"
          cancelText="No, mantener"
          danger={true}
          onConfirm={confirmarCancelacion}
          onClose={() => setTurnoACancelar(null)}
      />
    </div>
  )
}
