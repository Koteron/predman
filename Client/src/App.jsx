import './App.css'

import { createBrowserRouter, RouterProvider, } from 'react-router-dom';
import routesConfig from './config/RoutesConfig.jsx';
import { useEffect } from 'react';
import useAuthStore from './state/useAuthStore.jsx';

const router = createBrowserRouter(routesConfig);

const App = () => {

  useEffect(() => {
    const token = localStorage.getItem('auth-storage') 
      ? JSON.parse(localStorage.getItem('auth-storage')).state?.user?.token 
      : null;

    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const isExpired = payload.exp * 1000 < Date.now();

      if (isExpired) {
        useAuthStore.getState().logout();
      }
    }
  }, []);

  return (
    <div className="app">
      <RouterProvider router={router} />
    </div>
  );
};

export default App
