import { useEffect, useState } from 'react'
import { api } from '../api'

export default function CrearReceta({ onDone, onCancel }) {
  const [busqueda, setBusqueda] = useState('')
  const [resultados, setResultados] = useState([])
  const [pacienteSel, setPacienteSel] = useState(null)
  const [dniPaciente, setDniPaciente] = useState('')
  const [medicamentos, setMedicamentos] = useState([])
  const [items, setItems] = useState([]) // [{medicamentoId, dosis, duracion, indicaciones}]
  const [indicacionesGenerales, setIndicacionesGenerales] = useState('')
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')

  // Para el agregado de un nuevo item
  const [medSel, setMedSel] = useState('')
  const [dosis, setDosis] = useState('')
  const [duracion, setDuracion] = useState('')
  const [indicacionesItem, setIndicacionesItem] = useState('')

  useEffect(() => {
    api.medicamentosActivos().then(setMedicamentos).catch(() => {})
  }, [])

  useEffect(() => {
    if (busqueda.trim().length > 1) {
      api.buscarPacientes(busqueda)
          .then(setResultados)
          .catch(() => setResultados([]))
    }
  }, [busqueda])

  const agregarItem = () => {
    if (!medSel) { alert('Elegí un medicamento'); return }
    if (!dosis.trim()) { alert('La dosis es obligatoria'); return }
    const med = medicamentos.find((m) => String(m.id) === String(medSel))
    setItems([...items, {
      medicamentoId: Number(medSel),
      medicamentoNombre: med?.nombre,
      medicamentoPresentacion: med?.presentacion,
      dosis, duracion, indicaciones: indicacionesItem
    }])
    setMedSel(''); setDosis(''); setDuracion(''); setIndicacionesItem('')
  }

  const quitarItem = (idx) => setItems(items.filter((_, i) => i !== idx))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(''); setOk('')
    if (items.length === 0) { setError('Agregá al menos un medicamento'); return }
    try {
      await api.crearReceta({
        dniPaciente,
        items: items.map(({ medicamentoId, dosis, duracion, indicaciones }) => ({ medicamentoId, dosis, duracion: duracion || null, indicaciones: indicaciones || null })),
        indicacionesGenerales: indicacionesGenerales || null
      })
      setOk('Receta emitida correctamente')
      setTimeout(onDone, 800)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="card">
      <div className="card-header">
        <h2>Nueva receta</h2>
        <button className="link" onClick={onCancel}>← Volver</button>
      </div>
      <form onSubmit={handleSubmit}>
        <div className="field" style={{ position: 'relative' }}>
          <label>Paciente</label>
          {!pacienteSel ? (
              <>
                <input
                    placeholder="Buscar por nombre o DNI..."
                    value={busqueda}
                    onChange={(e) => {
                      const valor = e.target.value;
                      setBusqueda(valor);
                      if (valor.trim().length <= 1) {
                        setResultados([]);
                      }
                    }}
                />

                {resultados.length > 0 && (
                    <ul className="resultados-busqueda" style={{
                      position: 'absolute',
                      top: '100%',
                      left: 0,
                      width: '100%',
                      zIndex: 100,
                      border: '1px solid var(--border)',
                      borderRadius: '8px',
                      listStyle: 'none',
                      padding: 0,
                      marginTop: '4px',
                      maxHeight: '200px',
                      overflowY: 'auto',
                      background: 'white',
                      boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                    }}>
                      {resultados.map((p) => (
                          <li
                              key={p.dni}
                              onClick={() => {
                                setPacienteSel(p)
                                setDniPaciente(p.dni)
                                setBusqueda('')
                                setResultados([])
                              }}
                              className="resultado-item"
                              style={{ padding: '12px', cursor: 'pointer', borderBottom: '1px solid var(--border-soft)', color: '#333' }}
                          >
                            <strong>{p.nombre}</strong> (DNI: {p.dni})
                          </li>
                      ))}
                    </ul>
                )}
              </>
          ) : (
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '12px',
                background: 'var(--bg-soft)',
                borderRadius: '8px',
                border: '1px solid var(--primary-soft)'
              }}>
                <span><strong>{pacienteSel.nombre}</strong> (DNI: {pacienteSel.dni})</span>
                <button type="button" className="link" onClick={() => {
                  setPacienteSel(null)
                  setDniPaciente('')
                }}>Cambiar paciente</button>
              </div>
          )}
        </div>

        <h3 style={{ marginTop: 20, marginBottom: 8 }}>Medicamentos</h3>
        {items.length > 0 && (
          <ul className="items-receta-lista">
            {items.map((it, idx) => (
              <li key={idx} className="item-receta">
                <div>
                  <strong>{it.medicamentoNombre}</strong>
                  {it.medicamentoPresentacion && <span> — {it.medicamentoPresentacion}</span>}
                  <div className="item-detalle">
                    <strong>Dosis:</strong> {it.dosis}
                    {it.duracion && <> · <strong>Duración:</strong> {it.duracion}</>}
                  </div>
                  {it.indicaciones && <div className="item-detalle">{it.indicaciones}</div>}
                </div>
                <button type="button" className="link danger" onClick={() => quitarItem(idx)}>Quitar</button>
              </li>
            ))}
          </ul>
        )}

        <div className="agregar-item">
          <select value={medSel} onChange={(e) => setMedSel(e.target.value)}>
            <option value="">-- Elegir medicamento --</option>
            {medicamentos.map((m) => (
              <option key={m.id} value={m.id}>
                {m.nombre}{m.presentacion ? ` (${m.presentacion})` : ''}
              </option>
            ))}
          </select>
          <input placeholder="Dosis (ej. 1 comprimido cada 8hs)" value={dosis} onChange={(e) => setDosis(e.target.value)} />
          <input placeholder="Duración (ej. 7 días)" value={duracion} onChange={(e) => setDuracion(e.target.value)} />
          <input placeholder="Indicaciones extra (opcional)" value={indicacionesItem} onChange={(e) => setIndicacionesItem(e.target.value)} />
          <button type="button" onClick={agregarItem}>+ Agregar a la receta</button>
        </div>

        <h3 style={{ marginTop: 20, marginBottom: 8 }}>Indicaciones generales (opcional)</h3>
        <textarea
          placeholder="Ej. Reposo, dieta blanda, hidratación..."
          value={indicacionesGenerales}
          onChange={(e) => setIndicacionesGenerales(e.target.value)}
          rows={3}
        />

        <div className="actions" style={{ marginTop: 20 }}>
          <button type="submit">Emitir receta</button>
          <button type="button" className="link" onClick={onCancel}>Cancelar</button>
        </div>
      </form>

      {error && <p className="error">{error}</p>}
      {ok && <p className="ok">{ok}</p>}
    </div>
  )
}
