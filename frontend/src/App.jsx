import { useState } from 'react'
import Login from './components/Login'
import Registro from './components/Registro'
import Home from './components/Home'
import './App.css'

export default function App() {
  const [user, setUser] = useState(null)
  const [view, setView] = useState('login')

  if (user) return (
    <div className="app">
      <h1>OnTime Health</h1>
      <Home user={user} onLogout={() => setUser(null)} />
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
