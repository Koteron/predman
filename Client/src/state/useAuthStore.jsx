import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useAuthStore = create(persist(
  (set) => ({
    isLoggedIn: false,
    user: null,
    login: (userData) => set({ isLoggedIn: true, user: userData }),
    logout: () => set({ isLoggedIn: false, user: null }),
  }),
  {
    name: 'auth-storage',
  }
));

// window.authStore = useAuthStore; // dev feature. To log out: authStore.getState().logout()

export default useAuthStore;