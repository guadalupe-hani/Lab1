import { api } from '../api'

export default function Home({ user, onLogout }) {
  const handleLogout = async () => {
    await api.logout()
    onLogout()
  }

  const handleDelete = async () => {
    if (!confirm('¿Seguro que querés eliminar tu cuenta? Esta acción es irreversible.')) return
    await api.eliminar()
    onLogout()
  }

  return (
    <div className="card">
      <h2>Bienvenido/a a OnTime Health</h2>
      <p><strong>Nombre:</strong> {user.nombre} {user.apellido}</p>
      <p><strong>Email:</strong> {user.email}</p>
      <p><strong>Rol:</strong> {user.rol}</p>
      <div className="actions">
        <button onClick={handleLogout}>Cerrar sesión</button>
        <button className="danger" onClick={handleDelete}>Eliminar cuenta</button>
      </div>
    </div>
  )
}
