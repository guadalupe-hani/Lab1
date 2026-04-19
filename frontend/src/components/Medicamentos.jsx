import { useEffect, useState } from 'react'
import { api } from '../api'

export default function Medicamentos() {
  const [lista, setLista] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editando, setEditando] = useState(null) // null | 'nuevo' | medicamento object

  const cargar = () => {
    setLoading(true)
    api.medicamentos()
      .then((data) => { setLista(data); setError('') })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  useEffect(() => { cargar() }, [])

  const handleEliminar = async (id) => {
    if (!confirm('¿Dar de baja este medicamento? No aparecerá en las recetas nuevas pero las existentes seguirán mostrándolo.')) return
    try {
      await api.eliminarMedicamento(id)
      cargar()
    } catch (e) { alert(e.message) }
  }

  const handleReactivar = async (m) => {
    try {
      await api.editarMedicamento(m.id, { activo: true })
      cargar()
    } catch (e) { alert(e.message) }
  }

  if (editando) {
    return <MedicamentoForm
      medicamento={editando === 'nuevo' ? null : editando}
      onDone={() => { setEditando(null); cargar() }}
      onCancel={() => setEditando(null)}
    />
  }

  return (
    <div className="card">
      <div className="card-header">
        <h2>Medicamentos</h2>
        <button onClick={() => setEditando('nuevo')}>+ Nuevo medicamento</button>
      </div>
      {loading && <p>Cargando...</p>}
      {error && <p className="error">{error}</p>}
      {!loading && lista.length === 0 && <p>No hay medicamentos cargados.</p>}
      <ul className="medicamentos-lista">
        {lista.map((m) => (
          <li key={m.id} className={`medicamento-item ${!m.activo ? 'inactivo' : ''}`}>
            <div className="medicamento-main">
              <strong>{m.nombre}</strong>
              {m.presentacion && <span className="medicamento-presentacion"> — {m.presentacion}</span>}
              {!m.activo && <span className="badge-inactivo">Inactivo</span>}
              {m.descripcion && <div className="medicamento-desc">{m.descripcion}</div>}
            </div>
            <div className="medicamento-actions">
              <button className="link" onClick={() => setEditando(m)}>Editar</button>
              {m.activo
                ? <button className="link danger" onClick={() => handleEliminar(m.id)}>Dar de baja</button>
                : <button className="link" onClick={() => handleReactivar(m)}>Reactivar</button>}
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}

function MedicamentoForm({ medicamento, onDone, onCancel }) {
  const [nombre, setNombre] = useState(medicamento?.nombre || '')
  const [descripcion, setDescripcion] = useState(medicamento?.descripcion || '')
  const [presentacion, setPresentacion] = useState(medicamento?.presentacion || '')
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      if (medicamento) {
        await api.editarMedicamento(medicamento.id, { nombre, descripcion, presentacion })
      } else {
        await api.crearMedicamento({ nombre, descripcion, presentacion })
      }
      onDone()
    } catch (err) { setError(err.message) }
  }

  return (
    <div className="card">
      <div className="card-header">
        <h2>{medicamento ? 'Editar medicamento' : 'Nuevo medicamento'}</h2>
        <button className="link" onClick={onCancel}>← Volver</button>
      </div>
      <form onSubmit={handleSubmit}>
        <input placeholder="Nombre (ej. Ibuprofeno)" value={nombre} onChange={(e) => setNombre(e.target.value)} required />
        <input placeholder="Presentación (ej. Comprimidos 400mg)" value={presentacion} onChange={(e) => setPresentacion(e.target.value)} />
        <textarea placeholder="Descripción (opcional)" value={descripcion} onChange={(e) => setDescripcion(e.target.value)} rows={3} />
        <div className="actions">
          <button type="submit">{medicamento ? 'Guardar cambios' : 'Crear'}</button>
          <button type="button" className="link" onClick={onCancel}>Cancelar</button>
        </div>
      </form>
      {error && <p className="error">{error}</p>}
    </div>
  )
}
