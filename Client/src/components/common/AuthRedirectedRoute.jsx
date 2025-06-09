import { Outlet, useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import useAuthStore from '../../state/useAuthStore';

const AuthRedirectedRoute = ({ route, requireLogin }) => {
  const { isLoggedIn } = useAuthStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (requireLogin && !isLoggedIn || !requireLogin && isLoggedIn) {
      navigate(route, { replace: true });
      return;
    }

  }, [isLoggedIn]);

  return <Outlet />;
};

export default AuthRedirectedRoute;
