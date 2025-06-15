import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import AddTaskForm from './AddTaskForm';

describe('AddTaskForm', () => {
    const setup = (onCreateTask = jest.fn()) => {
        render(<AddTaskForm onCreateTask={onCreateTask} />);
        return { onCreateTask };
    };

    test('renders form fields', () => {
        setup();

        expect(screen.getByPlaceholderText(/name/i)).toBeInTheDocument();
        expect(screen.getByPlaceholderText(/description/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/story points/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /create/i })).toBeInTheDocument();
    });

    test('shows required errors on empty submit', async () => {
        setup();

        fireEvent.click(screen.getByRole('button', { name: /create/i }));

        await waitFor(() => {
            expect(screen.getByText(/please enter the task name/i)).toBeInTheDocument();
            expect(screen.getByText(/please enter the amount of estimated story points/i)).toBeInTheDocument();
        });
    });

    test('calls onCreateTask with form values on successful submit', async () => {
        const mockOnCreate = jest.fn().mockResolvedValue(null);
        setup(mockOnCreate);

        fireEvent.change(screen.getByPlaceholderText(/name/i), { target: { value: 'Test Task' } });
        fireEvent.change(screen.getByLabelText(/story points/i), { target: { value: '3.5' } });
        fireEvent.change(screen.getByPlaceholderText(/description/i), { target: { value: 'Some description' } });

        fireEvent.click(screen.getByRole('button', { name: /create/i }));

        await waitFor(() => {
            expect(mockOnCreate).toHaveBeenCalledWith('Test Task', 'Some description', '3.5');
            expect(screen.getByText(/task created!/i)).toBeInTheDocument();
        });
    });

    test('displays error message from onCreateTask if it returns a string', async () => {
        const mockOnCreate = jest.fn().mockResolvedValue('Server error!');
        setup(mockOnCreate);

        fireEvent.change(screen.getByPlaceholderText(/name/i), { target: { value: 'Test Task' } });
        fireEvent.change(screen.getByLabelText(/story points/i), { target: { value: '2.0' } });
        fireEvent.change(screen.getByPlaceholderText(/description/i), { target: { value: 'Some description' } });

        fireEvent.click(screen.getByRole('button', { name: /create/i }));

        await waitFor(() => {
            expect(screen.getByText(/server error!/i)).toBeInTheDocument();
        });
    });

    test('shows loading indicator while submitting', async () => {
        let resolveSubmit;
        const mockOnCreate = jest.fn(() => new Promise(r => resolveSubmit = r));
        setup(mockOnCreate);

        fireEvent.change(screen.getByPlaceholderText(/name/i), { target: { value: 'Loading Task' } });
        fireEvent.change(screen.getByLabelText(/story points/i), { target: { value: '1.0' } });
        fireEvent.click(screen.getByRole('button', { name: /create/i }));

        expect(await screen.findByRole('img')).toHaveAttribute('src', '/assets/loading.gif');

        resolveSubmit(null);
    });
});