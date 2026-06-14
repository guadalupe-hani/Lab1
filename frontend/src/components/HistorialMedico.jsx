import { useState, useEffect, useRef } from 'react'
import { api } from '../api'

const PREVIEW = 3

function InfoPaciente({ paciente }) {
    return (
        <div className="historial-info-card">
            <h3>{paciente.nombre} {paciente.apellido}</h3>
            <div className="historial-info-grid">
                {paciente.dni && <span><strong>DNI:</strong> {paciente.dni}</span>}
                {paciente.obraSocial && <span><strong>Obra social:</strong> {paciente.obraSocial}</span>}
                {paciente.plan && <span><strong>Plan:</strong> {paciente.plan}</span>}
                {paciente.telefono && <span><strong>Teléfono:</strong> {paciente.telefono}</span>}
            </div>
        </div>
    )
}

function TurnoItem({ turno }) {
    const cancelado = turno.estado === 'CANCELADO'
    return (
        <div className={'historial-item' + (cancelado ? ' cancelado' : '')}>
            <div className="historial-item-fecha">{turno.fecha} — {turno.hora}hs</div>
            <div className="historial-item-detalle">{turno.medico}</div>
            {turno.consultorio && <div className="historial-item-sub">{turno.consultorio}</div>}
            <span className={'historial-estado' + (cancelado ? ' cancelado' : '')}>{turno.estado}</span>
        </div>
    )
}

function RecetaItem({ receta }) {
    const [abierta, setAbierta] = useState(false)
    return (
        <div className="historial-item">
            <div className="historial-item-fecha" style={{ cursor: 'pointer' }} onClick={() => setAbierta(v => !v)}>
                {receta.fecha} — {receta.medico} {abierta ? '▲' : '▼'}
            </div>
            {abierta && (
                <div className="historial-receta-detalle">
                    {receta.items?.length > 0 && (
                        <ul className="historial-receta-items">
                            {receta.items.map((it, i) => (
                                <li key={i}>
                                    <strong>{it.medicamento}</strong>
                                    {it.dosis && ` — ${it.dosis}`}
                                    {it.duracion && ` (${it.duracion})`}
                                    {it.indicaciones && <div className="historial-item-sub">{it.indicaciones}</div>}
                                </li>
                            ))}
                        </ul>
                    )}
                    {receta.contenido && (
                        <div className="historial-receta-contenido">{receta.contenido}</div>
                    )}
                </div>
            )}
        </div>
    )
}

function SeccionConPaginado({ titulo, items, renderItem, emptyMsg }) {
    const [verTodos, setVerTodos] = useState(false)
    const visibles = verTodos ? items : items.slice(0, PREVIEW)

    return (
        <div className="historial-seccion">
            <h4 className="historial-seccion-titulo">{titulo} ({items.length})</h4>
            {items.length === 0
                ? <p className="historial-empty">{emptyMsg}</p>
                : <>
                    {visibles.map((item, i) => renderItem(item, i))}
                    {items.length > PREVIEW && (
                        <button
                            className="historial-ver-todos"
                            onClick={() => setVerTodos(v => !v)}
                        >
                            {verTodos ? 'Ver menos ▲' : `Ver todos (${items.length}) ▼`}
                        </button>
                    )}
                </>
            }
        </div>
    )
}

function Historial({ data }) {
    return (
        <div className="historial-body">
            <InfoPaciente paciente={data.paciente} />
            <SeccionConPaginado
                titulo="Turnos"
                items={data.turnos}
                renderItem={(t) => <TurnoItem key={t.id} turno={t} />}
                emptyMsg="Sin turnos registrados."
            />
            <SeccionConPaginado
                titulo="Recetas"
                items={data.recetas}
                renderItem={(r) => <RecetaItem key={r.id} receta={r} />}
                emptyMsg="Sin recetas registradas."
            />
        </div>
    )
}

function HistorialPropio() {
    const [data, setData] = useState(null)
    const [error, setError] = useState('')

    useEffect(() => {
        api.miHistorial()
            .then(setData)
            .catch(() => setError('No se pudo cargar el historial.'))
    }, [])

    if (error) return <p className="error">{error}</p>
    if (!data) return <p className="historial-empty">Cargando...</p>
    return <Historial data={data} />
}

function HistorialMedico() {
    const [busqueda, setBusqueda] = useState('')
    const [sugerencias, setSugerencias] = useState([])
    const [pacienteSel, setPacienteSel] = useState(null)
    const [data, setData] = useState(null)
    const [error, setError] = useState('')
    const timeoutRef = useRef(null)

    const handleBusqueda = (valor) => {
        setBusqueda(valor)
        setPacienteSel(null)
        setData(null)
        clearTimeout(timeoutRef.current)
        if (valor.length < 2) { setSugerencias([]); return }
        timeoutRef.current = setTimeout(() => {
            api.buscarPacientes(valor)
                .then(setSugerencias)
                .catch(() => setSugerencias([]))
        }, 300)
    }

    const seleccionar = (p) => {
        setPacienteSel(p)
        setBusqueda(p.nombre)
        setSugerencias([])
        setError('')
        api.historialPaciente(p.id)
            .then(setData)
            .catch(() => setError('No se pudo cargar el historial.'))
    }

    return (
        <div>
            <div className="historial-buscador-wrap">
                <input
                    className="historial-buscador"
                    placeholder="Buscar paciente por nombre, apellido o DNI..."
                    value={busqueda}
                    onChange={e => handleBusqueda(e.target.value)}
                    autoComplete="off"
                />
                {sugerencias.length > 0 && (
                    <ul className="historial-sugerencias">
                        {sugerencias.map(p => (
                            <li key={p.id} onClick={() => seleccionar(p)}>
                                {p.nombre}
                                {p.dni && <span className="historial-sugerencia-dni"> — DNI {p.dni}</span>}
                            </li>
                        ))}
                    </ul>
                )}
            </div>

            {error && <p className="error">{error}</p>}
            {!pacienteSel && !error && (
                <p className="historial-empty">Buscá un paciente para ver su historial.</p>
            )}
            {data && <Historial data={data} />}
        </div>
    )
}

export default function HistorialMedicoPage({ user }) {
    return (
        <div className="card">
            <h2>{user.rol === 'PACIENTE' ? 'Mi historial médico' : 'Historial de pacientes'}</h2>
            {user.rol === 'PACIENTE' ? <HistorialPropio /> : <HistorialMedico />}
        </div>
    )
}