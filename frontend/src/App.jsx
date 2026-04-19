import { useState } from 'react'
import Login from './components/Login'
import Registro from './components/Registro'
import Home from './components/Home'
import Navbar from './components/Navbar'
import Recetas from './components/Recetas'
import Agenda from './components/Agenda'
import Turnos from './components/Turnos'
import Medicamentos from './components/Medicamentos'
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
          <Home user={user} onLogout={() => setUser(null)} onUpdate={setUser} />
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
      </div>
    </div>
  )

  return (
    <div className="app">
      <h1>OnTime Health</h1>
      {view === 'login'
        ? <Login onSuccess={setUser} onSwitch={() => setView('registro')} />
        : <Registro onSwitch={() => setView('login')} />}
    </div>
  )
}
