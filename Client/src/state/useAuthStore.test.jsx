import useAuthStore from './useAuthStore';

describe('useAuthStore', () => {
  beforeEach(() => {
    const { logout } = useAuthStore.getState();
    logout();
  });

  test('has initial state: isLoggedIn false, user null', () => {
    const state = useAuthStore.getState();
    expect(state.isLoggedIn).toBe(false);
    expect(state.user).toBe(null);
  });

  test('login sets user and isLoggedIn to true', () => {
    const userData = { id: 'user-1', name: 'Test User' };

    const { login } = useAuthStore.getState();
    login(userData);

    const state = useAuthStore.getState();
    expect(state.isLoggedIn).toBe(true);
    expect(state.user).toEqual(userData);
  });

  test('logout resets isLoggedIn and user', () => {
    const { login, logout } = useAuthStore.getState();
    login({ id: 'user-1', name: 'Test User' });

    logout();
    const state = useAuthStore.getState();
    expect(state.isLoggedIn).toBe(false);
    expect(state.user).toBe(null);
  });
});
