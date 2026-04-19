import { api } from '../api'

export default function Navbar({ user, page, onNavigate, onLogout }) {
  const handleLogout = async () => {
    await api.logout()
    onLogout()
  }

  const linkClass = (p) => 'nav-link' + (page === p ? ' active' : '')

  return (
    <nav className="navbar">
      <div className="nav-brand">OnTime Health</div>
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
        <span>{user.nombre} ({user.rol})</span>
        <button className="link" onClick={handleLogout}>Cerrar sesión</button>
      </div>
    </nav>
  )
}
