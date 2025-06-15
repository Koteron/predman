import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Home from './Home';

jest.mock('../state/useAuthStore', () => ({
    __esModule: true,
    default: jest.fn(),
}));

import useAuthStore from '../state/useAuthStore';

describe('Home component', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    test('renders title and description', () => {
        useAuthStore.mockReturnValue({ isLoggedIn: false });

        render(<Home />, { wrapper: MemoryRouter });

        expect(screen.getByText('Predman')).toBeInTheDocument();
        expect(screen.getByText('The project manager with deadline predictions')).toBeInTheDocument();
        expect(screen.getByText(/Integrate machine learning/i)).toBeInTheDocument();
    });

    test('shows Login button when not logged in', () => {
        useAuthStore.mockReturnValue({ isLoggedIn: false });

        render(<Home />, { wrapper: MemoryRouter });

        const loginButton = screen.getByRole('button', { name: /login/i });
        expect(loginButton).toBeInTheDocument();
    });

    test('shows Profile button when logged in', () => {
        useAuthStore.mockReturnValue({ isLoggedIn: true, user: { username: 'test' } });

        render(<Home />, { wrapper: MemoryRouter });

        const profileButton = screen.getByRole('button', { name: /profile/i });
        expect(profileButton).toBeInTheDocument();
    });

    test('Get started button links correctly based on login state', () => {
        useAuthStore.mockReturnValue({ isLoggedIn: true });

        render(<Home />, { wrapper: MemoryRouter });

        const getStartedLink = screen.getByRole('link', { name: /get started/i });
        expect(getStartedLink).toHaveAttribute('href', '/profile');
    });

    test('renders logo and decoration image', () => {
        useAuthStore.mockReturnValue({ isLoggedIn: false });

        render(<Home />, { wrapper: MemoryRouter });

        expect(screen.getAllByRole('img')[0]).toHaveAttribute('src', '/assets/logo.png');
        expect(screen.getAllByRole('img')[1]).toHaveAttribute('src', '/assets/decoration 1.gif');
    });
});
