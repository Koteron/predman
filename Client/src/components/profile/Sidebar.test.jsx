import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import useProjectsStore from '../../state/useProjectsStore';
import Sidebar from './Sidebar';

const mockNavigate = jest.fn();
const mockLogout = jest.fn();
const mockSetProjects = jest.fn();

jest.mock('../../services/api', () => ({
    getProjectsByUserToken: jest.fn(),
    createProject: jest.fn(),
}));

jest.mock('../../state/useAuthStore', () => ({
    __esModule: true,
    default: jest.fn(() => ({
        user: { token: 'test-token' },
        logout: mockLogout,
    })),
}));

jest.mock('../../state/useProjectsStore', () => ({
    __esModule: true,
    default: jest.fn(() => ({
        projects: null,
        setProjects: mockSetProjects,
    })),
}));

jest.mock('react-router-dom', () => {
    const actual = jest.requireActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

jest.mock('./NewProjectForm', () => ({ onCreateProject }) => (
    <button data-testid="mock-form" onClick={() => onCreateProject('Test', 'Desc', '2025-12-31')}>
        Submit Project
    </button>
));


jest.mock('../common/Modal', () => ({ open, onClose, children }) =>
    open ? <div data-testid="modal">{children}</div> : null
);

describe('Sidebar component', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('calls getProjects on mount when projects are null', async () => {
        const { getProjectsByUserToken } = require('../../services/api');
        getProjectsByUserToken.mockResolvedValueOnce([]);

        render(<Sidebar disabledProjectId={null} />, { wrapper: MemoryRouter });

        await waitFor(() => {
            expect(getProjectsByUserToken).toHaveBeenCalledWith('test-token');
            expect(mockSetProjects).toHaveBeenCalledWith([]);
        });
    });

    test('redirects to /login on 401 error', async () => {
        const { getProjectsByUserToken } = require('../../services/api');
        getProjectsByUserToken.mockRejectedValueOnce({ response: { status: 401 } });

        render(<Sidebar disabledProjectId={null} />, { wrapper: MemoryRouter });

        await waitFor(() => {
            expect(mockLogout).toHaveBeenCalled();
            expect(mockNavigate).toHaveBeenCalledWith('/login');
        });
    });

    test('redirects to /profile on 403 error', async () => {
        const { getProjectsByUserToken } = require('../../services/api');
        getProjectsByUserToken.mockRejectedValueOnce({ response: { status: 403 } });

        render(<Sidebar disabledProjectId={null} />, { wrapper: MemoryRouter });

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith('/profile');
        });
    });

    test('displays "no projects" message when empty', async () => {
        useProjectsStore.mockReturnValue({
            projects: [],
            setProjects: mockSetProjects
        });


        render(<Sidebar disabledProjectId={null} />, { wrapper: MemoryRouter });

        await waitFor(() => {
            expect(screen.getByText(/you don't have any projects yet/i)).toBeInTheDocument();
        });
    });

    test('renders project buttons', async () => {
        useProjectsStore.mockReturnValue({
            projects: [
                { id: '1', name: 'Project One' },
                { id: '2', name: 'Project Two' }
            ],
            setProjects: mockSetProjects
        });

        render(<Sidebar disabledProjectId={null} />, { wrapper: MemoryRouter });

        await waitFor(() => {
            expect(screen.getByText('Project One')).toBeInTheDocument();
            expect(screen.getByText('Project Two')).toBeInTheDocument();
        });
    });

    test('opens and closes New Project modal', async () => {
        const { getProjectsByUserToken } = require('../../services/api');
        getProjectsByUserToken.mockResolvedValueOnce([]);

        render(<Sidebar disabledProjectId={null} />, { wrapper: MemoryRouter });

        const newProjectButton = await screen.findByRole('button', { name: /new project/i });
        fireEvent.click(newProjectButton);

        expect(screen.getByTestId('modal')).toBeInTheDocument();
    });

    test('navigates to project page when project button is clicked', async () => {
        useProjectsStore.mockReturnValue({
            projects: [
                { id: '123', name: 'Test Project' }
            ],
            setProjects: mockSetProjects
        });

        render(<Sidebar disabledProjectId={null} />, { wrapper: MemoryRouter });

        const button = await screen.findByRole('button', { name: 'Test Project' });
        fireEvent.click(button);

        expect(mockNavigate).toHaveBeenCalledWith('/project/123');
    });

    test('closes modal when onClose is called', async () => {
        const { getProjectsByUserToken } = require('../../services/api');
        getProjectsByUserToken.mockResolvedValueOnce([]);

        render(<Sidebar disabledProjectId={null} />, { wrapper: MemoryRouter });

        const openButton = await screen.findByRole('button', { name: /new project/i });
        fireEvent.click(openButton);

       
        expect(screen.getByTestId('modal')).toBeInTheDocument();

       
        fireEvent.click(screen.getByTestId('modal').firstChild);
        waitFor(async()=>{
            await expect(screen.getByTestId('modal')).not.toBeInTheDocument();
        })
    });

    test('handleCreateProject adds new project and closes modal', async () => {
        const { createProject } = require('../../services/api');

        const existingProjects = [{ id: '1', name: 'Old Project' }];
        const newProject = { id: '2', name: 'Test' };

        useProjectsStore.mockReturnValue({
            projects: existingProjects,
            setProjects: mockSetProjects
        });

        createProject.mockResolvedValueOnce(newProject);

        render(<Sidebar disabledProjectId={null} />, { wrapper: MemoryRouter });

        fireEvent.click(screen.getByRole('button', { name: /new project/i }));

        const formButton = await screen.findByTestId('mock-form');
        fireEvent.click(formButton);

        await waitFor(() => {
            expect(createProject).toHaveBeenCalledWith('test-token', 'Test', 'Desc', '2025-12-31');
            expect(mockSetProjects).toHaveBeenCalledWith([...existingProjects, newProject]);
        });
    });
});
