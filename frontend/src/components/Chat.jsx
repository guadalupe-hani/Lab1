import { useState, useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import { api } from '../api'

export default function Chat({ user }) {
    const [contactos, setContactos] = useState([])
    const [contactoSel, setContactoSel] = useState(null)
    const [mensajes, setMensajes] = useState([])
    const [texto, setTexto] = useState('')
    const [conectado, setConectado] = useState(false)
    const [noLeidosPor, setNoLeidosPor] = useState({})
    const clientRef = useRef(null)
    const listRef = useRef(null)

    // Conectar WebSocket
    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            reconnectDelay: 5000,
            onConnect: () => {
                setConectado(true)
                client.subscribe(`/user/${user.id}/queue/mensajes`, (frame) => {
                    const mensaje = JSON.parse(frame.body)
                    setMensajes((prev) => {
                        if (prev.some((m) => m.id === mensaje.id)) return prev
                        return [...prev, mensaje]
                    })
                    // Si el mensaje es de otro (no del contacto abierto), sumar al badge
                    setContactoSel((sel) => {
                        if (mensaje.emisorId !== user.id && mensaje.emisorId !== sel?.id) {
                            setNoLeidosPor((prev) => ({
                                ...prev,
                                [mensaje.emisorId]: (prev[mensaje.emisorId] ?? 0) + 1,
                            }))
                        }
                        return sel
                    })
                })
            },
            onDisconnect: () => setConectado(false),
        })
        client.activate()
        clientRef.current = client
        return () => { client.deactivate() }
    }, [user.id])

    // Cargar contactos y no leídos por emisor
    useEffect(() => {
        api.chatContactos().then(setContactos).catch(() => {})
        api.chatNoLeidosPorEmisor()
            .then((data) => setNoLeidosPor(data))
            .catch(() => {})
    }, [])

    // Cargar historial al seleccionar contacto y limpiar badge
    useEffect(() => {
        if (!contactoSel) return
        api.chatConversacion(contactoSel.id)
            .then(setMensajes)
            .catch(() => {})
        // Limpiar badge del contacto seleccionado
        setNoLeidosPor((prev) => ({ ...prev, [contactoSel.id]: 0 }))
    }, [contactoSel])

    // Scroll automático
    useEffect(() => {
        if (listRef.current) {
            listRef.current.scrollTop = listRef.current.scrollHeight
        }
    }, [mensajes])

    const enviar = () => {
        if (!texto.trim() || !contactoSel || !conectado) return
        clientRef.current.publish({
            destination: '/app/chat/enviar',
            body: JSON.stringify({ receptorId: contactoSel.id, contenido: texto.trim() }),
        })
        setTexto('')
    }

    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault()
            enviar()
        }
    }

    return (
        <div className="chat-layout">

            {/* Panel izquierdo: contactos */}
            <div className="chat-sidebar">
                <div className="chat-sidebar-title">Conversaciones</div>
                {contactos.length === 0 && (
                    <p className="chat-empty-contactos">
                        Aún no tenés turnos con ningún {user.rol === 'PACIENTE' ? 'médico' : 'paciente'}.
                    </p>
                )}
                {contactos.map((c) => {
                    const pendientes = noLeidosPor[c.id] ?? noLeidosPor[String(c.id)] ?? 0
                    return (
                        <button
                            key={c.id}
                            className={'chat-contacto' + (contactoSel?.id === c.id ? ' activo' : '')}
                            onClick={() => setContactoSel(c)}
                        >
                            <div className="chat-contacto-nombre">{c.nombre}</div>
                            {pendientes > 0 && (
                                <span className="chat-contacto-badge">{pendientes > 99 ? '99+' : pendientes}</span>
                            )}
                        </button>
                    )
                })}
            </div>

            {/* Panel derecho: conversación */}
            <div className="chat-panel">
                {!contactoSel ? (
                    <div className="chat-placeholder">
                        Seleccioná una conversación para comenzar.
                    </div>
                ) : (
                    <>
                        <div className="chat-panel-header">
                            <span>{contactoSel.nombre}</span>
                            <span className={'chat-estado' + (conectado ? ' online' : '')}>
                {conectado ? 'Conectado' : 'Reconectando...'}
              </span>
                        </div>

                        <div className="chat-mensajes" ref={listRef}>
                            {mensajes
                                .filter((m) =>
                                    (m.emisorId === user.id && m.receptorId === contactoSel.id) ||
                                    (m.emisorId === contactoSel.id && m.receptorId === user.id)
                                )
                                .map((m) => (
                                    <div
                                        key={m.id}
                                        className={'chat-burbuja' + (m.emisorId === user.id ? ' propio' : ' ajeno')}
                                    >
                                        {m.turnoInfo && <div className="chat-referencia">📅 {m.turnoInfo}</div>}
                                        {m.recetaInfo && <div className="chat-referencia">📋 {m.recetaInfo}</div>}
                                        <div className="chat-texto">{m.contenido}</div>
                                        <div className="chat-hora">
                                            {new Date(m.fechaEnvio).toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' })}
                                        </div>
                                    </div>
                                ))}
                        </div>

                        <div className="chat-input-area">
              <textarea
                  className="chat-input"
                  placeholder="Escribí un mensaje... (Enter para enviar)"
                  value={texto}
                  onChange={(e) => setTexto(e.target.value)}
                  onKeyDown={handleKeyDown}
                  rows={2}
              />
                            <button className="chat-send-btn" onClick={enviar} disabled={!texto.trim() || !conectado}>
                                Enviar
                            </button>
                        </div>
                    </>
                )}
            </div>
        </div>
    )
}