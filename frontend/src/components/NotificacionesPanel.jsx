import { useState, useEffect, useRef } from 'react'
import { api } from '../api'

function BellIcon({ count }) {
  return (
    <div className="notif-bell-wrap">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
        strokeLinecap="round" strokeLinejoin="round" className="notif-bell-icon">
        <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.73 21a2 2 0 0 1-3.46 0" />
      </svg>
      {count > 0 && (
        <span className="notif-badge">{count > 99 ? '99+' : count}</span>
      )}
    </div>
  )
}

function formatFecha(fechaStr) {
  const fecha = new Date(fechaStr)
  const ahora = new Date()
  const diffMs = ahora - fecha
  const diffMin = Math.floor(diffMs / 60000)
  const diffHs = Math.floor(diffMs / 3600000)
  const diffDias = Math.floor(diffMs / 86400000)

  if (diffMin < 1) return 'Ahora'
  if (diffMin < 60) return `Hace ${diffMin} min`
  if (diffHs < 24) return `Hace ${diffHs} h`
  if (diffDias === 1) return 'Ayer'
  return fecha.toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit' })
}

export default function NotificacionesPanel() {
  const [abierto, setAbierto] = useState(false)
  const [notifs, setNotifs] = useState([])
  const [noLeidas, setNoLeidas] = useState(0)
  const [cargando, setCargando] = useState(false)
  const panelRef = useRef(null)

  // Polling cada 30 segundos del contador de no leídas
  useEffect(() => {
    const fetchCount = () => {
      api.noLeidas()
        .then((data) => setNoLeidas(data.count ?? 0))
        .catch(() => {})
    }
    fetchCount()
    const interval = setInterval(fetchCount, 30000)
    return () => clearInterval(interval)
  }, [])

  // Cargar lista completa al abrir
  useEffect(() => {
    if (abierto) {
      setCargando(true)
      api.notificaciones()
        .then((data) => setNotifs(data))
        .catch(() => {})
        .finally(() => setCargando(false))
    }
  }, [abierto])

  // Cerrar al hacer click fuera
  useEffect(() => {
    const handleClickFuera = (e) => {
      if (panelRef.current && !panelRef.current.contains(e.target)) {
        setAbierto(false)
      }
    }
    if (abierto) {
      document.addEventListener('mousedown', handleClickFuera)
    }
    return () => document.removeEventListener('mousedown', handleClickFuera)
  }, [abierto])

  const handleMarcarLeida = async (id) => {
    try {
      await api.marcarLeida(id)
      setNotifs((prev) =>
        prev.map((n) => (n.id === id ? { ...n, leida: true } : n))
      )
      setNoLeidas((prev) => Math.max(0, prev - 1))
    } catch {}
  }

  const handleMarcarTodas = async () => {
    try {
      await api.marcarTodasLeidas()
      setNotifs((prev) => prev.map((n) => ({ ...n, leida: true })))
      setNoLeidas(0)
    } catch {}
  }

  return (
    <div className="notif-container" ref={panelRef}>
      <button
        className={'notif-btn' + (abierto ? ' active' : '')}
        onClick={() => setAbierto((v) => !v)}
        title="Notificaciones"
      >
        <BellIcon count={noLeidas} />
      </button>

      {abierto && (
        <div className="notif-panel">
          <div className="notif-panel-header">
            <span className="notif-panel-title">Notificaciones</span>
            {noLeidas > 0 && (
              <button className="notif-marcar-todas" onClick={handleMarcarTodas}>
                Marcar todas como leídas
              </button>
            )}
          </div>

          <div className="notif-list">
            {cargando && (
              <div className="notif-empty">Cargando...</div>
            )}
            {!cargando && notifs.length === 0 && (
              <div className="notif-empty">No tenés notificaciones.</div>
            )}
            {!cargando && notifs.map((n) => (
              <div
                key={n.id}
                className={'notif-item' + (n.leida ? ' leida' : ' no-leida')}
                onClick={() => !n.leida && handleMarcarLeida(n.id)}
              >
                <div className="notif-mensaje">{n.mensaje}</div>
                <div className="notif-meta">
                  <span className="notif-fecha">{formatFecha(n.fechaCreacion)}</span>
                  {!n.leida && <span className="notif-dot" />}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
