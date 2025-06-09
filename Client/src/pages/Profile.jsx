import styles from './Profile.module.css'
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../state/useAuthStore';
import Sidebar from '../components/profile/Sidebar';
import { useState } from 'react';
import Modal from '../components/common/Modal';
import { deleteUser } from "../services/api";
import Confirmation from '../components/profile/Confirmation';
import useProjectsStore from '../state/useProjectsStore';

const Profile = () => {
    const { user, logout } = useAuthStore();
    const navigate = useNavigate();
    const [ deleteOpen, setDeleteOpen ] = useState(false);
    const { clearProjects } = useProjectsStore();
   

    const handleLogout = () => {
        clearProjects();
        logout();
        navigate('/');
    }

    const handleDeleteAccount = async () => {
        try {
            await deleteUser(user.token);
            handleLogout();
        }
        catch (e) {
            console.error(e.message);
            return "There was an error deleting your account!";
        }
        return null;
    }

    return (
        <div className={styles.page_wrapper}>
            <Modal open={deleteOpen}
                onClose={() => setDeleteOpen(false)}
                children={<Confirmation question={<>Are you sure you want to delete<br /> your account?</>}
                                        onConfirm={handleDeleteAccount}
                                        onDeny={() => setDeleteOpen(false)} />}
                />
            <Sidebar />
            <div className={styles.profile_body_wrapper}>
                <div className={styles.profile_body}>
                    <div className={styles.profile_info_wrapper}>
                        <div>
                            <h1 style={{fontSize: "6vh"}}>Profile</h1>
                            <p className={styles.profile_label}><b>Login:</b> {user && user.login}</p>
                            <p className={styles.profile_label}><b>Email:</b> {user && user.email}</p>
                        </div>
                        <div>
                            <button onClick={handleLogout} className={`styled_button ${styles.logout_button}`}>Log out</button>
                        </div>
                    </div>
                    <div>
                        <button onClick={() => {setDeleteOpen(true)}} className={`styled_button ${styles.delete_button}`}>Delete account</button>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Profile;
