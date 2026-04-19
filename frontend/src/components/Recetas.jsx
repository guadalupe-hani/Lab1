import { useEffect, useState } from 'react'
import { api } from '../api'
import CrearReceta from './CrearReceta'
import RecetaDetalle from './RecetaDetalle'

export default function Recetas({ user }) {
  const [lista, setLista] = useState([])
  const [error, setError] = useState('')
  const [vista, setVista] = useState('lista') // 'lista' | 'crear' | 'detalle'
  const [detalleId, setDetalleId] = useState(null)
  const [loading, setLoading] = useState(true)

  const cargar = () => {
    setLoading(true)
    api.recetasMias()
      .then((data) => { setLista(data); setError('') })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => { cargar() }, [])

  if (vista === 'crear') {
    return <CrearReceta onDone={() => { cargar(); setVista('lista') }} onCancel={() => setVista('lista')} />
  }
  if (vista === 'detalle' && detalleId) {
    return <RecetaDetalle id={detalleId} user={user} onBack={() => { setVista('lista'); cargar() }} />
  }

  return (
    <div className="card">
      <div className="card-header">
        <h2>Mis recetas</h2>
        {user.rol === 'MEDICO' && (
          <button onClick={() => setVista('crear')}>+ Nueva receta</button>
        )}
      </div>
      {loading && <p>Cargando...</p>}
      {error && <p className="error">{error}</p>}
      {!loading && lista.length === 0 && <p>No tenés recetas todavía.</p>}
      <ul className="recetas-lista">
        {lista.map((r) => (
          <li key={r.id} className="receta-item" onClick={() => { setDetalleId(r.id); setVista('detalle') }}>
            <div>
              <strong>{r.fecha}</strong>
              <span className="receta-preview"> — {r.contenido?.slice(0, 60)}{r.contenido?.length > 60 ? '...' : ''}</span>
            </div>
            <div className="receta-meta">
              {user.rol === 'PACIENTE' ? r.medicoNombre : `Paciente: ${r.pacienteNombre}`}
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}
