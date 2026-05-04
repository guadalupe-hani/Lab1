import { useEffect, useState } from 'react'
import { api } from '../api'

const TELEFONO_REGEX = /^[+]?[\d\s\-().]{7,20}$/

function validate(form, rol) {
    const errors = {}

    if (!form.nombre.trim()) errors.nombre = 'El nombre es requerido'
    else if (form.nombre.trim().length < 2) errors.nombre = 'Mínimo 2 caracteres'
    else if (form.nombre.trim().length > 50) errors.nombre = 'Máximo 50 caracteres'

    if (!form.apellido.trim()) errors.apellido = 'El apellido es requerido'
    else if (form.apellido.trim().length < 2) errors.apellido = 'Mínimo 2 caracteres'
    else if (form.apellido.trim().length > 50) errors.apellido = 'Máximo 50 caracteres'

    if (!form.email.trim()) errors.email = 'El email es requerido'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) errors.email = 'Email inválido'

    if (form.password && form.password.length < 6) errors.password = 'Mínimo 6 caracteres'
    if (form.password && form.password.length > 100) errors.password = 'Máximo 100 caracteres'

    if (rol === 'PACIENTE' && form.telefono && !TELEFONO_REGEX.test(form.telefono.trim())) {
        errors.telefono = 'Teléfono inválido'
    }

    return errors
}

function ModalConfirmacion({ mensaje, onConfirmar, onCancelar }) {
    return (
        <div className="modal-overlay" onClick={onCancelar}>
            <div className="modal-box" onClick={(e) => e.stopPropagation()}>
                <p>{mensaje}</p>
                <div className="modal-actions">
                    <button className="btn-danger" onClick={onConfirmar}>Sí, eliminar</button>
                    <button className="link" onClick={onCancelar}>Cancelar</button>
                </div>
            </div>
        </div>
    )
}

export default function EditarPerfil({ user, onDone, onCancel, onLogout }) {
    const [form, setForm] = useState(null)
    const [error, setError] = useState('')
    const [ok, setOk] = useState('')
    const [fieldErrors, setFieldErrors] = useState({})
    const [mostrarModal, setMostrarModal] = useState(false)

    useEffect(() => {
        api.perfil()
            .then((data) => setForm({
                nombre: data.nombre || '',
                apellido: data.apellido || '',
                email: data.email || '',
                password: '',
                telefono: data.telefono || '',
                obraSocial: data.obraSocial || '',
                plan: data.plan || '',
                especialidadId: data.especialidadId ?? '',
                consultorioId: data.consultorioId ?? '',
            }))
            .catch((err) => setError(err.message))
    }, [])

    if (error && !form) return <div className="card"><p className="error">{error}</p></div>
    if (!form) return <div className="card"><p>Cargando...</p></div>

    const update = (k) => (e) => {
        setForm({ ...form, [k]: e.target.value })
        if (fieldErrors[k]) setFieldErrors({ ...fieldErrors, [k]: undefined })
    }

    const handleDelete = async () => {
        setMostrarModal(false)
        try {
            await api.eliminar()
            onLogout && onLogout()
        } catch (err) {
            setError(err.message)
        }
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')
        setOk('')

        const errors = validate(form, user.rol)
        if (Object.keys(errors).length > 0) {
            setFieldErrors(errors)
            return
        }
        setFieldErrors({})

        try {
            const body = {
                nombre: form.nombre,
                apellido: form.apellido,
                email: form.email,
                password: form.password || null,
            }
            if (user.rol === 'PACIENTE') {
                body.telefono = form.telefono
                body.obraSocial = form.obraSocial
                body.plan = form.plan
            } else if (user.rol === 'MEDICO') {
                body.especialidadId = form.especialidadId ? Number(form.especialidadId) : null
            } else if (user.rol === 'ADMINISTRATIVO') {
                body.consultorioId = form.consultorioId ? Number(form.consultorioId) : null
            }
            const updated = await api.editar(body)
            setOk('Perfil actualizado')
            setTimeout(() => onDone(updated), 800)
        } catch (err) {
            setError(err.message)
        }
    }

    const Field = ({ id, label, error: err, children }) => (
        <div className="field">
            <label htmlFor={id}>{label}</label>
            {children}
            {err && <span className="field-error">{err}</span>}
        </div>
    )

    return (
        <>
            {mostrarModal && (
                <ModalConfirmacion
                    mensaje="¿Seguro que querés eliminar tu cuenta? Esta acción es irreversible."
                    onConfirmar={handleDelete}
                    onCancelar={() => setMostrarModal(false)}
                />
            )}

            <div className="card">
                <h2>Editar perfil</h2>
                <form onSubmit={handleSubmit}>
                    <Field id="ep-nombre" label="Nombre" error={fieldErrors.nombre}>
                        <input id="ep-nombre" placeholder="Tu nombre" value={form.nombre} onChange={update('nombre')} />
                    </Field>
                    <Field id="ep-apellido" label="Apellido" error={fieldErrors.apellido}>
                        <input id="ep-apellido" placeholder="Tu apellido" value={form.apellido} onChange={update('apellido')} />
                    </Field>
                    <Field id="ep-email" label="Email" error={fieldErrors.email}>
                        <input id="ep-email" type="email" placeholder="ejemplo@correo.com" value={form.email} onChange={update('email')} />
                    </Field>
                    <Field id="ep-password" label="Nueva contraseña (dejar vacío para no cambiar)" error={fieldErrors.password}>
                        <input id="ep-password" type="password" placeholder="Mínimo 6 caracteres" value={form.password} onChange={update('password')} />
                    </Field>

                    {user.rol === 'PACIENTE' && (
                        <>
                            <Field id="ep-telefono" label="Teléfono (opcional)" error={fieldErrors.telefono}>
                                <input id="ep-telefono" placeholder="Ej: +54 11 1234-5678" value={form.telefono} onChange={update('telefono')} />
                            </Field>
                            <Field id="ep-obrasocial" label="Obra social (opcional)" error={fieldErrors.obraSocial}>
                                <input id="ep-obrasocial" placeholder="Nombre de la obra social" value={form.obraSocial} onChange={update('obraSocial')} />
                            </Field>
                            <Field id="ep-plan" label="Plan (opcional)" error={fieldErrors.plan}>
                                <input id="ep-plan" placeholder="Ej: 210, básico, etc." value={form.plan} onChange={update('plan')} />
                            </Field>
                        </>
                    )}
                    {user.rol === 'MEDICO' && (
                        <Field id="ep-especialidad" label="ID de especialidad" error={fieldErrors.especialidadId}>
                            <input id="ep-especialidad" placeholder="Ej: 1" value={form.especialidadId} onChange={update('especialidadId')} />
                        </Field>
                    )}
                    {user.rol === 'ADMINISTRATIVO' && (
                        <Field id="ep-consultorio" label="ID de consultorio" error={fieldErrors.consultorioId}>
                            <input id="ep-consultorio" placeholder="Ej: 1" value={form.consultorioId} onChange={update('consultorioId')} />
                        </Field>
                    )}

                    <div className="actions">
                        <button type="submit">Guardar</button>
                        <button type="button" className="link" onClick={onCancel}>Cancelar</button>
                    </div>
                </form>
                {error && <p className="error">{error}</p>}
                {ok && <p className="ok">{ok}</p>}

                <hr style={{ margin: '24px 0 16px', border: 0, borderTop: '1px solid var(--border-soft)' }} />
                <div style={{ textAlign: 'center' }}>
                    <button className="link danger" onClick={() => setMostrarModal(true)}>Eliminar cuenta</button>
                </div>
            </div>
        </>
    )
}