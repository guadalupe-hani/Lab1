import { useState } from 'react'
import { api } from '../api'

const PAGE_SIZE = 10

function Card({ label, valor, color }) {
    return (
        <div className={`est-card est-card-${color}`}>
            <div className="est-card-valor">{valor}</div>
            <div className="est-card-label">{label}</div>
        </div>
    )
}

function estadoTexto(t) {
    if (t.estado === 'CANCELADO') {
        const por = { MEDICO: 'por el médico', PACIENTE: 'por el paciente', ADMINISTRATIVO: 'por administrativo' }
        return `Cancelado ${por[t.canceladoPor] ?? ''}`
    }
    if (t.estadoPaciente === 'ATENDIDO') return 'Atendido'
    if (t.estadoPaciente === 'AUSENTE')  return 'Ausente'
    return 'Programado'
}

function estadoBadge(t) {
    if (t.estado === 'CANCELADO') return 'badge-cancelado'
    if (t.estadoPaciente === 'ATENDIDO') return 'badge-atendido'
    if (t.estadoPaciente === 'AUSENTE')  return 'badge-ausente'
    return 'badge-programado'
}

export default function EstadisticasAdmin() {
    const [busqueda, setBusqueda]     = useState('')
    const [sugerencias, setSugerencias] = useState([])
    const [stats, setStats]           = useState(null)
    const [loading, setLoading]       = useState(false)
    const [error, setError]           = useState(null)
    const [pagina, setPagina]         = useState(0)
    const [exportando, setExportando] = useState(false)

    const buscarMedico = async (valor) => {
        setBusqueda(valor)
        if (valor.length < 2) { setSugerencias([]); return }
        try {
            const res = await api.buscarProfesionales({ nombre: valor })
            setSugerencias(res)
        } catch { setSugerencias([]) }
    }

    const seleccionar = async (p) => {
        setBusqueda(p.nombre)
        setSugerencias([])
        setPagina(0)
        setLoading(true)
        setError(null)
        try {
            setStats(await api.estadisticasMedico(p.id))
        } catch (e) {
            setError(e.message)
            setStats(null)
        } finally {
            setLoading(false)
        }
    }

    const exportarPdf = async () => {
        if (!stats) return
        setExportando(true)
        try {
            const blob = await api.estadisticasMedicoPdf(stats.medico.id)
            const url  = URL.createObjectURL(blob)
            const a    = document.createElement('a')
            a.href     = url
            a.download = `estadisticas-${stats.medico.nombre.replace(/ /g, '-')}.pdf`
            a.click()
            URL.revokeObjectURL(url)
        } catch (e) {
            alert(e.message)
        } finally {
            setExportando(false)
        }
    }

    const turnos       = stats?.turnos ?? []
    const totalPaginas = Math.ceil(turnos.length / PAGE_SIZE)
    const pagActual    = turnos.slice(pagina * PAGE_SIZE, (pagina + 1) * PAGE_SIZE)

    return (
        <div className="est-container">
            <h1 className="est-titulo">Estadísticas por médico</h1>

            <div className="est-buscar-wrap">
                <input
                    className="est-buscar-input"
                    type="text"
                    placeholder="Buscar médico por nombre..."
                    value={busqueda}
                    onChange={(e) => buscarMedico(e.target.value)}
                />
                {sugerencias.length > 0 && (
                    <ul className="est-sugerencias">
                        {sugerencias.map(p => (
                            <li key={p.id} onClick={() => seleccionar(p)}>
                                <strong>{p.nombre}</strong>
                                {p.especialidad?.nombre && <span> — {p.especialidad.nombre}</span>}
                            </li>
                        ))}
                    </ul>
                )}
            </div>

            {loading && <p className="est-cargando">Cargando estadísticas...</p>}
            {error   && <p className="est-error">{error}</p>}

            {stats && (<>
                <div className="est-medico-header">
                    <div>
                        <h2 className="est-medico-nombre">Dr/a. {stats.medico.nombre}</h2>
                        <span className="est-medico-info">{stats.medico.especialidad} — Mat. {stats.medico.matricula}</span>
                    </div>
                    <button className="btn-exportar-pdf" onClick={exportarPdf} disabled={exportando}>
                        {exportando ? 'Generando...' : 'Exportar PDF'}
                    </button>
                </div>

                <div className="est-cards">
                    <Card label="Total turnos"           valor={stats.resumen.total}              color="total"    />
                    <Card label="Atendidos"              valor={stats.resumen.atendidos}           color="verde"    />
                    <Card label="Ausentes"               valor={stats.resumen.ausentes}            color="amarillo" />
                    <Card label="Programados"            valor={stats.resumen.programados}         color="azul"     />
                    <Card label="Cancelados por médico"  valor={stats.resumen.canceladosMedico}    color="rojo"     />
                    <Card label="Cancelados por paciente" valor={stats.resumen.canceladosPaciente} color="naranja"  />
                    <Card label="Cancelados por admin"   valor={stats.resumen.canceladosAdmin}     color="gris"     />
                </div>

                <div className="est-detalle">
                    <h3 className="est-detalle-titulo">Detalle de turnos ({turnos.length})</h3>
                    {turnos.length === 0 ? (
                        <p className="est-vacio">No hay turnos registrados.</p>
                    ) : (<>
                        <div className="est-tabla-wrap">
                            <table className="est-tabla">
                                <thead>
                                <tr><th>Fecha</th><th>Hora</th><th>Paciente</th><th>Estado</th></tr>
                                </thead>
                                <tbody>
                                {pagActual.map(t => (
                                    <tr key={t.id}>
                                        <td>{t.fecha}</td>
                                        <td>{t.hora?.slice(0, 5)}</td>
                                        <td>{t.pacienteNombre}</td>
                                        <td><span className={`est-badge ${estadoBadge(t)}`}>{estadoTexto(t)}</span></td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                        {totalPaginas > 1 && (
                            <div className="est-paginado">
                                <button className="est-pag-btn" disabled={pagina === 0}
                                        onClick={() => setPagina(p => p - 1)}>← Anterior</button>
                                <span className="est-pag-info">Página {pagina + 1} de {totalPaginas}</span>
                                <button className="est-pag-btn" disabled={pagina >= totalPaginas - 1}
                                        onClick={() => setPagina(p => p + 1)}>Siguiente →</button>
                            </div>
                        )}
                    </>)}
                </div>
            </>)}
        </div>
    )
}