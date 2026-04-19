import { useState } from 'react'
import { api } from '../api'

export default function Login({ onSuccess, onSwitch }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const user = await api.login({ email, password })
      onSuccess(user)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="card">
      <h2>Iniciar sesión</h2>
      <form onSubmit={handleSubmit}>
        <input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <input type="password" placeholder="Contraseña" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <button type="submit">Ingresar</button>
      </form>
      {error && <p className="error">{error}</p>}
      <p className="switch">
        ¿No tenés cuenta? <button className="link" onClick={onSwitch}>Registrarse</button>
      </p>
    </div>
  )
}
