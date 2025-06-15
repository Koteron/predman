import { useForm } from "react-hook-form"
import { useEffect, useState } from "react";
import styles from './ProjectStatistics.module.css'

const EditProjectForm = ({ projectInfo, onSave }) => {
    const { register, handleSubmit, formState: {errors, isSubmitting, isSubmitSuccessful}, setError, reset } = useForm();

    const handleFormSubmition = async (data) => {
        const changedFields = {
            ...(data.name !== projectInfo.name && { name: data.name }),
            ...(data.description !== projectInfo.description && { description: data.description }),
            ...(data.date !== projectInfo.due_date && { dueDate: data.date }),
            ...(data.hours !== projectInfo.available_hours && { availableHours: data.hours }),
            ...(data.exp !== projectInfo.sum_experience && { sumExperience: data.exp }),
            ...(data.ex_risk !== projectInfo.external_risk_probability && { externalRiskProbability: data.ex_risk }),
        };
        if (Object.keys(changedFields).length !== 0) {
            const result = await onSave(changedFields);
            if (result)
            {
                setError("root", {  message: result  });
                return;
            }
            return !result;
        }
    }

    useEffect(() => {
        if (projectInfo) {
            reset({
                name: projectInfo.name,
                date: projectInfo.due_date,
                hours: projectInfo.available_hours,
                exp: projectInfo.sum_experience,
                ex_risk: projectInfo.external_risk_probability,
                description: projectInfo.description
            });
        }
    }, [projectInfo, reset]);

    const autoResize = (e) => {
        e.target.style.height = "auto";
        e.target.style.height = e.target.scrollHeight + "px";
    };

    const today = new Date().toISOString().split('T')[0]; 
    return (
        <div className={styles.statistics_item_body}>
            <form onSubmit={handleSubmit(handleFormSubmition)} style={{width:"100%"}}>
                <div className={styles.input_container}> 
                <label htmlFor='name'><b>Project name: </b></label>
                <input className={styles.form_input} 
                                id="name"
                                {...register("name", {
                                    required: "Please enter the project name!",
                                })} type="text"/>
                                
                </div>
                <div style={{width: "100%", display: "flex", justifyContent: "center", marginBottom: "10px"}}>
                    {errors.name && (<label htmlFor='name'>
                        {errors.name.message}</label>)}
                </div>
                <div className={styles.input_container}>
                <label htmlFor='date'><b>Due date: </b></label>
                <input className={styles.form_input} 
                    id="date"
                    {...register("date", {
                        required: "Please enter the expected due date!",
                    })} type="date"
                    min={today}/>
                                                
                </div>

                <div style={{width: "100%", display: "flex", justifyContent: "center", marginBottom: "10px"}}>
                    {errors.date && (<label htmlFor='date'>
                        {errors.date.message}</label>)}
                </div>

                <div className={styles.input_container}>
                    <label htmlFor='hours'><b>Available hours: </b></label>
                    <input className={styles.form_input}
                            id="hours"
                            style={{width: "100px"}} 
                            min={0.0}
                            {...register("hours", {
                                min: 0,
                                required: "Please enter the amount of available hours for your team!",
                                valueAsNumber: true
                            })} step={1.0} type="number"/>
                </div>

                <div style={{width: "100%", display: "flex", justifyContent: "center", marginBottom: "10px"}}>
                    {errors.hours && (<label htmlFor='hours'>
                        {errors.hours.message}</label>)}
                </div>

                <div className={styles.input_container}>
                    <label htmlFor='exp'><b>Sum experience: </b></label>
                    <input className={styles.form_input}
                            id="exp"
                            min={0.0}
                            {...register("exp", {
                                min: 0,
                                required: "Please enter the sum experience of your team!",
                                valueAsNumber: true
                            })} step={1.0} type="number"/>
                </div>

                <div style={{width: "100%", display: "flex", justifyContent: "center", marginBottom: "10px"}}>
                    {errors.exp && (<label htmlFor='exp'>
                        {errors.exp.message}</label>)}
                </div>

                <div className={styles.input_container}>
                    <label htmlFor='ex_risk'><b>External risk probability: </b></label>
                    <input className={styles.form_input}
                            id="ex_risk"
                            max={1.0}
                            min={0.0}
                            style={{width: "100px"}} 
                            {...register("ex_risk", {
                                min: 0.0,
                                max: 1.0,
                                required: "Please enter the external risk probability for your project!",
                                valueAsNumber: true
                            })} step={0.01} type="number"/>
                </div>

                <div style={{width: "100%", display: "flex", justifyContent: "center", marginBottom: "10px"}}>
                    {errors.ex_risk && (<label htmlFor='ex_risk'>
                        {errors.ex_risk.message}</label>)}
                </div>

                <div className={styles.input_container}>
                <label htmlFor='description'><b>Description: </b></label>
                <textarea className={styles.form_input} 
                    id="description"
                    {...register("description", {
                        })} placeholder="Description"
                        onInput={autoResize}
                    ref={(e) => {
                        register("description").ref(e);
                        if (e) autoResize({ target: e });
                    }} 
                    style={{ overflow: "hidden", resize: "none" }}
                />
                </div>
                
                <div style={{display:"flex", flexDirection:"column", alignItems:"center"}}>
                    <button className={`styled_button ${styles.submit_button}`}>Save</button>
                    {isSubmitting ? <img className='loading_animation' src="/assets/loading.gif"></img>
                    : isSubmitSuccessful ? 
                    <p className='response-message'>Project saved!</p> 
                    : errors.root &&
                    <p>{errors.root.message}</p>}
                </div>
            </form>
        </div>
    );
}

export default EditProjectForm;