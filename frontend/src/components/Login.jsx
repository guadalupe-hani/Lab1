import { useState } from 'react'
import { api } from '../api'

function validate(email, password) {
    const errors = {}
    if (!email) errors.email = 'El email es requerido'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = 'Email inválido'

    if (!password) errors.password = 'La contraseña es requerida'
    else if (password.length < 6) errors.password = 'Mínimo 6 caracteres'

    return errors
}

export default function Login({ onSuccess, onSwitch }) {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [fieldErrors, setFieldErrors] = useState({})

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')

        const errors = validate(email, password)
        if (Object.keys(errors).length > 0) {
            setFieldErrors(errors)
            return
        }
        setFieldErrors({})

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
                <div className="field">
                    <label htmlFor="login-email">Email</label>
                    <input
                        id="login-email"
                        type="email"
                        placeholder="ejemplo@correo.com"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    {fieldErrors.email && <span className="field-error">{fieldErrors.email}</span>}
                </div>

                <div className="field">
                    <label htmlFor="login-password">Contraseña</label>
                    <input
                        id="login-password"
                        type="password"
                        placeholder="Mínimo 6 caracteres"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                    {fieldErrors.password && <span className="field-error">{fieldErrors.password}</span>}
                </div>

                <button type="submit">Ingresar</button>
            </form>
            {error && <p className="error">{error}</p>}
            <p className="switch">
                ¿No tenés cuenta? <button className="link" onClick={onSwitch}>Registrarse</button>
            </p>
        </div>
    )
}