import { useState, useEffect } from 'react'
import { api } from '../api'
import NotificacionesPanel from './NotificacionesPanel'

function BrandLogo() {
    return (
        <div className="brand-logo">
            <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 21s-7-4.35-7-10a4 4 0 0 1 7-2.65A4 4 0 0 1 19 11c0 5.65-7 10-7 10z" />
            </svg>
        </div>
    )
}

function ChatIconBtn({ onNavigate, page }) {
    const [noLeidos, setNoLeidos] = useState(0)

    useEffect(() => {
        const fetch = () => api.chatNoLeidos().then(d => setNoLeidos(d.count ?? 0)).catch(() => {})
        fetch()
        const interval = setInterval(fetch, 30000)
        return () => clearInterval(interval)
    }, [])

    return (
        <button
            className={'notif-btn' + (page === 'chat' ? ' active' : '')}
            onClick={() => onNavigate('chat')}
            title="Chat"
        >
            <div className="notif-bell-wrap">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
                     strokeLinecap="round" strokeLinejoin="round" className="notif-bell-icon">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                </svg>
                {noLeidos > 0 && (
                    <span className="notif-badge">{noLeidos > 99 ? '99+' : noLeidos}</span>
                )}
            </div>
        </button>
    )
}

export default function Navbar({ user, page, onNavigate, onLogout }) {
    const handleLogout = async () => {
        await api.logout()
        onLogout()
    }

    const linkClass = (p) => 'nav-link' + (page === p ? ' active' : '')

    return (
        <nav className="navbar">
            <div className="nav-brand">
                <BrandLogo />
                On-Time Health
            </div>
            <div className="nav-links">
                <button className={linkClass('inicio')} onClick={() => onNavigate('inicio')}>Inicio</button>
                <button className={linkClass('turnos')} onClick={() => onNavigate('turnos')}>Turnos</button>
                <button className={linkClass('recetas')} onClick={() => onNavigate('recetas')}>Recetas</button>
                {(user.rol === 'MEDICO' || user.rol === 'ADMINISTRATIVO') && (
                    <button className={linkClass('agenda')} onClick={() => onNavigate('agenda')}>
                        {user.rol === 'ADMINISTRATIVO' ? 'Agenda' : 'Mi agenda'}
                    </button>
                )}
                {user.rol === 'ADMINISTRATIVO' && (
                    <button className={linkClass('medicamentos')} onClick={() => onNavigate('medicamentos')}>Medicamentos</button>
                )}
            </div>
            <div className="nav-user">
                {(user.rol === 'PACIENTE' || user.rol === 'MEDICO') && (
                    <ChatIconBtn onNavigate={onNavigate} page={page} />
                )}
                <NotificacionesPanel />
                <button
                    className={'user-link' + (page === 'perfil' ? ' active' : '')}
                    onClick={() => onNavigate('perfil')}
                >
                    {user.nombre}
                </button>
                <button className="link" onClick={handleLogout}>Cerrar sesión</button>
            </div>
        </nav>
    )
}