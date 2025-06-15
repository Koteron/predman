import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

jest.mock('../state/useAuthStore', () => {
  const actual = jest.requireActual('zustand');
  return actual.create(() => ({
    isLoggedIn: true,
    user: {
      id: '123',
      name: 'Test User',
      token: 'valid-token',
    },
    login: jest.fn(),
    logout: jest.fn(),
  }));
});

import Project from './Project';

jest.mock('../state/useProjectsStore', () => ({
  __esModule: true,
  default: jest.fn(),
}));

import useProjectsStore from '../state/useProjectsStore';

describe('Project component', () => {
  const mockProject = { id: '1', name: 'Test Project' };

  beforeEach(() => {
    useProjectsStore.mockReturnValue({
      projects: [mockProject],
    });
  });

  test('renders project name and switches tabs', () => {
    render(
      <MemoryRouter initialEntries={['/project/1']}>
        <Routes>
          <Route path="/project/:projectId" element={<Project />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Test Project');

    expect(screen.getByText('Dashboard').className).toEqual(expect.stringContaining('tab_active'));
    expect(screen.getByText('Statistics').className).not.toEqual(expect.stringContaining('tab_active'));

    fireEvent.click(screen.getByText('Statistics'));

    expect(screen.getByText('Statistics').className).toEqual(expect.stringContaining('tab_active'));
    expect(screen.getByText('Dashboard').className).not.toEqual(expect.stringContaining('tab_active'));
  });

  test('Profile button links to /profile', () => {
    render(
      <MemoryRouter initialEntries={['/project/1']}>
        <Routes>
          <Route path="/project/:projectId" element={<Project />} />
        </Routes>
      </MemoryRouter>
    );

    const profileButton = screen.getByRole('button', { name: /profile/i });
    expect(profileButton.closest('a')).toHaveAttribute('href', '/profile');
  });

  test('clicking on Dashboard tab activates the dashboard view', () => {
    render(
        <MemoryRouter initialEntries={['/project/1']}>
        <Routes>
            <Route path="/project/:projectId" element={<Project />} />
        </Routes>
        </MemoryRouter>
    );

    fireEvent.click(screen.getByText('Statistics'));

    expect(screen.getByText('Statistics').className).toEqual(expect.stringContaining('tab_active'));

    fireEvent.click(screen.getByText('Dashboard'));

    expect(screen.getByText('Dashboard').className).toEqual(expect.stringContaining('tab_active'));
    expect(screen.getByText('Statistics').className).not.toEqual(expect.stringContaining('tab_active'));
  });

});
