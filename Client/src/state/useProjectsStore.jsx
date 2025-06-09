import { create } from 'zustand';

const useProjectsStore = create((set) => ({
  projects: null,
  setProjects: (newProjects) => set({ projects: newProjects }),
  clearProjects: () => set({ projects: null }),
  updateItem: (id, updatedFields) =>
    set((state) => ({
      projects: state.projects.map((project) =>
        project.id === id ? { ...project, ...updatedFields } : project
      ),
    })),
}));

export default useProjectsStore;