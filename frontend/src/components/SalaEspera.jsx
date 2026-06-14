import { useState, useEffect, useCallback } from 'react'
import { api } from '../api'

function estadoLabel(e) {
    return { ESPERANDO: 'Esperando', EN_ATENCION: 'En atención', ATENDIDO: 'Atendido', AUSENTE: 'Ausente' }[e] ?? e
}

function TvDisplay({ datos }) {
    const [hora, setHora] = useState(new Date())
    useEffect(() => {
        const t = setInterval(() => setHora(new Date()), 1000)
        return () => clearInterval(t)
    }, [])

    const enAtencion = datos?.enAtencion ?? []
    const esperando = datos?.esperando ?? []

    return (
        <div className="tv-screen">
            <div className="tv-top-bar">
                <span className="tv-brand">On-Time Health</span>
                {datos?.consultorio && <span className="tv-consultorio">{datos.consultorio}</span>}
                <span className="tv-reloj">
                    {hora.toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' })}
                </span>
            </div>

            <div className="tv-atencion-zona">
                {enAtencion.length > 0 ? enAtencion.map(p => (
                    <div key={p.turnoId} className="tv-atencion-card">
                        <div className="tv-atencion-etiqueta">EN ATENCIÓN AHORA</div>
                        <div className="tv-atencion-paciente">{p.pacienteNombre}</div>
                        <div className="tv-atencion-medico">con {p.medicoNombre}</div>
                    </div>
                )) : (
                    <div className="tv-atencion-card tv-atencion-vacia">
                        <div className="tv-atencion-etiqueta">PRÓXIMO TURNO</div>
                        <div className="tv-atencion-espera">Aguardando llamado...</div>
                    </div>
                )}
            </div>

            <div className="tv-lista-zona">
                <h2 className="tv-lista-titulo">PRÓXIMOS TURNOS</h2>
                {esperando.length === 0 ? (
                    <p className="tv-sin-espera">No hay más pacientes en espera</p>
                ) : (
                    <div className="tv-lista">
                        {esperando.map((p, i) => (
                            <div key={p.turnoId} className="tv-lista-item">
                                <span className="tv-lista-pos">{i + 1}</span>
                                <span className="tv-lista-nombre">{p.pacienteNombre}</span>
                                <span className="tv-lista-hora">{p.horaEstimada || p.hora}</span>
                                <span className="tv-lista-medico">{p.medicoNombre}</span>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    )
}

function AdminPanel({ datos, onRefresh, loading }) {
    const [acciones, setAcciones] = useState({})

    const cambiarEstado = async (turnoId, estado) => {
        setAcciones(a => ({ ...a, [turnoId]: true }))
        try {
            await api.marcarEstadoPaciente(turnoId, estado)
            await onRefresh()
        } catch (e) {
            alert(e.message)
        } finally {
            setAcciones(a => ({ ...a, [turnoId]: false }))
        }
    }

    const todos = datos?.todos ?? []

    return (
        <div className="sala-admin">
            <div className="sala-admin-header">
                <div>
                    <h1 className="sala-admin-titulo">Sala de espera</h1>
                    {datos?.consultorio && (
                        <span className="sala-admin-sub">{datos.consultorio} — {datos?.fecha}</span>
                    )}
                </div>
                <div className="sala-admin-acciones">
                    {loading && <span className="sala-actualizando">Actualizando...</span>}
                    <button className="btn-tv" onClick={() => window.open('/sala-tv', '_blank')}>
                        Pantalla TV
                    </button>
                </div>
            </div>

            {todos.length === 0 ? (
                <p className="sala-vacia">No hay turnos programados para hoy.</p>
            ) : (
                <div className="sala-tabla-wrap">
                    <table className="sala-tabla">
                        <thead>
                        <tr>
                            <th>Hora</th>
                            <th>Hora estimada</th>
                            <th>Paciente</th>
                            <th>Médico</th>
                            <th>Estado</th>
                            <th>Acciones</th>
                        </tr>
                        </thead>
                        <tbody>
                        {todos.map(p => {
                            const est = p.estadoPaciente || 'ESPERANDO'
                            return (
                                <tr key={p.turnoId} className={`sala-fila sala-fila-${est.toLowerCase()}`}>
                                    <td>{p.hora}</td>
                                    <td>{p.horaEstimada}</td>
                                    <td className="sala-nombre">{p.pacienteNombre}</td>
                                    <td>{p.medicoNombre}</td>
                                    <td>
                                            <span className={`sala-badge sala-badge-${est.toLowerCase()}`}>
                                                {estadoLabel(est)}
                                            </span>
                                    </td>
                                    <td>
                                        {est === 'ESPERANDO' && (
                                            <button className="sala-btn sala-btn-llamar"
                                                    disabled={acciones[p.turnoId]}
                                                    onClick={() => cambiarEstado(p.turnoId, 'EN_ATENCION')}>
                                                Llamar
                                            </button>
                                        )}
                                        {est === 'EN_ATENCION' && (<>
                                            <button className="sala-btn sala-btn-fin"
                                                    disabled={acciones[p.turnoId]}
                                                    onClick={() => cambiarEstado(p.turnoId, 'ATENDIDO')}>
                                                Finalizar
                                            </button>
                                            <button className="sala-btn sala-btn-ausente"
                                                    disabled={acciones[p.turnoId]}
                                                    onClick={() => cambiarEstado(p.turnoId, 'AUSENTE')}>
                                                Ausente
                                            </button>
                                        </>)}
                                        {(est === 'ATENDIDO' || est === 'AUSENTE') && (
                                            <button className="sala-btn sala-btn-reset"
                                                    disabled={acciones[p.turnoId]}
                                                    onClick={() => cambiarEstado(p.turnoId, 'ESPERANDO')}>
                                                Restablecer
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            )
                        })}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    )
}

export default function SalaEsperaPage({ modoTv = false }) {
    const [datos, setDatos] = useState(null)
    const [error, setError] = useState(null)
    const [loading, setLoading] = useState(false)

    const cargar = useCallback(async () => {
        setLoading(true)
        try {
            const d = await api.salaEspera()
            setDatos(d)
            setError(null)
        } catch (e) {
            setError(e.message)
        } finally {
            setLoading(false)
        }
    }, [])

    useEffect(() => {
        cargar()
        const interval = setInterval(cargar, modoTv ? 10000 : 30000)
        return () => clearInterval(interval)
    }, [cargar, modoTv])

    if (error) return <div className="sala-error">Error: {error}</div>
    if (!datos) return <div className="sala-cargando">Cargando sala de espera...</div>

    if (modoTv) return <TvDisplay datos={datos} />
    return <AdminPanel datos={datos} onRefresh={cargar} loading={loading} />
}