import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import ProjectStatistics from './ProjectStatistics';
import { MemoryRouter } from 'react-router-dom';


jest.mock('../../services/api', () => ({
  addProjectMember: jest.fn(),
  changeProjectOwner: jest.fn(),
  getFullProjectInfo: jest.fn(),
  getProjectMembers: jest.fn(),
  removeProjectMember: jest.fn(),
  updateProject: jest.fn(),
  getStatistics: jest.fn(),
  deleteProject: jest.fn(),
}));

import useAuthStore from '../../state/useAuthStore';
jest.mock('../../state/useAuthStore', () => ({
  __esModule: true,
  default: jest.fn(),
}));

jest.mock('../../state/useProjectsStore', () => ({
  __esModule: true,
  default: jest.fn(() => ({
    projects: [{ id: 'project1', name: 'Sample Project' }],
    setProjects: jest.fn(),
    updateItem: jest.fn(),
  })),
}));

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => {
  const actual = jest.requireActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

import {
  getProjectMembers,
  getFullProjectInfo,
  getStatistics,
  addProjectMember,
  changeProjectOwner,
  updateProject,
  removeProjectMember,
} from '../../services/api';

describe('ProjectStatistics', () => {
  beforeAll(() => {
    global.ResizeObserver = class {
      observe() {}
      unobserve() {}
      disconnect() {}
    };
  });

  beforeEach(() => {
    jest.clearAllMocks();

    useAuthStore.mockReturnValue({ user: { id: 'user1', token: 'token123' } });

    getProjectMembers.mockResolvedValue([
      { id: 'user1', email: 'user1@example.com' },
      { id: 'member', email: 'member@example.com' },
    ]);

    getFullProjectInfo.mockResolvedValue({
        id: 'project1',
        owner_id: 'user1',
        name: 'Test Project',
        description: 'Test Desc',
        due_date: '2025-06-20',
        available_hours: 100,
        sum_experience: 50,
        external_risk: 0.2,
    });


    getStatistics.mockResolvedValue([
      { saved_at: '2024-01-01T00:00:00Z', remaining_tasks: 5 },
    ]);
  });

  test('renders sections correctly', async () => {
    render(<ProjectStatistics projectId="project1" />, { wrapper: MemoryRouter });

    await waitFor(() => {
      expect(screen.getByText(/Project characteristics/i)).toBeInTheDocument();
      expect(screen.getByText(/Project members/i)).toBeInTheDocument();
      expect(screen.getByText(/Current prediction/i)).toBeInTheDocument();
      expect(screen.getByText(/Tasks per day graph/i)).toBeInTheDocument();
    });
  });

  test('renders delete button for owner', async () => {
    render(<ProjectStatistics projectId="project1" />, { wrapper: MemoryRouter });

    await waitFor(() => {
      const deleteButton = screen.getByRole('button', { name: /delete/i });
      expect(deleteButton).toBeInTheDocument();
    });
  });

  test('opens confirmation modal when deleting', async () => {
    render(<ProjectStatistics projectId="project1" />, { wrapper: MemoryRouter });

    await waitFor(() => {
      const deleteButton = screen.getByRole('button', { name: /delete/i });
      expect(deleteButton).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /delete/i }));

    await waitFor(() => {
      expect(screen.getByText(/Are you sure you want to delete/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /yes/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /yes/i }));
  });

    test('handles API failure with 403', async () => {
        getProjectMembers.mockRejectedValueOnce({ response: { status: 403 } });

        render(<ProjectStatistics projectId="project1" />, { wrapper: MemoryRouter });

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/profile');
        });
    });

    test('handles adding member', async () => {
    addProjectMember.mockResolvedValue({ id: 'user2', email: 'user2@example.com' });

    render(<ProjectStatistics projectId="project1" />, { wrapper: MemoryRouter });

    fireEvent.click(await screen.findByRole('button', { name: '+' }));
    const input = screen.getByPlaceholderText(/email/i);
    fireEvent.change(input, { target: { value: 'user2@example.com' } });
    fireEvent.click(await screen.findByRole('button', { name: '+' }));

    await waitFor(() => {
      expect(addProjectMember).toHaveBeenCalledWith('token123', 'user2@example.com', 'project1');
    });
  });

  test('handles change owner', async () => {
    changeProjectOwner.mockResolvedValue({ owner_id: 'user2' });

    render(<ProjectStatistics projectId="project1" />, { wrapper: MemoryRouter });

    fireEvent.click(await screen.findByRole('button', { name: "" }));
    expect(await screen.findByText(/Are you sure you want to hand project/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /yes/i }));

    await waitFor(() => {
      expect(changeProjectOwner).toHaveBeenCalled();
    });
  });

  test('handles remove member', async () => {
    removeProjectMember.mockResolvedValue();

    render(<ProjectStatistics projectId="project1" />, { wrapper: MemoryRouter });

    fireEvent.click(await screen.findByRole('button', { name: /×/i }));
    expect(await screen.findByText(/Are you sure you want to remove/i)).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /yes/i }));

    await waitFor(() => {
      expect(removeProjectMember).toHaveBeenCalled();
    });
  });

  test('handles leave project', async () => {
    removeProjectMember.mockResolvedValue();

    render(<ProjectStatistics projectId="project1" />, { wrapper: MemoryRouter });

    fireEvent.click(await screen.findByRole('button', { name: /leave/i }));
    expect(await screen.findByText(/Are you sure you want to leave/i)).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /yes/i }));

    await waitFor(() => {
      expect(removeProjectMember).toHaveBeenCalledWith('token123', 'user1', 'project1');
      expect(mockNavigate).toHaveBeenCalledWith('/profile');
    });
  });
});
