import { useState } from 'react'
import './ConfirmModal.css'

export default function PromptModal({
                                        open,
                                        title = 'Ingresar motivo',
                                        message,
                                        confirmText = 'Continuar',
                                        cancelText = 'Cancelar',
                                        onConfirm,
                                        onClose
                                    }) {
    const [valor, setValor] = useState('')

    if (!open) return null

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-box" onClick={(e) => e.stopPropagation()}>
                <h3>{title}</h3>
                <p>{message}</p>

                <textarea
                    style={{
                        width: '100%',
                        marginTop: '10px',
                        padding: '12px',
                        borderRadius: '8px',
                        border: '1px solid var(--border)',
                        fontFamily: 'inherit'
                    }}
                    rows={3}
                    placeholder="Escribí acá el motivo..."
                    value={valor}
                    onChange={(e) => setValor(e.target.value)}
                    autoFocus
                />

                <div className="modal-actions">
                    <button onClick={() => onConfirm(valor)}>
                        {confirmText}
                    </button>
                    <button className="link" onClick={onClose}>
                        {cancelText}
                    </button>
                </div>
            </div>
        </div>
    )
}