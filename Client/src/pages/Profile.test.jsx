import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Profile from './Profile';
import useAuthStore from '../state/useAuthStore';
import useProjectsStore from '../state/useProjectsStore';
import { deleteUser } from '../services/api';
import { MemoryRouter } from 'react-router-dom';

jest.mock('../state/useAuthStore');
jest.mock('../state/useProjectsStore');
jest.mock('../services/api');
jest.mock('react-router-dom', () => {
  const originalModule = jest.requireActual('react-router-dom');
  return {
    ...originalModule,
    useNavigate: () => jest.fn(),
  };
});

describe('Profile component', () => {
  const mockLogout = jest.fn();
  const mockClearProjects = jest.fn();
  const mockNavigate = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    useAuthStore.mockReturnValue({
      user: { login: 'testuser', email: 'test@example.com', token: 'token123' },
      logout: mockLogout,
    });

    useProjectsStore.mockReturnValue({
      clearProjects: mockClearProjects,
    });

    jest.spyOn(require('react-router-dom'), 'useNavigate').mockImplementation(() => mockNavigate);
  });

  test('renders user info', () => {
    render(<Profile />, { wrapper: MemoryRouter });

    expect(screen.getByText(/profile/i)).toBeInTheDocument();
    expect(screen.getByTestId('profile_login')).toHaveTextContent('Login: testuser');
    expect(screen.getByTestId('profile_email')).toHaveTextContent('Email: test@example.com');
  });

  test('log out button calls clearProjects, logout and navigates to /', () => {
    render(<Profile />, { wrapper: MemoryRouter });

    fireEvent.click(screen.getByRole('button', { name: /log out/i }));

    expect(mockClearProjects).toHaveBeenCalled();
    expect(mockLogout).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith('/');
  });

  test('clicking delete account button opens confirmation modal', () => {
    render(<Profile />, { wrapper: MemoryRouter });

    fireEvent.click(screen.getByRole('button', { name: /delete account/i }));

    expect(screen.getByText(/are you sure you want to delete/i)).toBeInTheDocument();
  });

  test('handleDeleteAccount calls deleteUser and logs out on success', async () => {
    deleteUser.mockResolvedValueOnce();

    render(<Profile />, { wrapper: MemoryRouter });

    fireEvent.click(screen.getByRole('button', { name: /delete account/i }));

    const confirmButton = screen.getByRole('button', { name: /yes/i });

    fireEvent.click(confirmButton);

    await waitFor(() => {
      expect(deleteUser).toHaveBeenCalledWith('token123');
      expect(mockClearProjects).toHaveBeenCalled();
      expect(mockLogout).toHaveBeenCalled();
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  test('calls setDeleteOpen(false) when modal is closed via onClose', async () => {
    render(<Profile />, { wrapper: MemoryRouter });

    fireEvent.click(screen.getByText('Delete account'));

    expect(screen.getByText(/Are you sure you want to delete/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /×/i }));

    await waitFor(() => {
      expect(screen.queryByText(/Are you sure you want to delete/i)).not.toBeInTheDocument();
    });
  });

  test('calls setDeleteOpen(false) when onDeny in Confirmation is clicked', async () => {
    render(<Profile />, { wrapper: MemoryRouter });
    fireEvent.click(screen.getByText('Delete account'));
    fireEvent.click(screen.getByText('No'));

    await waitFor(() => {
      expect(screen.queryByText(/Are you sure you want to delete/i)).not.toBeInTheDocument();
    });
  });
});
