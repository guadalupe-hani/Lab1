import { useEffect, useState } from 'react'
import { api } from '../api'

export default function RecetaDetalle({ id, user, onBack }) {
  const [receta, setReceta] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api.recetaDetalle(id)
      .then(setReceta)
      .catch((err) => setError(err.message))
  }, [id])

  const handleEliminar = async () => {
    if (!confirm('¿Seguro que querés eliminar esta receta? Esta acción es irreversible.')) return
    try {
      await api.eliminarReceta(id)
      onBack()
    } catch (err) {
      setError(err.message)
    }
  }

  if (error) return (
    <div className="card">
      <p className="error">{error}</p>
      <button onClick={onBack}>Volver</button>
    </div>
  )
  if (!receta) return <div className="card"><p>Cargando...</p></div>

  return (
    <div className="card receta-detalle">
      <div className="card-header">
        <h2>Receta #{receta.id}</h2>
        <button className="link" onClick={onBack}>← Volver</button>
      </div>
      <p><strong>Fecha:</strong> {receta.fecha}</p>
      <p><strong>Paciente:</strong> {receta.pacienteNombre} (DNI {receta.pacienteDni})</p>
      <p><strong>Obra social:</strong> {receta.obraSocial || '—'}</p>
      <p><strong>Médico:</strong> {receta.medicoNombre} — Matrícula {receta.medicoMatricula}</p>
      <hr />
      <h3>Medicamentos prescritos</h3>
      {receta.items && receta.items.length > 0 ? (
        <ul className="items-receta-lista">
          {receta.items.map((it) => (
            <li key={it.id} className="item-receta">
              <div>
                <strong>{it.medicamentoNombre}</strong>
                {it.medicamentoPresentacion && <span> — {it.medicamentoPresentacion}</span>}
                <div className="item-detalle">
                  <strong>Dosis:</strong> {it.dosis}
                  {it.duracion && <> · <strong>Duración:</strong> {it.duracion}</>}
                </div>
                {it.indicaciones && <div className="item-detalle">{it.indicaciones}</div>}
              </div>
            </li>
          ))}
        </ul>
      ) : (
        <p>Sin medicamentos.</p>
      )}
      {receta.contenido && (
        <>
          <hr />
          <h3>Indicaciones generales</h3>
          <pre className="contenido">{receta.contenido}</pre>
        </>
      )}
      <hr />
      <p className="firma"><em>{receta.firma}</em></p>
      {user.rol === 'MEDICO' && (
        <div className="actions">
          <button className="danger" onClick={handleEliminar}>Eliminar receta</button>
        </div>
      )}
    </div>
  )
}
