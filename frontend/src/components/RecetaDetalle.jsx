import { useEffect, useState } from 'react'
import { api } from '../api'
import ConfirmModal from './ConfirmModal'

export default function RecetaDetalle({ id, user, onBack }) {
  const [receta, setReceta] = useState(null)
  const [error, setError] = useState('')
  const [mostrarModal, setMostrarModal] = useState(false)

  useEffect(() => {
    api.recetaDetalle(id)
      .then(setReceta)
      .catch((err) => setError(err.message))
  }, [id])

    const handleClickEliminar = () => {
        setMostrarModal(true)
    }

    const confirmarEliminacion = async () => {
        setMostrarModal(false)
        try {
            await api.eliminarReceta(id)
            onBack()
        } catch (err) {
            setError(err.message)
        }
    }

    const handleDescargarPdf = async () => {
        try {
            const blob = await api.recetaPdf(id)
            const url = window.URL.createObjectURL(blob)
            const a = document.createElement('a')
            a.href = url
            a.download = `receta-${id}.pdf`
            document.body.appendChild(a)
            a.click()
            a.remove()
            window.URL.revokeObjectURL(url)
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
        <div>
          <button onClick={handleDescargarPdf}>Descargar PDF</button>
          <button className="link" onClick={onBack}>← Volver</button>
        </div>
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
          <button className="danger" onClick={handleClickEliminar}>Eliminar receta</button>
        </div>
      )}
        <ConfirmModal
            open={mostrarModal}
            title="Eliminar receta"
            message="¿Seguro que querés eliminar esta receta? Esta acción es irreversible."
            confirmText="Sí, eliminar"
            danger={true}
            onConfirm={confirmarEliminacion}
            onClose={() => setMostrarModal(false)}
        />
    </div>
  )
}
