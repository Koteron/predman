import styles from '../common/ModalForm.module.css'

const TaskInnerForm = ({innerList, 
    listName, 
    selectMessage, 
    secondaryList, 
    onAdd, 
    onRemove, 
    setIsSelecting, 
    isSelecting, 
    loadingInnerForm,
    displayField}) => {
    return (
        <div className={styles.assignee_list_wrapper}>
            <b>{listName}:</b>
            {innerList && innerList.map((element) => 
                <div className={styles.assignee_card} key={element.id}>
                    <span className={styles.card_title} style={{display:"block"}}>
                        {element[displayField]}
                    </span>
                    <button 
                        type="button"
                        className={styles.assignee_card_button}
                        onClick={() => onRemove(element.id)}
                        >&times;</button>
                </div>
            )}
            {isSelecting ? loadingInnerForm 
                ? <div style={{display: 'flex', justifyContent: "center"}}>
                    <img className='loading_animation' src="/assets/loading.gif" />
                    </div> : (
                <select
                    autoFocus
                    onBlur={() => setIsSelecting(false)}
                    onChange={(e) => {
                    const elementId = e.target.value;
                    if (elementId) {
                        onAdd(elementId);
                    }
                    setIsSelecting(false);
                    }}
                >
                    <option value="">{selectMessage}</option>
                    {secondaryList && secondaryList
                    .filter((u) => !innerList.some((a) => a.id === u.id))
                    .map((element) => (
                        <option key={element.id} value={element.id}>
                        {element[displayField]}
                        </option>
                    ))}
                </select>
                ) : (
                <button type="button"
                    className={styles.add_button} onClick={() => setIsSelecting(true)}>+</button>
            )}
        </div>
    );
}

export default TaskInnerForm;