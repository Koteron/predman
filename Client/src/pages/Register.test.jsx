import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Register from './Register';

jest.mock('../services/api', () => ({
    registerUser: jest.fn(),
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
import { registerUser } from '../services/api';

describe('Register component', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('renders register form', () => {
        render(<Register />, { wrapper: MemoryRouter });

        expect(screen.getByPlaceholderText('Login')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Email')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Enter the password')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Repeat the password')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /sign up/i })).toBeInTheDocument();
    });

    test('shows required error messages on submit without input', async () => {
        render(<Register />, { wrapper: MemoryRouter });

        fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

        await waitFor(() => {
            expect(screen.getByText(/please enter the login/i)).toBeInTheDocument();
            expect(screen.getByText(/please enter the email/i)).toBeInTheDocument();
            expect(screen.getByText(/please enter the password/i)).toBeInTheDocument();
            expect(screen.getByText(/please repeat the password/i)).toBeInTheDocument();
        });
    });

    test('validates password and repeated password matching', async () => {
        render(<Register />, { wrapper: MemoryRouter });

        fireEvent.input(screen.getByPlaceholderText('Login'), { target: { value: 'testlogin' } });
        fireEvent.input(screen.getByPlaceholderText('Email'), { target: { value: 'test@example.com' } });
        fireEvent.input(screen.getByPlaceholderText('Enter the password'), { target: { value: 'Pass1!' } });
        fireEvent.input(screen.getByPlaceholderText('Repeat the password'), { target: { value: 'Mismatch1!' } });

        fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

        await waitFor(() => {
            expect(screen.getByText(/passwords do not match/i)).toBeInTheDocument();
        });
    });

    test('calls registerUser and navigates on successful registration', async () => {
        const userData = { username: 'testuser' };
        registerUser.mockResolvedValueOnce(userData);
        const loginMock = jest.fn();
        useAuthStore.mockReturnValue({ login: loginMock });

        render(<Register />, { wrapper: MemoryRouter });

        fireEvent.input(screen.getByPlaceholderText('Login'), { target: { value: 'testlogin' } });
        fireEvent.input(screen.getByPlaceholderText('Email'), { target: { value: 'test@example.com' } });
        fireEvent.input(screen.getByPlaceholderText('Enter the password'), { target: { value: 'Password1!' } });
        fireEvent.input(screen.getByPlaceholderText('Repeat the password'), { target: { value: 'Password1!' } });

        fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

        await waitFor(() => {
            expect(registerUser).toHaveBeenCalledWith('test@example.com', 'testlogin', 'Password1!');
            expect(loginMock).toHaveBeenCalledWith(userData);
            expect(mockNavigate).toHaveBeenCalledWith('/profile');
        });
    });

    test('displays error message for 400 status (email exists)', async () => {
        registerUser.mockRejectedValueOnce({ response: { status: 400 } });

        render(<Register />, { wrapper: MemoryRouter });

        fireEvent.input(screen.getByPlaceholderText('Login'), { target: { value: 'testlogin' } });
        fireEvent.input(screen.getByPlaceholderText('Email'), { target: { value: 'test@example.com' } });
        fireEvent.input(screen.getByPlaceholderText('Enter the password'), { target: { value: 'Password1!' } });
        fireEvent.input(screen.getByPlaceholderText('Repeat the password'), { target: { value: 'Password1!' } });

        fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

        expect(await screen.findByText(/user with this email already exists/i)).toBeInTheDocument();
    });

    test('displays error message for 500 status (server error)', async () => {
        registerUser.mockRejectedValueOnce({ response: { status: 500 } });

        render(<Register />, { wrapper: MemoryRouter });

        fireEvent.input(screen.getByPlaceholderText('Login'), { target: { value: 'testlogin' } });
        fireEvent.input(screen.getByPlaceholderText('Email'), { target: { value: 'test@example.com' } });
        fireEvent.input(screen.getByPlaceholderText('Enter the password'), { target: { value: 'Password1!' } });
        fireEvent.input(screen.getByPlaceholderText('Repeat the password'), { target: { value: 'Password1!' } });

        fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

        expect(await screen.findByText(/server error occurred/i)).toBeInTheDocument();
    });

    test('displays generic error message on unknown error', async () => {
        registerUser.mockRejectedValueOnce({});

        render(<Register />, { wrapper: MemoryRouter });

        fireEvent.input(screen.getByPlaceholderText('Login'), { target: { value: 'testlogin' } });
        fireEvent.input(screen.getByPlaceholderText('Email'), { target: { value: 'test@example.com' } });
        fireEvent.input(screen.getByPlaceholderText('Enter the password'), { target: { value: 'Password1!' } });
        fireEvent.input(screen.getByPlaceholderText('Repeat the password'), { target: { value: 'Password1!' } });

        fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

        expect(await screen.findByText(/there was an error creating an account/i)).toBeInTheDocument();
    });

    test('navigates to login page when clicking "Click here to sign in."', () => {
        render(<Register />, { wrapper: MemoryRouter });
        fireEvent.click(screen.getByText(/click here to sign in/i));
        expect(mockNavigate).toHaveBeenCalledWith('/login');
    });

    const fillAndSubmitForm = async ({ password, repeatedPassword }) => {
        fireEvent.input(screen.getByPlaceholderText('Login'), {
            target: { value: 'testlogin' },
        });
        fireEvent.input(screen.getByPlaceholderText('Email'), {
            target: { value: 'test@example.com' },
        });
        fireEvent.input(screen.getByPlaceholderText('Enter the password'), {
            target: { value: password },
        });
        fireEvent.input(screen.getByPlaceholderText('Repeat the password'), {
            target: { value: repeatedPassword ?? password },
        });

        fireEvent.click(screen.getByRole('button', { name: /sign up/i }));
    };

    test.each([
        ['Password should include at least one number', 'Password!'],
        ['Password should include at least one special symbol', 'Password1'],
        ['Password should include at least one lowercase latin letter', 'PASSWORD1!'],
        ['Password should include at least one uppercase latin letter', 'password1!'],
    ])('shows error if password validation fails: %s', async (expectedError, invalidPassword) => {
        render(<Register />, { wrapper: MemoryRouter });
        await fillAndSubmitForm({ password: invalidPassword });

        await waitFor(() => {
            expect(screen.getByText(new RegExp(expectedError, 'i'))).toBeInTheDocument();
        });
    });

    test('shows error if passwords do not match', async () => {
        render(<Register />, { wrapper: MemoryRouter });
        await fillAndSubmitForm({ password: 'Password1!', repeatedPassword: 'Password2!' });

        await waitFor(() => {
            expect(screen.getByText(/passwords do not match/i)).toBeInTheDocument();
        });
    });
});
