import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import EditTaskForm from './EditTaskForm';
import useAuthStore from '../../state/useAuthStore';
import * as api from '../../services/api';
import { act } from '@testing-library/react';

jest.mock('../../services/api', () => ({
  getTaskAssignees: jest.fn(() => Promise.resolve([])),
  getProjectMembers: jest.fn(() => Promise.resolve([])),
  assignUserToTask: jest.fn(() => Promise.resolve({ id: 'user1' })),
  removeAssignment: jest.fn(() => Promise.resolve()),
  getTaskDependencies: jest.fn(() => Promise.resolve([])),
  addTaskDependency: jest.fn(() => Promise.resolve({ dependency_id: 'task2' })),
  removeTaskDependency: jest.fn(() => Promise.resolve()),
}));

jest.mock('../../state/useAuthStore');

const editingTask = {
  id: 'task1',
  name: 'Initial Task',
  description: 'Initial Desc',
  story_points: 3.5,
};

const tasks = {
  planned: [{ id: 'task2', name: 'Planned Task' }],
  inprogress: [],
};

describe('EditTaskForm', () => {
    const user = { token: 'mock-token' };
    beforeEach(() => {
        useAuthStore.mockReturnValue({ user });
    });
  const setup = (props = {}) => {
    const onSave = jest.fn().mockResolvedValue(null);
    const onDelete = jest.fn().mockResolvedValue(null);
    const closeForm = jest.fn();
    render(<EditTaskForm
      onSave={onSave}
      onDelete={onDelete}
      closeForm={closeForm}
      loadingDelete={false}
      editingTask={editingTask}
      projectId="project1"
      tasks={tasks}
      {...props}
    />);
    return { onSave, onDelete, closeForm };
  };

  test('renders form fields with default values', () => {
    setup();
    expect(screen.getByDisplayValue(/Initial Task/)).toBeInTheDocument();
    expect(screen.getByDisplayValue(/Initial Desc/)).toBeInTheDocument();
    expect(screen.getByDisplayValue('3.5')).toBeInTheDocument();
  });

  test('shows required errors on empty submit', async () => {
    setup();
    fireEvent.change(screen.getByPlaceholderText(/name/i), { target: { value: '' } });
    fireEvent.change(screen.getByLabelText(/story points/i), { target: { value: '' } });
    fireEvent.click(screen.getByRole('button', { name: /save/i }));
    await waitFor(() => {
      expect(screen.getByText(/please enter the task name/i)).toBeInTheDocument();
      expect(screen.getByText(/please enter the amount of estimated story points/i)).toBeInTheDocument();
    });
  });

  test('calls onSave with changed fields', async () => {
    const { onSave, closeForm } = setup();
    fireEvent.change(screen.getByPlaceholderText(/name/i), { target: { value: 'Updated Task' } });
    fireEvent.click(screen.getByRole('button', { name: /save/i }));
    await waitFor(() => {
      expect(onSave).toHaveBeenCalledWith({ name: 'Updated Task' });
      expect(closeForm).toHaveBeenCalled();
    });
  });

  test('calls onDelete when delete button clicked', async () => {
    const { onDelete } = setup();
    fireEvent.click(screen.getByRole('button', { name: /delete/i }));
    await waitFor(() => {
      expect(onDelete).toHaveBeenCalled();
    });
  });

  test('displays loading indicator when submitting', async () => {
    let resolveSubmit;

    const mockOnSave = jest.fn(() => new Promise((resolve) => {
        resolveSubmit = resolve;
    }));

    const mockCloseForm = jest.fn();

    render(<EditTaskForm
        onSave={mockOnSave}
        onDelete={jest.fn()}
        closeForm={mockCloseForm}
        loadingDelete={false}
        editingTask={{ id: '1', name: 'Old Task', description: '', story_points: 3 }}
        projectId={'p1'}
        tasks={{ planned: [], inprogress: [] }}
    />);

    fireEvent.change(screen.getByPlaceholderText(/name/i), { target: { value: 'Changed Name' } });
    fireEvent.change(screen.getByLabelText(/story points/i), { target: { value: '5' } });

    fireEvent.click(screen.getByRole('button', { name: /save/i }));

    expect(await screen.findByRole('img')).toHaveAttribute('src', '/assets/loading.gif');

    resolveSubmit();
  });

  test('returns null when editingTask is null', () => {
    const { container } = render(
      <EditTaskForm
        editingTask={null}
        onSave={jest.fn()}
        onDelete={jest.fn()}
        closeForm={jest.fn()}
        loadingDelete={false}
        projectId={'proj1'}
        tasks={tasks}
      />
    );
    expect(container.firstChild).toBeNull();
  });

  test('filters and maps dependencies correctly', async () => {
    jest.spyOn(api, 'getTaskDependencies').mockResolvedValue([
      { task_id: 'task1', dependency_id: 'dep1' }
    ]);
    jest.spyOn(api, 'getTaskAssignees').mockResolvedValue([]);

    render(
      <EditTaskForm
        editingTask={editingTask}
        onSave={jest.fn()}
        onDelete={jest.fn()}
        closeForm={jest.fn()}
        loadingDelete={false}
        projectId={'proj1'}
        tasks={tasks}
      />
    );

    expect(await screen.findByText(/edit task/i)).toBeInTheDocument();
  });

  test('fetches project members when selecting assignees', async () => {
    jest.spyOn(api, 'getProjectMembers').mockResolvedValue([{ id: 'user1', email: 'test@example.com' }]);
    jest.spyOn(api, 'getTaskAssignees').mockResolvedValue([]);
    jest.spyOn(api, 'getTaskDependencies').mockResolvedValue([]);

    render(
      <EditTaskForm
        editingTask={editingTask}
        onSave={jest.fn()}
        onDelete={jest.fn()}
        closeForm={jest.fn()}
        loadingDelete={false}
        projectId={'proj1'}
        tasks={tasks}
      />
    );

    const wrapper = screen.getByText('Assignees:').closest('div');
    const addButton = wrapper.querySelector('button');
    fireEvent.click(addButton);
    expect(await screen.findByText(/select users/i)).toBeInTheDocument();
  });

  test('can add and remove an assignee via handler', async () => {
    const assignSpy = jest.spyOn(api, 'assignUserToTask').mockResolvedValue({ id: 'user2', email: 'added@example.com' });
    const removeSpy = jest.spyOn(api, 'removeAssignment').mockResolvedValue();

    let component;
    await act(async () => {
      const { container } = render(
        <EditTaskForm
          editingTask={editingTask}
          onSave={jest.fn()}
          onDelete={jest.fn()}
          closeForm={jest.fn()}
          loadingDelete={false}
          projectId={'proj1'}
          tasks={tasks}
        />
      );
      component = container;
    });

    const addBtn = screen.getByText('Assignees:').closest('div').querySelector('button');
    fireEvent.click(addBtn);

    await act(async () => {
      await api.assignUserToTask(user.token, 'user2', 'proj1', editingTask.id);
      await api.removeAssignment(user.token, 'user2', 'proj1', editingTask.id);
    });

    expect(assignSpy).toHaveBeenCalledWith(user.token, 'user2', 'proj1', editingTask.id);
    expect(removeSpy).toHaveBeenCalledWith(user.token, 'user2', 'proj1', editingTask.id);
  });

  test('can add and remove a dependency via handler', async () => {
    const addSpy = jest.spyOn(api, 'addTaskDependency').mockResolvedValue({ dependency_id: 'dep1' });
    const removeSpy = jest.spyOn(api, 'removeTaskDependency').mockResolvedValue();

    await act(async () => {
      const { container } = render(
        <EditTaskForm
          editingTask={editingTask}
          onSave={jest.fn()}
          onDelete={jest.fn()}
          closeForm={jest.fn()}
          loadingDelete={false}
          projectId={'proj1'}
          tasks={tasks}
        />
      );

      await api.addTaskDependency(user.token, editingTask.id, 'dep1');
      await api.removeTaskDependency(user.token, editingTask.id, 'dep1');
    });

    expect(addSpy).toHaveBeenCalledWith(user.token, editingTask.id, 'dep1');
    expect(removeSpy).toHaveBeenCalledWith(user.token, editingTask.id, 'dep1');
  });
});
