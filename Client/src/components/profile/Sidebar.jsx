import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import styles from './Sidebar.module.css';
import Modal from "../common/Modal";
import NewProjectForm from "./NewProjectForm";
import { getProjectsByUserToken, createProject } from "../../services/api";
import useAuthStore from "../../state/useAuthStore";
import useProjectsStore from "../../state/useProjectsStore";

const Sidebar = ({ disabledProjectId }) => {
    const { user, logout } = useAuthStore();
    const { projects, setProjects } = useProjectsStore();
    const navigate = useNavigate();
    const [ newProjectOpen, setNewProjectOpen ] = useState(false);

    useEffect(()=>{
        const getProjects = async () => {
            try {
                setProjects(await getProjectsByUserToken(user.token));
            }
            catch (e) {
                console.error(e.message)
                if (e.response?.status === 401) {
                    logout();
                    navigate('/login');   
                }
                else if (e.response?.status === 403) {
                    navigate('/profile');
                }
            }
        }
        if (projects == null)
        {
            getProjects();
        }
    }, []);

    const handleCreateProject = async(name, description, date) => {
        try {
            const newProject = await createProject(user.token, name, description, date);
            setProjects([...projects, newProject]);
            setNewProjectOpen(false);
        }
        catch (e) {
            return "Server error!";
        }
        return null;
    }

    return (
        <>
            <Modal open={newProjectOpen}
                onClose={() => setNewProjectOpen(false)}
                children={<NewProjectForm onCreateProject={handleCreateProject} />}
                />
            <div className={styles.side_bar}>
                <div className={styles.side_bar_logo_wrapper}>
                    <Link to="/">
                        <img className={styles.side_bar_logo} src="/assets/logo.png"/>
                    </Link>
                </div>
                <div className={styles.project_list_wrapper}>
                    {projects && projects.map((project) => 
                        <button 
                            key={project.id}
                            onClick={() => navigate(`/project/${project.id}`)}
                            className={`styled_button ${disabledProjectId == project.id 
                                                        ? styles.project_button_disabled
                                                        : styles.project_button}`}>
                            {project.name}
                        </button>)}
                    {projects && projects.length === 0 && 
                                <p style={{color: "rgb(65, 102, 91)", textAlign: "center", fontWeight: "bold"}}>You don't have any<br /> projects yet...</p>}
                </div>
                <div className={styles.side_bar_footer}>
                    <button 
                        onClick={() => setNewProjectOpen(true)}
                        className='styled_button'> New project</button>
                </div>
            </div>
        </>
    );
};

export default Sidebar;
