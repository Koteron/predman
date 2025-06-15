import styles from '../common/ModalForm.module.css'
import { useForm } from "react-hook-form"
import { useEffect, useState } from 'react';
import { getTaskAssignees,
     getProjectMembers, 
     assignUserToTask, 
     removeAssignment, 
     getTaskDependencies, 
     addTaskDependency, 
     removeTaskDependency } from '../../services/api';
import useAuthStore from "../../state/useAuthStore";
import TaskInnerForm from './TaskInnerForm';

const EditTaskForm = ({ onSave, onDelete, closeForm, loadingDelete, editingTask, projectId, tasks }) => {
    if (editingTask == null) return null;

    const { register, handleSubmit, formState: {errors, isSubmitting, isSubmitSuccessful}, reset, setError } = useForm();
    const [ assignees, setAssignees ] = useState(null);
    const [ members, setMembers ] = useState(null);
    const [ dependencies, setDependencies ] = useState(null);
    const [ loadingAssigneeForm, setLoadingAssigneeForm ] = useState(false);
    const [ loadingDependencyForm, setLoadingDependencyForm ] = useState(false);
    
    const [ isSelectingAssignees, setIsSelectingAssignees ] = useState(false);
    const [ isSelectingDependencies, setIsSelectingDependencies ] = useState(false);
    const { user } = useAuthStore();

    useEffect(() => {
        if (editingTask) {
        reset({
            name: editingTask.name || "",
            description: editingTask.description || "",
            story_points: editingTask.story_points || 0
        });
        }
    }, [editingTask, reset]);

    useEffect(()=>{
        const getAssignees = async () => {
            try {
                const assigneeResponse = await getTaskAssignees(user.token, projectId, editingTask.id);
                setAssignees(assigneeResponse);
            }
            catch (e) {
                console.error(e.message)
            }
        }
        const getDependencies = async () => {
            try {
                const response = await getTaskDependencies(user.token, editingTask.id);
                const dependentTaskIds = response
                    .filter(pair => pair.task_id === editingTask.id)
                    .map(pair => pair.dependency_id);
                setDependencies([...tasks.planned, ...tasks.inprogress].filter(task => 
                    dependentTaskIds.includes(task.id)));
            }
            catch (e) {
                console.error(e.message);
            }
        }
        if (editingTask != null)
        {
            getAssignees();
            getDependencies();
        }
    }, [editingTask]);

    useEffect(()=>{
        const getMembers = async () => {
            try {
                setLoadingAssigneeForm(true);
                const response = await getProjectMembers(user.token, projectId);
                setMembers(response);
                setLoadingAssigneeForm(false);
            }
            catch (e) {
                console.error(e.message);
                setIsSelectingAssignees(false);
                setLoadingAssigneeForm(false);
            }
        }
        if (isSelectingAssignees)
        {
            getMembers();
        }
    }, [isSelectingAssignees])

    const handleFormSubmition = async (data) => {
        const changedFields = {
            ...(data.name != editingTask.name && { name: data.name }),
            ...(data.story_points != editingTask.story_points && { story_points: data.story_points }),
            ...(data.description != editingTask.description && { description: data.description }),
        };
        if (Object.keys(changedFields).length !== 0) {
            const result = await onSave(changedFields);
            if (result)
            {
                setError("root", {  message: result  });
                return;
            }
            closeForm();
            return !result;
        }
        else {
            closeForm();
        }
    }

    const autoResize = (e) => {
        e.target.style.height = "auto";
        e.target.style.height = e.target.scrollHeight + "px";
    };

    const handleAddAssignee = async (userId) => {
        try {
                setLoadingAssigneeForm(true);
                const response = await assignUserToTask(user.token, userId, projectId, editingTask.id);
                setAssignees((prev) => ([...prev, response]));

                setLoadingAssigneeForm(false);
            }
            catch (e) {
                console.error(e.message);
                setIsSelectingAssignees(false);
                setLoadingAssigneeForm(false);
            }
    }

    const handleRemoveAssignee = async (userId) => {
        try {
                setLoadingAssigneeForm(true);
                await removeAssignment(user.token, userId, projectId, editingTask.id);
                setAssignees((prev) => (prev.filter(member => member.id !== userId)));
                setLoadingAssigneeForm(false);
            }
            catch (e) {
                console.error(e.message);
                setIsSelectingAssignees(false);
                setLoadingAssigneeForm(false);
            }
    }

    const handleAddDependency = async (taskId) => {
        try {
                setLoadingDependencyForm(true);
                const response = await addTaskDependency(user.token, editingTask.id, taskId);
                setDependencies((prev) => ([...prev, 
                    [...tasks.planned, ...tasks.inprogress].filter((task)=>(task.id == response.dependency_id))[0]]));
                setLoadingDependencyForm(false);
            }
            catch (e) {
                console.error(e.message);
                setIsSelectingDependencies(false);
                setLoadingDependencyForm(false);
            }
    }

    const handleRemoveDependency = async (taskId) => {
        try {
                setLoadingDependencyForm(true);
                await removeTaskDependency(user.token, editingTask.id, taskId);
                setDependencies((prev) => (prev.filter(task => task.id !== taskId)));
                setLoadingDependencyForm(false);
            }
            catch (e) {
                setIsSelectingDependencies(false);
                setLoadingDependencyForm(false);
            }
    }

    const handleDelete = async () => {
        try {
            await onDelete();
        }
        catch (e) {
            console.error(e.message);
            setError("There was an error while deleting the task");
        }
    }

    return (
        <>
            <h2 style={{textAlign: "center"}}>Edit task</h2>
            <form onSubmit={handleSubmit(handleFormSubmition)} 
                className={styles.modal_form}>
                <div className={styles.outer_input_list_wrapper}>
                    <div className={styles.inner_input_list_wrapper} style={{justifyContent: "space-around"}}>
                        <div>
                        <label htmlFor='story_points'><b>Name: </b></label>
                        <input className={styles.form_input} 
                                        id="name"
                                        {...register("name", {
                                            required: "Please enter the task name!",
                                        })} placeholder="Name" type="text"
                                        />
                                        {errors.name && (<label htmlFor='name'>
                                            {errors.name.message}</label>)}
                        </div>
                        <div>
                        <label htmlFor='story_points'><b>Story points: </b></label>
                        <input className={styles.form_input}
                                id="story_points"
                                style={{width: "100px"}} 
                                {...register("story_points", {
                                    required: "Please enter the amount of estimated story points!",
                                })}
                                step={0.1}
                                type="number"/>
                        {errors.story_points && (<label htmlFor='story_points'>
                            {errors.story_points.message}</label>)}
                        </div>
                        <div style={{display: "flex"}}>
                            <label htmlFor='description' style={{marginRight: "5px"}}><b>Description: </b></label>
                            <textarea className={styles.form_input} 
                            id="description"
                            {...register("description")}
                            placeholder={editingTask.decription}
                            onInput={autoResize}
                            ref={(e) => {
                                register("description").ref(e);
                                if (e) autoResize({ target: e });
                            }} 
                            style={{ overflow: "hidden", resize: "none" }}
                            />
                        </div>
                    </div>
                    <div className={styles.inner_input_list_wrapper}>
                        <TaskInnerForm innerList={assignees}
                            listName={"Assignees"}
                            secondaryList={members}
                            onAdd={handleAddAssignee}
                            onRemove={handleRemoveAssignee}
                            setIsSelecting={setIsSelectingAssignees}
                            isSelecting={isSelectingAssignees}
                            loadingInnerForm={loadingAssigneeForm}
                            selectMessage={"Select users..."}
                            displayField={"email"}
                            />
                        <TaskInnerForm innerList={dependencies}
                            listName={"Dependencies"}
                            secondaryList={[...tasks.planned, ...tasks.inprogress].filter((task)=>task.id != editingTask.id)}
                            onAdd={handleAddDependency}
                            onRemove={handleRemoveDependency}
                            setIsSelecting={setIsSelectingDependencies}
                            isSelecting={isSelectingDependencies}
                            loadingInnerForm={loadingDependencyForm}
                            selectMessage={"Select tasks..."}
                            displayField={"name"}
                            />
                    </div>
                </div>
                <div className={styles.submit_button_container}>
                    <button className={`styled_button ${styles.submit_button}`}>Save</button>
                    <button
                        type='button'
                        onClick={handleDelete}
                        className={`styled_button ${styles.delete_button}`}>Delete</button>
                </div>
                {isSubmitting || loadingDelete ? <img className='loading_animation' src="/assets/loading.gif"></img>
                    : isSubmitSuccessful ? 
                    <p className='response-message'>Task saved!</p> 
                    : errors.root &&
                    <p>{errors.root.message}</p>}
            </form>
        </>
    );
}

export default EditTaskForm;