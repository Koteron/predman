import styles from './ProjectDashboard.module.css'
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import { useEffect, useState } from 'react';
import { getTasksByProjectId, updateTask, createTask, deleteTask } from '../../services/api';
import useAuthStore from '../../state/useAuthStore';
import Modal from '../common/Modal';
import AddTaskForm from './AddTaskForm';
import EditTaskForm from './EditTaskForm';

const ProjectDashboard = ({ projectId }) => {

    const statusMap = {
        planned: 'PLANNED',
        inprogress: 'IN_PROGRESS',
        completed: 'COMPLETED'
    };

    const [tasks, setTasks] = useState(null);
    const [addTaskOpen, setAddTaskOpen] = useState(false);
    const [ loadingDelete, setLoadingDelete ] = useState(false);
    const [editingTask, setEditingTask] = useState(null);
    const { user } = useAuthStore();
    
    useEffect(()=>{
        const getTasks = async () => {
            try {
                const taskList = await getTasksByProjectId(user.token, projectId);
                setTasks(taskList);
            }
            catch (e) {
                console.error(e.message);
            }
        }
        getTasks();
    }, [projectId])    

    const onDragEnd = async (result) => {
        const { source, destination } = result;
        if (!destination) return;

        const { droppableId: srcId, index: srcIndex } = source;
        const { droppableId: destId, index: destIndex } = destination;

        const previousTasks = structuredClone(tasks);

        const updatedList = Array.from(tasks[srcId]);
        const [moved] = updatedList.splice(srcIndex, 1);
        
        try {
            if (srcId === destId) {
                updatedList.splice(destIndex, 0, moved);

                setTasks((prev) => ({
                    ...prev,
                    [srcId]: updatedList
                }));

                await updateTask(user.token, moved.id, 
                    { 
                        isNextUpdated: true,
                        next: updatedList[destIndex + 1]?.id || null
                    });
            }
            else {
                const destList = Array.from(tasks[destId]);

                destList.splice(destIndex, 0, moved);

                setTasks((prev) => ({
                    ...prev,
                    [srcId]: updatedList,
                    [destId]: destList
                }));

                await updateTask(user.token, moved.id, 
                    { 
                        status: statusMap[destId], 
                        isNextUpdated: true,
                        next: destList[destIndex + 1]?.id || null
                    });
            }
        }
        catch(e)
        {
            setTasks(previousTasks)
            console.error(e);
        }
    };

    const handleAddTask = async (name, decription, storyPoints) => {
        try {
            const newTask = await createTask(user.token, projectId, name, decription, storyPoints);
            setTasks((prev) => ({
            ...prev,
            planned: [...prev.planned, newTask]
            }));
            setAddTaskOpen(false);
        }
        catch (e) {
            console.error(e.message);
            return "Server error!"
        }
        return null;
    }

    const handleEditTask = async ( changedfields ) => {
        try {
            await updateTask(user.token, editingTask.id, changedfields);
            const columnId = columns.find(col => col.json_status === editingTask.status).id;
            if (!columnId) throw new Error("Column not found for status");
            setTasks((prev) => ({
                ...prev,
                [columnId]: prev[columnId].map((task) =>
                     task.id === editingTask.id ? { ...task, ...changedfields } : task
                )
            }));
            setEditingTask(null);
        }
        catch (e) {
            console.error(e.message);
            return "Server error!"
        }
        return null;
    }

    const handleDeleteTask = async () => {
        try {
                setLoadingDelete(true);
                await deleteTask(user.token, editingTask.id, projectId);
                const columnId = columns.find(col => col.json_status === editingTask.status).id;
                if (!columnId) throw new Error("Column not found for status");
                setTasks((prev) => ({
                ...prev,
                [columnId]: prev[columnId].filter((task) => task.id != editingTask.id)
                }));
                setLoadingDelete(false);
                setEditingTask(null);
            }
            catch (e) {
                console.error(e.message);
                setLoadingDelete(false);
                throw Error("There was an error while deleting the task!")
            }
    }

    const columns = [
        { id: 'planned', title: 'To do', json_status: "PLANNED" },
        { id: 'inprogress', title: 'In progress', json_status: "IN_PROGRESS" },
        { id: 'completed', title: 'Done', json_status: "COMPLETED" }
    ];

    return (
        <>
            <Modal children={<AddTaskForm onCreateTask={handleAddTask}/>} 
                open={addTaskOpen}
                onClose={() => {setAddTaskOpen(false)}}/>
            <Modal children={<EditTaskForm
                    editingTask={editingTask}
                    projectId={projectId}
                    onSave={handleEditTask}
                    onDelete={handleDeleteTask}
                    loadingDelete={loadingDelete}
                    tasks={tasks}
                    closeForm={()=>setEditingTask(null)}
                    />} 
                open={editingTask != null}
                onClose={() => {setEditingTask(null)}}/>
            <DragDropContext onDragEnd={onDragEnd}>
                <div className={styles.column_wrapper}>
                    {columns.map((column) => (
                    <div className={styles.column} key={column.id}>
                        <h2 className={styles.column_title}>{column.title}</h2>
                        <Droppable droppableId={column.id}>
                        {(provided) => (
                            <div
                            ref={provided.innerRef}
                            {...provided.droppableProps}
                            className={styles.task_list}
                            >
                            {tasks && tasks[column.id].map((task, index) => (
                                <Draggable key={task.id} draggableId={task.id} index={index}>
                                {(provided) => (
                                    <div
                                    ref={provided.innerRef}
                                    {...provided.draggableProps}
                                    {...provided.dragHandleProps}
                                    className={styles.task}
                                    onClick={()=>{setEditingTask(task)}}
                                    >
                                        <h4 className={styles.task_title}>{task.name}</h4>
                                        <div className={styles.story_points_wrapper}>
                                            <span className={styles.task_story_points}>SP: {task.story_points}</span>
                                        </div>
                                    </div>
                                )}
                                </Draggable>
                            ))}
                            {provided.placeholder}
                            {column.id == "planned" && 
                                <div className={styles.add_button_wrapper}>
                                    <button onClick={() => {setAddTaskOpen(true)}} 
                                        className={styles.add_button}>+</button>
                                </div>}
                            </div>
                        )}
                        </Droppable>
                    </div>
                    ))}
                </div>
            </DragDropContext>
        </>
    );
}

export default ProjectDashboard;