import styles from './Home.module.css'
import { Link } from 'react-router-dom';
import useAuthStore from '../state/useAuthStore';

const Home = () => {
    const { isLoggedIn, user } = useAuthStore();
    return (
    <>
        <div className={styles.navbar}>
            <Link to="/">
                <img className="logo" src="/assets/logo.png"/>
            </Link>
            <div className={styles.navbar_button_container}>
                { isLoggedIn 
                    ? <Link to={`/profile`}> <button className="styled_button">Profile</button> </Link>
                    : <Link to="/login"> <button className="styled_button">Login</button> </Link>
                }
            </div>
        </div>
        <div className={styles.home_body}>
            <div className={styles.text_container}>
                <h1 className={styles.title}> Predman</h1>
                <h2 className={styles.sub_title}> The project manager with deadline predictions</h2>
                <p className={styles.description}>Integrate machine learning into your 
                    project management to automatically predict the
                    completion dates with confidence estimation!</p>
                <div>
                    <Link to={isLoggedIn ? '/profile' : '/login'}>
                        <button style={{
                            minHeight:"9vh",
                            minWidth:"24vh",
                            fontSize:"clamp(20px, 2vw, 2vw)", 
                            whiteSpace:"nowrap"}}
                            className="styled_button">
                                Get started
                        </button>
                    </Link>
                </div>
            </div>
            <img className={styles.decoration_image} src="/assets/decoration 1.gif" />
        </div>
    </>
    )
}

export default Home
