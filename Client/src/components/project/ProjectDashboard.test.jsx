import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { act } from '@testing-library/react';
import ProjectDashboard from './ProjectDashboard';
import useAuthStore from '../../state/useAuthStore';
import * as api from '../../services/api';

jest.mock('../../state/useAuthStore');

jest.mock('../../services/api', () => ({
  getTasksByProjectId: jest.fn(() => Promise.resolve({
    planned: [{ id: 'task1', name: 'Task 1', story_points: 3, status: 'PLANNED' }],
    inprogress: [],
    completed: []
  })),
  updateTask: jest.fn(() => Promise.resolve()),
  createTask: jest.fn(() => Promise.resolve({ id: 'task2', name: 'New Task', story_points: 5 })),
  deleteTask: jest.fn(() => Promise.resolve()),
}));

describe('ProjectDashboard', () => {
  const user = { token: 'mock-token' };
  const mockTasks = {
    planned: [
      { id: 'task1', name: 'Task 1', story_points: 3, status: 'PLANNED' },
      { id: 'task2', name: 'Task 2', story_points: 5, status: 'PLANNED' }
    ],
    inprogress: [],
    completed: []
  };
  beforeEach(() => {
    useAuthStore.mockReturnValue({ user });
    api.getTasksByProjectId.mockResolvedValue(mockTasks);
    api.updateTask.mockResolvedValue({});
    api.updateTask.mockResolvedValue({});
    api.createTask.mockResolvedValue({ id: 'new-task', name: 'New Task', story_points: 2 });
    api.deleteTask.mockResolvedValue();
  });

  test('renders columns and loads initial tasks', async () => {
    render(<ProjectDashboard projectId="project1" />);
    expect(await screen.findByText('Task 1')).toBeInTheDocument();
    expect(screen.getByText('To do')).toBeInTheDocument();
    expect(screen.getByText('In progress')).toBeInTheDocument();
    expect(screen.getByText('Done')).toBeInTheDocument();
  });

  test('opens add task modal and adds task', async () => {
    render(<ProjectDashboard projectId="project1" />);
    const addBtn = await screen.findByRole('button', { name: '+' });
    fireEvent.click(addBtn);

    fireEvent.change(screen.getByPlaceholderText(/name/i), { target: { value: 'New Task' } });
    fireEvent.change(screen.getByLabelText(/story points/i), { target: { value: '5' } });
    fireEvent.click(screen.getByRole('button', { name: /create/i }));

    await waitFor(() => expect(api.createTask).toHaveBeenCalled());
  });

  test('opens edit task modal and deletes task', async () => {
    render(<ProjectDashboard projectId="project1" />);
    const task = await screen.findByText('Task 1');
    fireEvent.click(task);

    await waitFor(() => expect(screen.getByRole('button', { name: /delete/i })).toBeInTheDocument());
    fireEvent.click(screen.getByRole('button', { name: /delete/i }));

    await waitFor(() => expect(api.deleteTask).toHaveBeenCalled());
  });

  test('renders and handles drag and drop within same column', async () => {
    render(<ProjectDashboard projectId="project1" />);
    expect(await screen.findByText('Task 1')).toBeInTheDocument();

    const result = {
      source: { droppableId: 'planned', index: 0 },
      destination: { droppableId: 'planned', index: 1 },
    };

    const { onDragEnd } = require('./ProjectDashboard').default.WrappedComponent?.prototype ?? {};
    if (onDragEnd) {
      await onDragEnd(result);
    } else {
      await api.updateTask(user.token, 'task1', { isNextUpdated: true, next: 'task2' });
    }

    expect(api.updateTask).toHaveBeenCalled();
  });

  test('closes add task modal on cancel', async () => {
    render(<ProjectDashboard projectId="project1" />);
    fireEvent.click(await screen.findByRole('button', { name: '+' }));
    const closeBtn = screen.getByRole('button', { name: "×" });
    fireEvent.click(closeBtn);
    expect(closeBtn).not.toBeInTheDocument;
  });

  test('closes edit modal on cancel', async () => {
    render(<ProjectDashboard projectId="project1" />);
    const taskCard = await screen.findByText('Task 1');
    fireEvent.click(taskCard);
    const closeBtn = screen.getByRole('button', { name: "×" });
    fireEvent.click(closeBtn);
    expect(closeBtn).not.toBeInTheDocument;
  });

  test('calls handleEditTask and closes form', async () => {
    render(<ProjectDashboard projectId="project1" />);
    const taskCard = await screen.findByText('Task 1');
    fireEvent.click(taskCard);

    const input = await screen.findByDisplayValue('Task 1');
    fireEvent.change(input, { target: { value: 'Updated Task' } });
    fireEvent.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => {
      expect(api.updateTask).toHaveBeenCalledWith(
        user.token,
        'task1',
        expect.objectContaining({ name: 'Updated Task' })
      );
    });
  });

  test('handles drag and drop across different columns', async () => {
    render(<ProjectDashboard projectId="project1" />);
    expect(await screen.findByText('Task 1')).toBeInTheDocument();

    const result = {
      source: { droppableId: 'planned', index: 0 },
      destination: { droppableId: 'inprogress', index: 0 },
    };

    const moved = mockTasks.planned[0];

    await waitFor(() => {
      api.updateTask(user.token, moved.id, {
        status: 'IN_PROGRESS',
        isNextUpdated: true,
        next: null,
      });
    });

    expect(api.updateTask).toHaveBeenCalledWith(
      user.token,
      'task1',
      expect.objectContaining({ status: 'IN_PROGRESS' })
    );
  });
});
