import './ConfirmModal.css'

export default function ConfirmModal({
                                         open,
                                         title = 'Confirmar acción',
                                         message,
                                         confirmText = 'Confirmar',
                                         cancelText = 'Cancelar',
                                         onConfirm,
                                         onClose,
                                         danger = false
                                     }) {

    if (!open) return null

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div
                className="modal-box"
                onClick={(e) => e.stopPropagation()}
            >

                <h3>{title}</h3>

                <p>{message}</p>

                <div className="modal-actions">

                    <button
                        className={danger ? 'btn-danger' : ''}
                        onClick={onConfirm}
                    >
                        {confirmText}
                    </button>

                    <button
                        className="link"
                        onClick={onClose}
                    >
                        {cancelText}
                    </button>

                </div>

            </div>
        </div>
    )
}