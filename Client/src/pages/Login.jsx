import styles from './Auth.module.css'
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from "react-hook-form"
import useAuthStore from '../state/useAuthStore';
import { loginUser } from '../services/api'

const Login = () => {
    const { register, handleSubmit, formState: {errors, isSubmitting, isSubmitSuccessful}, setError } = useForm();
    const navigate = useNavigate();
    const { login } = useAuthStore();

    const handleLogin = async (data) => {
        try {
            const userData = await loginUser(data.login, data.password);
            login(userData);
            navigate(`/profile`);
            } catch (error) {
                switch (error.response?.status) {
                    case 404: 
                        setError("root", {
                            message: 'Incorrect login or password!'
                        });
                        break;
    
                    case 500:
                        setError("root", {
                            message: 'Server error occurred!'
                        });
                        break;
    
                    default:
                        setError("root", {
                            message: 'There was an error logging in!'
                        });
                }
            }
    };
    return (
    <div className={styles.page_wrapper}>
        <div className={styles.navbar}>
            <Link to="/">
                <img className="logo" src="/assets/logo.png"/>
            </Link>
            <Link to="/register">
                <button className='styled_button'>Register</button>
            </Link>
        </div>
        <div className={styles.login_form_wrapper}>
            <h1>Login</h1>
            <form onSubmit={handleSubmit(handleLogin)} className={styles.login_form}>
                <input className={styles.login_input} {...register("login", {
                    required: "Please enter the email!",
                })} placeholder="Email" type="text"/>
                {errors.login && (<label htmlFor='login' className={styles.error_message}>
                    {errors.login.message}</label>)}

                <input className={styles.login_input} {...register("password", {
                    required: "Please enter the password!"
                })} placeholder="Enter the password" type="password" />
                {errors.password && (<label htmlFor='password' className={styles.error_message}>
                    {errors.password.message}</label>)}

                <button className='styled_button' style={{fontSize:"15px", 
                    padding:"10px 15px", 
                    backgroundColor:"#15ab65"}} type="submit" disabled={isSubmitting}>Sign in</button>
                {isSubmitting ? <img className='loading_animation' src="/assets/loading.gif"></img>
                    : isSubmitSuccessful ? 
                    <p className='response-message'>Login successful!</p> 
                    : <p className={styles.error_message}>{errors.root && (errors.root.message)}</p>}
                <h4 style={{margin: 0, marginBottom: "20px"}}>
                    <span>New to Predman? </span>
                    <span className={styles.inline_link} onClick={() => navigate("/register")}>Sign Up now.</span>
                </h4>
            </form>
        </div>
    </div>
    )
}

export default Login
