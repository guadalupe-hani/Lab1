import { useState, useEffect } from 'react'
import Login from './components/Login'
import Registro from './components/Registro'
import Home from './components/Home'
import Navbar from './components/Navbar'
import Recetas from './components/Recetas'
import Agenda from './components/Agenda'
import Turnos from './components/Turnos'
import Medicamentos from './components/Medicamentos'
import EditarPerfil from './components/EditarPerfil'
import { api } from './api'
import './App.css'
import Chat from './components/Chat'
import FilaEnVivo from './components/FilaEnVivo'
import HistorialMedicoPage from './components/HistorialMedico'
import SalaEsperaPage from './components/SalaEspera'
import EstadisticasAdmin from './components/EstadisticasAdmin'

export default function App() {
  const [user, setUser] = useState(null)
  const [cargandoSesion, setCargandoSesion] = useState(true)
  const [view, setView] = useState('login')
    const [page, setPage] = useState(() => {
        const path = window.location.pathname.slice(1)
        const validas = ['inicio', 'recetas', 'agenda', 'turnos', 'medicamentos', 'perfil', 'chat', 'fila', 'historial', 'sala-espera', 'sala-tv', 'estadisticas']
        return validas.includes(path) ? path : 'inicio'
    })

    useEffect(() => {
        api.perfil()
            .then((datosUsuario) => {
                setUser(datosUsuario)
            })
            .catch(() => {
                setUser(null)
            })
            .finally(() => {
                setCargandoSesion(false)
            })
    }, []);

    useEffect(() => {
        if (user) {
            window.history.pushState({ paginaGuardada: page }, '', `/${page}`)
        }
    }, [page, user])

    useEffect(() => {
        const escucharBotonAtras = (evento) => {
            if (evento.state && evento.state.paginaGuardada) {
                setPage(evento.state.paginaGuardada)
            }
        }

        window.addEventListener('popstate', escucharBotonAtras)

        return () => window.removeEventListener('popstate', escucharBotonAtras)
    }, [])

  if(cargandoSesion){
      return (
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', color: '#2563eb', fontWeight: '600' }}>
              Cargando On-Time Health...
          </div>
      )
  }

    if (user && page === 'sala-tv') {
        return <SalaEsperaPage modoTv={true} />
    }

  if (user) return (
    <div className="app-logged">
      <Navbar user={user} page={page} onNavigate={setPage} onLogout={() => setUser(null)} />
      <div className="app-content">
        {page === 'inicio' && (
          <Home user={user} onLogout={() => setUser(null)} onUpdate={setUser} onNavigate={setPage} />
        )}
        {page === 'recetas' && (<Recetas user={user} />)}
        {page === 'agenda' && (<Agenda user={user} />)}
        {page === 'turnos' && (<Turnos user={user} />)}
        {page === 'medicamentos' && (<Medicamentos />)}
        {page === 'perfil' && (
          <EditarPerfil
            user={user}
            onDone={(updated) => { setUser(updated); setPage('inicio') }}
            onCancel={() => setPage('inicio')}
            onLogout={() => setUser(null)}
          />
        )}
        {page === 'chat' && (<Chat user={user} />)}
        {page === 'fila' && (<FilaEnVivo user={user} />)}
        {page === 'historial' && (<HistorialMedicoPage user={user} />)}
        {page === 'sala-espera' && <SalaEsperaPage />}
        {page === 'estadisticas' && <EstadisticasAdmin />}
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
        ? <Login onSuccess={(u) => { setUser(u); setPage('inicio') }} onSwitch={() => setView('registro')} />
        : <Registro onSwitch={() => setView('login')} />}
      <p className="tagline">Gestiona tus turnos médicos de forma inteligente</p>
    </div>
  )
}
