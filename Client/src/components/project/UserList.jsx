import { useState } from 'react';
import styles from './ProjectStatistics.module.css'
import useAuthStore from '../../state/useAuthStore';

const UserList = ({ userList, projectInfo, onAdd, onRemove, onLeave, onChangeOwner, isAdding, setIsAdding }) => {
    const [ newUserEmail, setNewUserEmail ] = useState("")
    const [ errorMessage, setErrorMessage ] = useState(null)
    const { user } = useAuthStore();
    const handleSubmit = (e) => {
        e.preventDefault();
        setErrorMessage(onAdd(newUserEmail));
    };
    return (
        <div className={styles.statistics_item_body}>
            <div style={{display: "flex", flexDirection: "column", gap: "10px", width: "60%"}}>
                {userList && userList.map((member) => (
                    <div className={styles.assignee_card} key={member.id}>
                        { projectInfo && (member.id === projectInfo.owner_id ?
                            <span className={styles.owner_crown}>♛</span> 
                            : user.id === projectInfo.owner_id ? 
                            <button className={styles.make_owner_button}
                                onClick={() => onChangeOwner(member)}></button>
                            : <span className={styles.owner_crown}></span> )
                        }
                        <span className={styles.card_title} style={{display:"block"}}>
                            {member.email}
                        </span>
                        { projectInfo && user.id === projectInfo.owner_id && user.id != member.id ?
                        <button 
                            type="button"
                            className={styles.assignee_card_button}
                            onClick={() => onRemove(member)}
                            >&times;</button>
                            : <span></span> 
                        }
                    </div>
                ))}
                {projectInfo && user.id === projectInfo.owner_id && isAdding ? (
                    <div style={{display:"flex", flexDirection: "column", alignItems: "center", gap:"10px"}}>
                    <form onSubmit={handleSubmit} className={styles.add_form}>
                        <button type="button"
                        className={styles.add_button} onClick={() => setIsAdding(false)}>{"<"}</button>
                        <input className={styles.form_input}
                            style={{width: "100px"}} 
                            id="new_member_email"
                            placeholder='New member email'
                            onChange={(e)=>setNewUserEmail(e.target.value)}
                            value={newUserEmail}
                            />
                        <button className={styles.add_button} type='submit'>+</button>
                    </form>
                    {errorMessage && <label htmlFor='new_member_email'>{errorMessage}</label>}
                    </div>
                    ) : (
                    <>
                        { projectInfo && user.id === projectInfo.owner_id &&
                            <button type="button"
                                className={styles.add_button} onClick={() => setIsAdding(true)}>+</button>
                        }
                    </>
                )}
            </div>
            <button onClick={onLeave}
                className={`styled_button ${styles.leave_button}`}>Leave</button>
        </div>
    );
}

export default UserList;