import { api } from '../api'

function BrandLogo() {
  return (
    <div className="brand-logo">
      <svg viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 21s-7-4.35-7-10a4 4 0 0 1 7-2.65A4 4 0 0 1 19 11c0 5.65-7 10-7 10z" />
      </svg>
    </div>
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
          <button className={linkClass('agenda')} onClick={() => onNavigate('agenda')}>Mi agenda</button>
        )}
        {(user.rol === 'MEDICO' || user.rol === 'ADMINISTRATIVO') && (
          <button className={linkClass('medicamentos')} onClick={() => onNavigate('medicamentos')}>Medicamentos</button>
        )}
      </div>
      <div className="nav-user">
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
