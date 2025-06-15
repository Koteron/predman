import styles from '../common/ModalForm.module.css'
import { useForm } from "react-hook-form"

const NewProjectForm = ({ onCreateProject }) => {
    const { register, handleSubmit, formState: {errors, isSubmitting, isSubmitSuccessful}, setError } = useForm();

    const handleFormSubmition = async (data) => {
        const result = await onCreateProject(data.name, data.description, data.date);
        if (result) {
            setError("root", { message: result });
        }
        return !result;
    }

    const today = new Date().toISOString().split('T')[0]; 
    return (
        <>
            <h2 style={{textAlign: "center"}}>Create new project</h2>
            <form onSubmit={handleSubmit(handleFormSubmition)} className={styles.modal_form}>
                <input className={styles.form_input} 
                                id="name"
                                {...register("name", {
                                    required: "Please enter the project name!",
                                })} placeholder="Name" type="text"/>
                                {errors.name && (<label htmlFor='name'>
                                    {errors.name.message}</label>)}
                <div>
                    <label htmlFor='date'><b>Due date: </b></label>
                    <input className={styles.form_input} 
                                    id="date"
                                    {...register("date", {
                                        required: "Please enter the expected due date!",
                                    })} type="date"
                                    min={today}/>
                </div>
                                {errors.date && (<label htmlFor='date'>
                                    {errors.date.message}</label>)}

                <textarea className={styles.form_input} 
                                id="description"
                                {...register("description", {
                                })} placeholder="Description" />
                
                <button className={`styled_button ${styles.submit_button}`}>Create</button>

                {isSubmitting ? <img className='loading_animation' src="/assets/loading.gif"></img>
                    : isSubmitSuccessful ? 
                    <p className='response-message'>Project created!</p> 
                    : errors.root &&
                    <p>{errors.root.message}</p>}
            </form>
        </>
    );
}

export default NewProjectForm;