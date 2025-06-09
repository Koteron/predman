import styles from './Modal.module.css'

const Modal = ({ open, onClose, children }) => {
  if (!open) return null;

  return (
    <div className={styles.modal_overlay} onClick={onClose}>
      <div className={styles.modal_content} onClick={(e) => e.stopPropagation()}>
        <button 
          onClick={onClose}
          className={styles.modal_close_button}
          >&times;
        </button>
        {children}
      </div>
    </div>
  );
}

export default Modal;