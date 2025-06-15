import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Login from './Login';

jest.mock('../services/api', () => ({
    loginUser: jest.fn(),
}));

jest.mock('../state/useAuthStore', () => ({
    __esModule: true,
    default: jest.fn(() => ({ login: jest.fn() })),
}));

const mockNavigate = jest.fn();

jest.mock('react-router-dom', () => {
    const actual = jest.requireActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

import useAuthStore from '../state/useAuthStore';
import { loginUser } from '../services/api';

describe('Login component', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('renders login form', () => {
        render(<Login />, { wrapper: MemoryRouter });

        expect(screen.getByPlaceholderText('Email')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Enter the password')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
    });

    test('shows required error messages on submit without input', async () => {
        render(<Login />, { wrapper: MemoryRouter });

        fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

        await waitFor(() => {
            expect(screen.getByText(/please enter the email/i)).toBeInTheDocument();
            expect(screen.getByText(/please enter the password/i)).toBeInTheDocument();
        });
    });

    test('calls loginUser and navigates on successful login', async () => {
        const userData = { username: 'testuser' };
        loginUser.mockResolvedValueOnce(userData);
        const loginMock = jest.fn();
        useAuthStore.mockReturnValue({ login: loginMock });

        render(<Login />, { wrapper: MemoryRouter });

        fireEvent.input(screen.getByPlaceholderText('Email'), {
            target: { value: 'test@example.com' },
        });
        fireEvent.input(screen.getByPlaceholderText('Enter the password'), {
            target: { value: 'password123' },
        });

        fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

        await waitFor(() => {
            expect(loginUser).toHaveBeenCalledWith('test@example.com', 'password123');
            expect(loginMock).toHaveBeenCalledWith(userData);
            expect(mockNavigate).toHaveBeenCalledWith('/profile');
        });
    });

    test('displays error message for 404', async () => {
        loginUser.mockRejectedValueOnce({ response: { status: 404 } });

        render(<Login />, { wrapper: MemoryRouter });

        fireEvent.input(screen.getByPlaceholderText('Email'), {
            target: { value: 'wrong@example.com' },
        });
        fireEvent.input(screen.getByPlaceholderText('Enter the password'), {
            target: { value: 'wrongpass' },
        });

        fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

        await waitFor(() => {
            expect(screen.getByText(/incorrect login or password/i)).toBeInTheDocument();
        });
    });

    test('displays loading animation during submit', async () => {
        let resolveLogin;
        loginUser.mockImplementation(
            () => new Promise((resolve) => (resolveLogin = resolve))
        );

        render(<Login />, { wrapper: MemoryRouter });

        fireEvent.input(screen.getByPlaceholderText('Email'), {
            target: { value: 'loading@example.com' },
        });
        fireEvent.input(screen.getByPlaceholderText('Enter the password'), {
            target: { value: 'loading' },
        });

        fireEvent.click(screen.getByRole('button', { name: /sign in/i }));
        await waitFor(async () => {
            expect(await screen.findByTestId('loading')).toBeInTheDocument();
        });

        resolveLogin({ username: 'loading' });
    });

    test('displays server error on 500 status', async () => {
        loginUser.mockRejectedValueOnce({ response: { status: 500 } });

        render(<Login />, { wrapper: MemoryRouter });
        fireEvent.change(screen.getByPlaceholderText(/email/i), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByPlaceholderText(/enter the password/i), { target: { value: 'password123' } });
        fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

        expect(await screen.findByText(/server error occurred/i)).toBeInTheDocument();
    });

    test('displays generic error message on unknown error', async () => {
        loginUser.mockRejectedValueOnce({});

        render(<Login />, { wrapper: MemoryRouter });
        fireEvent.change(screen.getByPlaceholderText(/email/i), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByPlaceholderText(/enter the password/i), { target: { value: 'password123' } });
        fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

        expect(await screen.findByText(/there was an error logging in/i)).toBeInTheDocument();
    });

    test('navigates to register page when clicking Sign Up now', async () => {
        render(<Login />, { wrapper: MemoryRouter });
        fireEvent.click(screen.getByText(/sign up now/i));
        expect(mockNavigate).toHaveBeenCalledWith('/register');
    });
});
