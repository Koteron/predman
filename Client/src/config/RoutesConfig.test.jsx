import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import routesConfig from './RoutesConfig';
import '@testing-library/jest-dom';

function renderRoutesWithPath(initialPath) {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <Routes>
        {routesConfig.map((route, i) =>
          route.children ? (
            <Route key={i} element={route.element}>
              {route.children.map((child, j) => (
                <Route key={j} path={child.path} element={child.element} />
              ))}
            </Route>
          ) : (
            <Route key={i} path={route.path} element={route.element} />
          )
        )}
      </Routes>
    </MemoryRouter>
  );
}

jest.mock('../components/common/AuthRedirectedRoute.jsx', () => {
  const React = require('react');
  const { Outlet } = require('react-router-dom');
  return function MockAuthRedirectedRoute() {
    return <Outlet />;
  };
});


jest.mock('../pages/Home.jsx', () => () => <div>Home Page</div>);
jest.mock('../pages/Login.jsx', () => () => <div>Login Page</div>);
jest.mock('../pages/Register.jsx', () => () => <div>Register Page</div>);
jest.mock('../pages/Profile.jsx', () => () => <div>Profile Page</div>);
jest.mock('../pages/Project.jsx', () => () => <div>Project Page</div>);

describe('routesConfig', () => {
  test('renders Home at "/"', () => {
    renderRoutesWithPath('/');
    expect(screen.getByText('Home Page')).toBeInTheDocument();
  });

  test('renders Login at "/login"', () => {
    renderRoutesWithPath('/login');
    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });

  test('renders Register at "/register"', () => {
    renderRoutesWithPath('/register');
    expect(screen.getByText('Register Page')).toBeInTheDocument();
  });

  test('renders Profile at "/profile"', () => {
    renderRoutesWithPath('/profile');
    expect(screen.getByText('Profile Page')).toBeInTheDocument();
  });

  test('renders Project at "/project/:projectId"', () => {
    renderRoutesWithPath('/project/123');
    expect(screen.getByText('Project Page')).toBeInTheDocument();
  });
});
