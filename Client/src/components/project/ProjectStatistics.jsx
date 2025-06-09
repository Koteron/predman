import EditProjectForm from './EditProjectForm';
import Prediction from './Prediction';
import TasksGraph from './TasksGraph';
import UserList from './UserList';
import Modal from '../common/Modal';
import Confirmation from '../profile/Confirmation';
import styles from './ProjectStatistics.module.css'
import { useEffect, useState } from 'react';
import { addProjectMember,
     changeProjectOwner,
      getFullProjectInfo,
       getProjectMembers,
        removeProjectMember,
         updateProject,
          getStatistics,
            deleteProject } from '../../services/api';
import useAuthStore from '../../state/useAuthStore';
import useProjectsStore from '../../state/useProjectsStore';
import { useNavigate } from 'react-router-dom';

const ProjectStatistics = ({ projectId }) => {
    const { projects, setProjects } = useProjectsStore();
    const [ members, setMembers ] = useState(null);
    const [ projectInfo, setProjectInfo ] = useState(null);
    const { user } = useAuthStore();
    const [ modalChildren, setModalChildren ] = useState(null);
    const [ openConfirm, setOpenConfirm ] = useState(false);
    const [ isAdding, setIsAdding] = useState(false);
    const { updateItem } = useProjectsStore();
    const [ statistics, setStatistics ] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const getMembers = async () => {
            const response = await getProjectMembers(user.token, projectId);
            setMembers(response);
        }
        const getInfo = async () => {
            const response = await getFullProjectInfo(user.token, projectId);
            setProjectInfo(response);
        }
        const getProjectStatistics = async () => {
            const response = await getStatistics(user.token, projectId);
            setStatistics(response);
        }
        try {
            getMembers();
            getInfo();
            getProjectStatistics();
        }
        catch (e) {
            if (e.response?.status === 403) {
                navigate('/profile');
            }
        }
    }, [projectId]);

    const handleRemoveMember = async(memberId) => {
        try {
            await removeProjectMember(user.token, memberId, projectId);
            setOpenConfirm(false);
        }
        catch (e) {
            console.error(e.message);
            return "There was an error removing the user!";
        }
        return null;
    }

    const handleAddMember = async (memberEmail) => {
        try {
            if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(memberEmail))
            {
                return "Incorrect email!";
            }
            const response = await addProjectMember(user.token, memberEmail, projectId);
            setMembers((prev) => [...prev, response]);
            setIsAdding(false);
        }
        catch (e) {
            if (e.response?.status === 404)
            {
                return "Cannot find a user with such email!"
            }
            return "There was an error adding the user!";
        }
        return null;
    }

    const handleChangeOwner = async (newOwnerEmail) => {
        try {
            const response = await changeProjectOwner(user.token, newOwnerEmail, projectId)
            setProjectInfo((prev) => ({
                ...prev,
                owner_id: response.owner_id,
            }));
            setOpenConfirm(false);
        }
        catch(e) {
            if (e.response?.status === 404)
            {
                return "Cannot find a user with such email!"
            }
            return "There was an error changing the owner!";
        }
    }

    const handleEditProject = async (changes) => {
        try {
            const response = await updateProject(user.token, projectId, changes);
            const updatedFields = {
                ...(changes.name && { name: changes.name }),
                ...(changes.description && { description: changes.description }),
            }
            if (Object.keys(updatedFields).length !== 0) {
                updateItem(response.id, updatedFields);
            }
            setProjectInfo(response);
        }
        catch(e) {
            console.error(e)
            return "Could not save changes!"
        }
        return null;
    }

    const handleDeleteProject = async () => {
        try {
            await deleteProject(user.token, projectId);
            setOpenConfirm(false);
        }
        catch(e) {
            return "There was an error deleting the project!";
        }
    }

    return (
        <div className={styles.statistics_body_wrapper}>
            <Modal open={ openConfirm }
                children={ modalChildren }
                onClose={()=>{setOpenConfirm(false)}}/>
            <div className={styles.statistics_body}>
                <div className={`${styles.edit_form} ${styles.statistics_item}`}>
                    <h2 className={styles.statistics_item_title}>Project characteristics</h2>
                    <EditProjectForm onSave={handleEditProject} projectInfo={projectInfo}/>
                </div>
                <div className={styles.statistics_item} style={{position: "relative"}}>
                    <h2 className={styles.statistics_item_title}>Project members</h2>
                    <UserList userList={members} 
                        projectInfo={projectInfo}
                        onChangeOwner={(member)=>{
                            setOpenConfirm(true);
                            setModalChildren(<Confirmation question={<>Are you sure you want to hand project<br />
                             ownership over to {member.email}?</>}
                                    onConfirm={() => handleChangeOwner(member.email)}
                                    onDeny={() => setOpenConfirm(false)} />)
                        }}
                        onAdd={(email) => handleAddMember(email)}
                        onRemove={(member) => {
                            setOpenConfirm(true);
                            setModalChildren(<Confirmation question={<>Are you sure you want to remove<br />
                             {member.email} from project?</>}
                                    onConfirm={() => {
                                        handleRemoveMember(member.id);
                                        setMembers((prev) => prev.filter((projectMember) => projectMember.id !== member.id));
                                    }}
                                    onDeny={() => setOpenConfirm(false)} />)
                        }}
                        isAdding={isAdding}
                        setIsAdding={setIsAdding}
                        onLeave={()=>{
                            setOpenConfirm(true);
                            setModalChildren(<Confirmation question={<>Are you sure you want to leave<br />
                             the project?</>}
                                    onConfirm={() => {
                                        handleRemoveMember(user.id);
                                        navigate('/profile');
                                        setProjects(projects.filter((project) => project.id !== projectId));
                                    }}
                                    onDeny={() => setOpenConfirm(false)} />)}}
                        />
                        {projectInfo && user.id === projectInfo.owner_id && 
                        <button 
                            onClick={()=>{
                            setOpenConfirm(true);
                            setModalChildren(<Confirmation question={<>Are you sure you want to delete<br />
                             the project?</>}
                                    onConfirm={() => {
                                        handleDeleteProject();
                                        navigate('/profile');
                                        setProjects(projects.filter((project) => project.id !== projectId));
                                    }}
                                    onDeny={() => setOpenConfirm(false)} />)}}
                            className={`styled_button ${styles.leave_button}`} 
                            style={{top:"auto",bottom:0}}>Delete</button>
                            }
                </div>
                <div className={styles.statistics_item}>
                    <h2 className={styles.statistics_item_title}>Current prediction</h2>
                    <Prediction projectInfo={projectInfo} />
                </div>
                <div className={`${styles.graph_container} ${styles.statistics_item}`}>
                    <h2 className={styles.statistics_item_title} style={{marginBottom: "10px"}}>Tasks per day graph</h2>
                    <TasksGraph taskDayData={statistics}/>
                </div>
            </div>
        </div>
    );
}

export default ProjectStatistics;