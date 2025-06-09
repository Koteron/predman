import styles from './Confirmation.module.css'
import { useState } from 'react';

const Confirmation = ({ question, onConfirm, onDeny }) => {
    const [error, setError] = useState(null);
    const handleConfirm = () => {
        setError(onConfirm());
    }
    return (
        <div style={{textAlign: "center"}}>
            <h2>{question}</h2>
            <div className={styles.confirmation_wrapper}>
                <button onClick={ handleConfirm } className={`styled_button ${styles.yes_button}`}>Yes</button>
                <button onClick={ onDeny } className={`styled_button ${styles.no_button}`}>No</button>                
            </div>
                {error && <p><b>{error}</b></p>}
        </div>
    );
}

export default Confirmation;