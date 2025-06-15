import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import UserList from './UserList';
import useAuthStore from '../../state/useAuthStore';

jest.mock('../../state/useAuthStore');

const mockUser = { id: 'user1', email: 'user1@example.com' };

jest.mock('../../state/useAuthStore', () => ({
  __esModule: true,
  default: jest.fn(() => ({ user: { id: '1', email: 'owner@example.com' } }))
}));

describe('UserList component', () => {
  const mockOnAdd = jest.fn(() => 'User already exists');
  const mockOnRemove = jest.fn();
  const mockOnLeave = jest.fn();
  const mockOnChangeOwner = jest.fn();
  const mockSetIsAdding = jest.fn();

  const defaultProps = {
    userList: [
      { id: '1', email: 'owner@example.com' },
      { id: '2', email: 'member@example.com' },
    ],
    projectInfo: { owner_id: '1' },
    onAdd: mockOnAdd,
    onRemove: mockOnRemove,
    onLeave: mockOnLeave,
    onChangeOwner: mockOnChangeOwner,
    isAdding: false,
    setIsAdding: mockSetIsAdding
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders member emails and remove button for non-owner', () => {
    render(<UserList {...defaultProps} />);
    expect(screen.getByText('owner@example.com')).toBeInTheDocument();
    expect(screen.getByText('member@example.com')).toBeInTheDocument();
    fireEvent.click(screen.getAllByRole('button', { name: /×/i })[0]);
    expect(mockOnRemove).toHaveBeenCalledWith(defaultProps.userList[1]);
  });

  test('renders + button to toggle add form', () => {
    render(<UserList {...defaultProps} isAdding={false} />);
    const addBtn = screen.getByRole('button', { name: '+' });
    fireEvent.click(addBtn);
    expect(mockSetIsAdding).toHaveBeenCalledWith(true);
  });

  test('renders form when isAdding is true and submits new user', () => {
    render(<UserList {...defaultProps} isAdding={true} />);
    fireEvent.change(screen.getByPlaceholderText('New member email'), {
      target: { value: 'test@example.com' }
    });
    fireEvent.click(screen.getByRole('button', { name: '+' }));
    expect(mockOnAdd).toHaveBeenCalledWith('test@example.com');
    expect(screen.getByText('User already exists')).toBeInTheDocument();
  });

  test('calls onLeave when leave button clicked', () => {
    render(<UserList {...defaultProps} />);
    fireEvent.click(screen.getByRole('button', { name: 'Leave' }));
    expect(mockOnLeave).toHaveBeenCalled();
  });

  test('calls onChangeOwner when make owner button is clicked', () => {
    render(<UserList {...defaultProps} />);
    const makeOwnerBtn = screen.getAllByRole('button').find(btn => btn.className.includes('make_owner_button'));
    fireEvent.click(makeOwnerBtn);
    expect(mockOnChangeOwner).toHaveBeenCalledWith(defaultProps.userList[1]);
  });

  test('renders members and leave button (not owner)', () => {
    useAuthStore.mockReturnValue({ user: mockUser });
    render(<UserList {...defaultProps} />);
    expect(screen.getByText('owner@example.com')).toBeInTheDocument();
    expect(screen.getByText('member@example.com')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /leave/i })).toBeInTheDocument();
  });

  test('does not render remove or make owner buttons if not owner', () => {
    useAuthStore.mockReturnValue({ user: mockUser });
    render(<UserList {...defaultProps} />);
    expect(screen.queryByRole('button', { name: /×/ })).toBeNull();
    expect(screen.queryByRole('button', { name: '' })).toBeNull();
  });

  test('renders add button and toggles setIsAdding on clicks (owner)', async () => {
    useAuthStore.mockReturnValue({ user: { id: '1', email: 'owner@example.com' } });
    render(<UserList {...defaultProps} />);
    const addButton = screen.getByRole('button', { name: '+' });
    fireEvent.click(addButton);
    expect(defaultProps.setIsAdding).toHaveBeenCalledWith(true);
  });

  test('renders add button and toggles setIsAdding on clicks (owner)', async () => {
    useAuthStore.mockReturnValue({ user: { id: '1', email: 'owner@example.com' } });
    render(<UserList {...defaultProps} isAdding={true} />);
    const backButton = screen.getByRole('button', { name: '<' });
    fireEvent.click(backButton);
    expect(defaultProps.setIsAdding).toHaveBeenCalledWith(false);
  });
});
