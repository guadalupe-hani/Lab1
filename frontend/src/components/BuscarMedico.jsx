import { useEffect, useState } from 'react'
import { api } from '../api'

export default function BuscarMedico({ onElegir, onCancel }) {
  const [especialidades, setEspecialidades] = useState([])
  const [especialidadId, setEspecialidadId] = useState('')
  const [nombre, setNombre] = useState('')
  const [resultados, setResultados] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [buscado, setBuscado] = useState(false)

  useEffect(() => {
    api.especialidades().then(setEspecialidades).catch(() => {})
  }, [])

  const buscar = async (e) => {
    if (e) e.preventDefault()
    setLoading(true); setError('')
    try {
      const data = await api.buscarProfesionales({
        especialidadId: especialidadId || undefined,
        nombre: nombre || undefined
      })
      setResultados(data)
      setBuscado(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card">
      <div className="card-header">
        <h2>Buscar médico</h2>
        <button className="link" onClick={onCancel}>← Volver</button>
      </div>
      <form onSubmit={buscar} className="form-inline">
        <select value={especialidadId} onChange={(e) => setEspecialidadId(e.target.value)}>
          <option value="">Todas las especialidades</option>
          {especialidades.map((e) => <option key={e.id} value={e.id}>{e.nombre}</option>)}
        </select>
        <input placeholder="Nombre del médico" value={nombre} onChange={(e) => setNombre(e.target.value)} />
        <button type="submit">Buscar</button>
      </form>
      {loading && <p>Buscando...</p>}
      {error && <p className="error">{error}</p>}
      {buscado && !loading && resultados.length === 0 && <p>No se encontraron médicos.</p>}
      <ul className="medicos-lista">
        {resultados.map((m) => (
          <li key={m.id} className="medico-item">
            <div className="medico-header">
              <div>
                <strong>Dr/a. {m.nombre}</strong>
                <div className="medico-meta">
                  {m.especialidad || 'Sin especialidad'} — Matrícula {m.matricula}
                </div>
              </div>
              <button onClick={() => onElegir(m)} disabled={m.horarios.length === 0}>
                Ver turnos
              </button>
            </div>
            {m.horarios.length === 0 ? (
              <p className="hint">Este médico todavía no tiene horarios cargados.</p>
            ) : (
              <div className="medico-horarios">
                {m.horarios.map((h, i) => (
                  <div key={i} className="mini-horario">
                    {h.diaSemana} {h.horaInicio?.slice(0, 5)}-{h.horaFin?.slice(0, 5)} · {h.consultorioNombre}
                  </div>
                ))}
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  )
}
