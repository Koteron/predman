import styles from '../common/ModalForm.module.css'
import { useForm } from "react-hook-form"

const AddTaskForm = ({ onCreateTask }) => {
    const { register, handleSubmit, formState: {errors, isSubmitting, isSubmitSuccessful}, setError } = useForm();

    const handleFormSubmition = async (data) => {
        const result = await onCreateTask(data.name, data.description, data.story_points);
        if (result)
        {
            setError("root", {  message: result  });
        }
        return !result;
    }
    
    return (
        <>
            <h2 style={{textAlign: "center"}}>Create new task</h2>
            <form onSubmit={handleSubmit(handleFormSubmition)} className={styles.modal_form}>
                <input className={styles.form_input} 
                                id="name"
                                {...register("name", {
                                    required: "Please enter the task name!",
                                })} placeholder="Name" type="text"/>
                                {errors.name && (<label htmlFor='name'>
                                    {errors.name.message}</label>)}
                <div>
                <label htmlFor='story_points'><b>Story points: </b></label>
                <input className={styles.form_input}
                        id="story_points"
                        style={{width: "100px"}} 
                        min={0.0}
                        {...register("story_points", {
                            min: 0,
                            required: "Please enter the amount of estimated story points!",
                        })} step={0.1} type="number"/>
                </div>
                {errors.story_points && (<label htmlFor='story_points'>
                    {errors.story_points.message}</label>)}

                <textarea className={styles.form_input} 
                                id="description"
                                {...register("description", {
                                })} placeholder="Description" />
                
                <button className={`styled_button ${styles.submit_button}`}>Create</button>

                {isSubmitting ? <img className='loading_animation' src="/assets/loading.gif"></img>
                    : isSubmitSuccessful ? 
                    <p className='response-message'>Task created!</p> 
                    : errors.root &&
                    <p>{errors.root.message}</p>}
            </form>
        </>
    );
}

export default AddTaskForm;