import { useState } from 'react'
import Login from './components/Login'
import Registro from './components/Registro'
import Home from './components/Home'
import Navbar from './components/Navbar'
import Recetas from './components/Recetas'
import Agenda from './components/Agenda'
import Turnos from './components/Turnos'
import Medicamentos from './components/Medicamentos'
import EditarPerfil from './components/EditarPerfil'
import './App.css'

export default function App() {
  const [user, setUser] = useState(null)
  const [view, setView] = useState('login')
  const [page, setPage] = useState('inicio')

  if (user) return (
    <div className="app-logged">
      <Navbar user={user} page={page} onNavigate={setPage} onLogout={() => setUser(null)} />
      <div className="app-content">
        {page === 'inicio' && (
          <Home user={user} onLogout={() => setUser(null)} onUpdate={setUser} onNavigate={setPage} />
        )}
        {page === 'recetas' && (
          <Recetas user={user} />
        )}
        {page === 'agenda' && (
          <Agenda user={user} />
        )}
        {page === 'turnos' && (
          <Turnos user={user} />
        )}
        {page === 'medicamentos' && (
          <Medicamentos />
        )}
        {page === 'perfil' && (
          <EditarPerfil
            user={user}
            onDone={(updated) => { setUser(updated); setPage('inicio') }}
            onCancel={() => setPage('inicio')}
            onLogout={() => setUser(null)}
          />
        )}
      </div>
    </div>
  )

  return (
    <div className="app">
      <div className="app-header">
        <div className="brand">
          <div className="brand-logo">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 21s-7-4.35-7-10a4 4 0 0 1 7-2.65A4 4 0 0 1 19 11c0 5.65-7 10-7 10z" />
            </svg>
          </div>
          On-Time Health
        </div>
      </div>
      {view === 'login'
        ? <Login onSuccess={setUser} onSwitch={() => setView('registro')} />
        : <Registro onSwitch={() => setView('login')} />}
      <p className="tagline">Gestiona tus turnos médicos de forma inteligente</p>
    </div>
  )
}
