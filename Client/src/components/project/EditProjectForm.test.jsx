import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import EditProjectForm from './EditProjectForm';

const mockProjectInfo = {
    name: 'Old Name',
    due_date: '2025-12-31',
    available_hours: 120,
    sum_experience: 300,
    external_risk_probability: 0.25,
    description: 'Old description'
};

describe('EditProjectForm', () => {
    const setup = (onSave = jest.fn()) => {
        render(<EditProjectForm projectInfo={mockProjectInfo} onSave={onSave} />);
        return { onSave };
    };

    test('renders form fields with initial project info', () => {
        setup();

        expect(screen.getByLabelText(/project name/i)).toHaveValue('Old Name');
        expect(screen.getByLabelText(/due date/i)).toHaveValue('2025-12-31');
        expect(screen.getByLabelText(/available hours/i)).toHaveValue(120);
        expect(screen.getByLabelText(/sum experience/i)).toHaveValue(300);
        expect(screen.getByLabelText(/external risk probability/i)).toHaveValue(0.25);
        expect(screen.getByLabelText(/description/i)).toHaveDisplayValue('Old description');
    });

    test('shows required errors on empty name and date', async () => {
        setup();

        fireEvent.change(screen.getByLabelText(/project name/i), { target: { value: '' } });
        fireEvent.change(screen.getByLabelText(/due date/i), { target: { value: '' } });

        fireEvent.click(screen.getByRole('button', { name: /save/i }));

        await waitFor(() => {
            expect(screen.getByText(/please enter the project name/i)).toBeInTheDocument();
            expect(screen.getByText(/please enter the expected due date/i)).toBeInTheDocument();
        });
    });

    test('calls onSave only with changed fields', async () => {
        const mockOnSave = jest.fn().mockResolvedValue(null);
        setup(mockOnSave);

        fireEvent.change(screen.getByLabelText(/project name/i), { target: { value: 'New Name' } });

        fireEvent.click(screen.getByRole('button', { name: /save/i }));

        await waitFor(() => {
            expect(mockOnSave).toHaveBeenCalledWith({ name: 'New Name' });
            expect(screen.getByText(/project saved!/i)).toBeInTheDocument();
        });
    });

    test('does not call onSave if no fields are changed', async () => {
        const mockOnSave = jest.fn();
        setup(mockOnSave);

        fireEvent.click(screen.getByRole('button', { name: /save/i }));

        await waitFor(() => {
            expect(mockOnSave).not.toHaveBeenCalled();
        });
    });

    test('displays error message from onSave if returned', async () => {
        const mockOnSave = jest.fn().mockResolvedValue('Failed to save');
        setup(mockOnSave);

        fireEvent.change(screen.getByLabelText(/sum experience/i), { target: { value: 500 } });
        fireEvent.click(screen.getByRole('button', { name: /save/i }));

        await waitFor(() => {
            expect(screen.getByText(/failed to save/i)).toBeInTheDocument();
        });
    });

    test('shows loading indicator while submitting', async () => {
        let resolveSubmit;
        const mockOnSave = jest.fn(() => new Promise(res => resolveSubmit = res));
        setup(mockOnSave);

        fireEvent.change(screen.getByLabelText(/available hours/i), { target: { value: 200 } });
        fireEvent.click(screen.getByRole('button', { name: /save/i }));

        expect(await screen.findByRole('img')).toHaveAttribute('src', '/assets/loading.gif');

        resolveSubmit(null);
    });
});
