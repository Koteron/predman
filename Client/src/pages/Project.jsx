import { useEffect, useState } from 'react';
import Sidebar from '../components/profile/Sidebar';
import styles from './Project.module.css'
import { Link, useParams } from 'react-router-dom';
import ProjectDashboard from '../components/project/ProjectDashboard';
import ProjectStatistics from '../components/project/ProjectStatistics';
import useProjectsStore from '../state/useProjectsStore';

const Project = () => {
    const { projectId } = useParams();
    const [ dashboardOpen, setDashboardOpen ] = useState(true);
    const { projects } = useProjectsStore();
    const [ project, setProject ] = useState();

    useEffect(()=>{
        if (projects != null)
        {
            setProject(projects.filter((project) => project.id == projectId)[0]);
        }
    }, [projects, projectId])

    return (
        <div className={styles.page_wrapper}>
            <Sidebar disabledProjectId={projectId} />
            <div className={styles.project_wrapper}>
                <div className={styles.project_header}>
                    <div className={styles.title_wrapper}>
                        <h1 style={{margin: 0}}>{project?.name}</h1>
                    </div>
                    <Link to='/profile'>
                        <button className={`styled_button ${styles.profile_button}`}>Profile</button>
                    </Link>
                </div>
                <div className={styles.project_tabs}>
                    <button className={`${styles.tab} ${dashboardOpen && styles.tab_active}`} 
                        style={{borderRight: "1px solid rgba(0, 0, 0, 0.2)"}}
                        onClick={() => setDashboardOpen(true)}>
                            Dashboard
                    </button>
                    <button className={`${styles.tab} ${!dashboardOpen && styles.tab_active}`}  
                        style={{borderLeft: "1px solid rgba(0, 0, 0, 0.2)"}}
                        onClick={() => setDashboardOpen(false)}>
                            Statistics
                    </button>
                </div>
                { dashboardOpen 
                    ? <ProjectDashboard projectId={projectId} /> 
                    : <ProjectStatistics projectId={projectId} />
                }
            </div>
        </div>
    );
}

export default Project;