import { api } from '../api'

const ICONS = {
  calendar: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2" /><path d="M16 2v4M8 2v4M3 10h18" /><path d="M12 14v4M10 16h4" />
    </svg>
  ),
  list: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01" />
    </svg>
  ),
  user: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" />
    </svg>
  ),
  agenda: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2" /><path d="M16 2v4M8 2v4M3 10h18" />
    </svg>
  ),
  receta: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><path d="M14 2v6h6M9 13h6M9 17h4" />
    </svg>
  ),
  pill: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M10.5 20.5a4.95 4.95 0 0 1-7-7l10-10a4.95 4.95 0 0 1 7 7z" /><path d="M8.5 8.5l7 7" />
    </svg>
  ),
}

function ActionCard({ icon, title, description, button, onClick }) {
  return (
    <div className="action-card">
      <div className="icon-wrap">{ICONS[icon]}</div>
      <h3>{title}</h3>
      <p>{description}</p>
      <button className="action-btn" onClick={onClick}>{button}</button>
    </div>
  )
}

export default function Home({ user, onLogout, onNavigate }) {
  const handleDelete = async () => {
    if (!confirm('¿Seguro que querés eliminar tu cuenta? Esta acción es irreversible.')) return
    await api.eliminar()
    onLogout()
  }

  const welcome = {
    PACIENTE: '¡Bienvenido/a, paciente!',
    MEDICO: '¡Bienvenido/a, doctor/a!',
    ADMINISTRATIVO: '¡Bienvenido/a!',
  }[user.rol] || '¡Bienvenido/a!'

  const cards = []

  if (user.rol === 'PACIENTE') {
    cards.push(
      { icon: 'calendar', title: 'Reservar turno', description: 'Buscá un profesional y agendá tu próxima consulta.', button: 'Reservar', to: 'turnos' },
      { icon: 'list', title: 'Mis turnos', description: 'Ver tus turnos próximos e históricos.', button: 'Ver turnos', to: 'turnos' },
      { icon: 'receta', title: 'Mis recetas', description: 'Consultá las recetas que te emitieron tus médicos.', button: 'Ver recetas', to: 'recetas' },
      { icon: 'user', title: 'Mi perfil', description: 'Actualizá tus datos personales.', button: 'Editar perfil', to: 'perfil' },
    )
  } else if (user.rol === 'MEDICO') {
    cards.push(
      { icon: 'list', title: 'Mis turnos', description: 'Consultá tus turnos próximos y pasados.', button: 'Ver turnos', to: 'turnos' },
      { icon: 'agenda', title: 'Mi agenda', description: 'Gestioná horarios de trabajo y días libres.', button: 'Gestionar', to: 'agenda' },
      { icon: 'receta', title: 'Recetas', description: 'Emití nuevas recetas a tus pacientes.', button: 'Ver recetas', to: 'recetas' },
      { icon: 'pill', title: 'Medicamentos', description: 'Administrá el catálogo de medicamentos.', button: 'Gestionar', to: 'medicamentos' },
      { icon: 'user', title: 'Mi perfil', description: 'Actualizá tus datos profesionales.', button: 'Editar perfil', onClick: () => setEditing(true) },
    )
  } else {
    cards.push(
      { icon: 'calendar', title: 'Cargar turno', description: 'Registrá un turno para un paciente del consultorio.', button: 'Cargar', to: 'turnos' },
      { icon: 'list', title: 'Turnos del consultorio', description: 'Ver todos los turnos asignados.', button: 'Ver turnos', to: 'turnos' },
      { icon: 'agenda', title: 'Agenda de médicos', description: 'Configurá horarios y días libres.', button: 'Gestionar', to: 'agenda' },
      { icon: 'pill', title: 'Medicamentos', description: 'Administrá el catálogo de medicamentos.', button: 'Gestionar', to: 'medicamentos' },
      { icon: 'user', title: 'Mi perfil', description: 'Actualizá tus datos personales.', button: 'Editar perfil', to: 'perfil' },
    )
  }

  return (
    <>
      <div className="card welcome-card">
        <h2>{welcome}</h2>
        <p className="welcome-subtitle">Gestioná tus turnos médicos de forma inteligente.</p>
      </div>

      <div className="home-grid">
        {cards.map((c, i) => (
          <ActionCard
            key={i}
            icon={c.icon}
            title={c.title}
            description={c.description}
            button={c.button}
            onClick={() => onNavigate && onNavigate(c.to)}
          />
        ))}
      </div>

      <div className="actions" style={{ justifyContent: 'center', marginTop: 32 }}>
        <button className="link danger" onClick={handleDelete}>Eliminar cuenta</button>
      </div>
    </>
  )
}
