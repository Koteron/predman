import Home from '../pages/Home.jsx';
import Login from '../pages/Login.jsx';
import Register from '../pages/Register.jsx';
import Profile from '../pages/Profile.jsx';
import AuthRedirectedRoute from '../components/common/AuthRedirectedRoute.jsx';
import Project from '../pages/Project.jsx';

const routesConfig = [
    { path: "/", element: <Home /> },
    {
      element: <AuthRedirectedRoute route={'/profile'} requireLogin={false} />,
      children: [
        { path: "/login", element: <Login /> },
        { path: "/register", element: <Register /> },
      ]
    },
    {
      element: <AuthRedirectedRoute route={'/login'} requireLogin={true} />,
      children: [
        { path: "/profile", element: <Profile /> },
        { path: "/project/:projectId", element: <Project /> },
      ]
    },
];

export default routesConfig;