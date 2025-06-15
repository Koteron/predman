import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import NewProjectForm from './NewProjectForm';

describe('NewProjectForm', () => {
    const setup = (onCreateProject = jest.fn()) => {
        render(<NewProjectForm onCreateProject={onCreateProject} />);
        return { onCreateProject };
    };

    test('renders form fields', () => {
        setup();

        expect(screen.getByPlaceholderText(/name/i)).toBeInTheDocument();
        expect(screen.getByPlaceholderText(/description/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/due date/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /create/i })).toBeInTheDocument();
    });

    test('shows required errors on empty submit', async () => {
        setup();

        fireEvent.click(screen.getByRole('button', { name: /create/i }));

        await waitFor(() => {
            expect(screen.getByText(/please enter the project name/i)).toBeInTheDocument();
            expect(screen.getByText(/please enter the expected due date/i)).toBeInTheDocument();
        });
    });

    test('calls onCreateProject with form values on successful submit', async () => {
        const mockOnCreate = jest.fn().mockResolvedValue(null);
        setup(mockOnCreate);

        fireEvent.change(screen.getByPlaceholderText(/name/i), { target: { value: 'My Project' } });
        fireEvent.change(screen.getByLabelText(/due date/i), { target: { value: '2025-12-31' } });
        fireEvent.change(screen.getByPlaceholderText(/description/i), { target: { value: 'Some desc' } });

        fireEvent.click(screen.getByRole('button', { name: /create/i }));

        await waitFor(() => {
            expect(mockOnCreate).toHaveBeenCalledWith('My Project', 'Some desc', '2025-12-31');
            expect(screen.getByText(/project created!/i)).toBeInTheDocument();
        });
    });

    test('displays error message from onCreateProject if it returns a string', async () => {
        const mockOnCreate = jest.fn().mockResolvedValue('Server error!');
        setup(mockOnCreate);

        fireEvent.change(screen.getByPlaceholderText(/name/i), { target: { value: 'My Project' } });
        fireEvent.change(screen.getByLabelText(/due date/i), { target: { value: '2025-12-31' } });
        fireEvent.change(screen.getByPlaceholderText(/description/i), { target: { value: 'Some desc' } });

        fireEvent.click(screen.getByRole('button', { name: /create/i }));

        await waitFor(() => {
            expect(mockOnCreate).toHaveBeenCalledWith('My Project', 'Some desc', '2025-12-31');
            expect(screen.getByText(/server error!/i)).toBeInTheDocument();
        });
    });

    test('shows loading indicator while submitting', async () => {
        let resolve;
        const mockOnCreate = jest.fn(() => new Promise(r => (resolve = r)));
        setup(mockOnCreate);

        fireEvent.change(screen.getByPlaceholderText(/name/i), { target: { value: 'Loading Project' } });
        fireEvent.change(screen.getByLabelText(/due date/i), { target: { value: '2025-12-31' } });
        fireEvent.click(screen.getByRole('button', { name: /create/i }));

        expect(await screen.findByRole('img')).toHaveAttribute('src', '/assets/loading.gif');

        resolve(null);
    });
});
