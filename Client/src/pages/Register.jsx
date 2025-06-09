import styles from './Auth.module.css'
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from "react-hook-form"
import useAuthStore from '../state/useAuthStore';
import { registerUser } from '../services/api';

const Register = () => {
    const { register, handleSubmit, formState: {errors, isSubmitting, isSubmitSuccessful}, getValues,
    setError, clearErrors } = useForm();
    const navigate = useNavigate();
    const { login } = useAuthStore();

    const handleRegister = async (data) => {
            try {
                const userData = await registerUser(data.email, data.login, data.password);
                login(userData);
                navigate(`/profile`);
            } catch (error) {
                switch (error.response?.status) {
                    case 400:
                        setError("root", {
                            message: 'User with this email already exists!'
                        });
                        break;
    
                    case 500:
                        setError("root", {
                            message: 'Server error occurred!'
                        });
                        break;
    
                    default:
                        setError("root", {
                            message: 'There was an error creating an account!'
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
            <Link to="/login">
                <button className='styled_button'>Login</button>
            </Link>
        </div>
        <div className={styles.login_form_wrapper}>
            <h1>Register</h1>
            <form onSubmit={handleSubmit(handleRegister)} className={styles.login_form}>
                <input {...register("login", {
                    required: "Please enter the login!",
                    pattern: {
                        value: /^[A-Za-z]+[A-Za-z0-9_,.-]*$/,
                        message: "Login must start from a latin letter"
                    },
                    minLength: {
                        value: 6,
                        message: "Login must be from 6 to 32 symbols long",
                    },
                    maxLength: {
                        value: 32,
                        message: "Login must be from 6 to 32 symbols long",
                    },
                })} placeholder="Login" type="text" className={styles.login_input} />
                {errors.login && (<label htmlFor='login' className={styles.error_message}>
                    {errors.login.message}</label>)}

                <input {...register("email", {
                    required: "Please enter the email!",
                    pattern: {
                        value: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
                        message: "Incorrect email! Example: email@email.com"
                    }
                })} placeholder="Email" type="email" className={styles.login_input} />
                {errors.email && (<label htmlFor='email' className={styles.error_message}>
                    {errors.email.message}</label>)}

                <input {...register("password", {
                    required: "Please enter the password!",
                    validate: (value) => {
                        if (!/\d/.test(value)) {
                            return "Password should include at least one number";
                        }
                        if (!/[!@#$%^&*(),.?":{}|<>_]/.test(value)) {
                            return "Password should include at least one special symbol";
                        }
                        if (!/[a-z]/.test(value)) {
                            return "Password should include at least one lowercase latin letter";
                        }
                        if (!/[A-Z]/.test(value)) {
                            return "Password should include at least one uppercase latin letter";
                        }
                        const values = getValues();
                        if (value !== values.repeatedPassword) {
                            setError("repeatedPassword", {
                                message: "Passwords do not match"
                            });
                        }
                        else {
                            clearErrors("repeatedPassword");
                        }
                        return true;
                    },
                    minLength: {
                        value: 6,
                        message: "Password must be from 6 to 32 symbols long",
                    },
                    maxLength: {
                        value: 32,
                        message: "Password must be from 6 to 32 symbols long",
                    },
                })} placeholder="Enter the password" type="password" className={styles.login_input} />
                {errors.password && (<label htmlFor='password' className={styles.error_message}>
                    {errors.password.message}</label>)}

                <input {...register("repeatedPassword", {
                            required: "Please repeat the password",
                            validate: (value) => {
                                const values = getValues();
                                if (value !== values.password) {
                                return "Passwords do not match";
                                }
                                return true;
                            },
                })} placeholder="Repeat the password" type="password" className={styles.login_input} />
                {errors.repeatedPassword && (<label htmlFor='repeatedPassword' className={styles.error_message}>
                    {errors.repeatedPassword.message}</label>)}

                <button className='styled_button' style={{fontSize:"15px", 
                    padding:"10px 15px", 
                    backgroundColor:"#15ab65"}} type="submit" disabled={isSubmitting}>Sign up</button>
                {isSubmitting ? <img className='loading_animation' src="/assets/loading.gif"></img>
                    : isSubmitSuccessful ? 
                    <p className='response-message'>Login successful!</p> 
                    : <p className={styles.error_message}>{errors.root && (errors.root.message)}</p>}
                <h4 style={{margin: 0, marginBottom: "20px"}}>
                    <span>Already have an account? </span>
                    <span className={styles.inline_link} onClick={() => navigate("/login")}>Click here to sign in.</span>
                </h4>
            </form>
        </div>
    </div>
    )
}

export default Register
