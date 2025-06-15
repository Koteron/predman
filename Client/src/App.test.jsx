import { render } from '@testing-library/react';
import App from './App';
import useAuthStore from './state/useAuthStore';
import { act } from 'react-dom/test-utils';

jest.mock('react-router-dom', () => {
  const actual = jest.requireActual('react-router-dom');
  return {
    ...actual,
    RouterProvider: () => <div>Router Loaded</div>,
  };
});

jest.mock('./state/useAuthStore', () => {
  return {
    __esModule: true,
    default: {
      getState: jest.fn(() => ({
        logout: jest.fn(),
      })),
    },
  };
});

describe('App', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  test('renders RouterProvider', () => {
    const { getByText } = render(<App />);
    expect(getByText('Router Loaded')).toBeInTheDocument();
  });

  test('calls logout if token is expired', async () => {
    const fakePayload = {
        exp: Math.floor(Date.now() / 1000) - 1000, // expired
    };
    const fakeToken = `header.${btoa(JSON.stringify(fakePayload))}.sig`;
    localStorage.setItem(
        'auth-storage',
        JSON.stringify({ state: { user: { token: fakeToken } } })
    );

    const logout = jest.fn();
    useAuthStore.getState.mockReturnValue({ logout });

    await act(async () => {
        render(<App />);
    });

    expect(logout).toHaveBeenCalled();
  });


  test('does not call logout if token is valid', () => {
    const fakePayload = {
      exp: Math.floor(Date.now() / 1000) + 3600, // valid
    };
    const fakeToken = `header.${btoa(JSON.stringify(fakePayload))}.sig`;
    localStorage.setItem(
      'auth-storage',
      JSON.stringify({ state: { user: { token: fakeToken } } })
    );

    render(<App />);

    const logout = useAuthStore.getState().logout;
    expect(logout).not.toHaveBeenCalled();
  });

  test('does not call logout if no token in localStorage', () => {
    render(<App />);

    const logout = useAuthStore.getState().logout;
    expect(logout).not.toHaveBeenCalled();
  });
});
