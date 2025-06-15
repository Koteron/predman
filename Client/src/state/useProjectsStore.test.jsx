import useProjectsStore from './useProjectsStore';

describe('useProjectsStore', () => {
  beforeEach(() => {
    const { clearProjects } = useProjectsStore.getState();
    clearProjects();
  });

  test('has initial state: projects null', () => {
    const state = useProjectsStore.getState();
    expect(state.projects).toBe(null);
  });

  test('setProjects sets the projects array', () => {
    const projects = [
      { id: '1', name: 'Project One' },
      { id: '2', name: 'Project Two' },
    ];

    const { setProjects } = useProjectsStore.getState();
    setProjects(projects);

    const state = useProjectsStore.getState();
    expect(state.projects).toEqual(projects);
  });

  test('clearProjects resets projects to null', () => {
    const { setProjects, clearProjects } = useProjectsStore.getState();
    setProjects([{ id: '1', name: 'Project' }]);

    clearProjects();
    const state = useProjectsStore.getState();
    expect(state.projects).toBe(null);
  });

  test('updateItem updates a specific project by id', () => {
    const initialProjects = [
      { id: '1', name: 'Old Name', description: 'Old desc' },
      { id: '2', name: 'Other Project' },
    ];

    const { setProjects, updateItem } = useProjectsStore.getState();
    setProjects(initialProjects);

    updateItem('1', { name: 'Updated Name', newField: 'new value' });

    const state = useProjectsStore.getState();
    expect(state.projects).toEqual([
      { id: '1', name: 'Updated Name', description: 'Old desc', newField: 'new value' },
      { id: '2', name: 'Other Project' },
    ]);
  });

  test('updateItem does not modify state if id not found', () => {
    const initialProjects = [
      { id: '1', name: 'Alpha' },
      { id: '2', name: 'Beta' },
    ];

    const { setProjects, updateItem } = useProjectsStore.getState();
    setProjects(initialProjects);

    updateItem('3', { name: 'Gamma' });

    const state = useProjectsStore.getState();
    expect(state.projects).toEqual(initialProjects); // unchanged
  });
});
