import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import AuthRedirectedRoute from './AuthRedirectedRoute';

jest.mock('../../state/useAuthStore', () => ({
  __esModule: true,
  default: jest.fn(),
}));

import useAuthStore from '../../state/useAuthStore';

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => {
  const actual = jest.requireActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('AuthRedirectedRoute', () => {
  const TestComponent = () => <div>Protected Content</div>;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderWithRoute = ({ isLoggedIn, requireLogin }) => {
    useAuthStore.mockReturnValue({ isLoggedIn });

    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route
            element={
              <AuthRedirectedRoute route="/redirect" requireLogin={requireLogin} />
            }
          >
            <Route path="/" element={<TestComponent />} />
          </Route>
          <Route path="/redirect" element={<div>Redirected</div>} />
        </Routes>
      </MemoryRouter>
    );
  };

  test('redirects to route if not logged in but login is required', () => {
    renderWithRoute({ isLoggedIn: false, requireLogin: true });
    expect(mockNavigate).toHaveBeenCalledWith('/redirect', { replace: true });
  });

  test('redirects to route if logged in but login is not required (e.g., public route)', () => {
    renderWithRoute({ isLoggedIn: true, requireLogin: false });
    expect(mockNavigate).toHaveBeenCalledWith('/redirect', { replace: true });
  });

  test('renders children when login requirement matches auth status (requireLogin=true & isLoggedIn=true)', () => {
    renderWithRoute({ isLoggedIn: true, requireLogin: true });
    expect(mockNavigate).not.toHaveBeenCalled();
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  test('renders children when login is not required and user is not logged in', () => {
    renderWithRoute({ isLoggedIn: false, requireLogin: false });
    expect(mockNavigate).not.toHaveBeenCalled();
    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });
});
