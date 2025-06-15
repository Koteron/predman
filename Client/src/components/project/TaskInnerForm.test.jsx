import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import TaskInnerForm from './TaskInnerForm';

describe('TaskInnerForm', () => {
  const mockOnAdd = jest.fn();
  const mockOnRemove = jest.fn();
  const mockSetIsSelecting = jest.fn();

  const defaultProps = {
    listName: 'Assignees',
    selectMessage: 'Select user',
    displayField: 'email',
    innerList: [{ id: '1', email: 'test1@example.com' }],
    secondaryList: [
      { id: '1', email: 'test1@example.com' },
      { id: '2', email: 'test2@example.com' },
    ],
    onAdd: mockOnAdd,
    onRemove: mockOnRemove,
    setIsSelecting: mockSetIsSelecting,
    isSelecting: false,
    loadingInnerForm: false,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders innerList items with remove buttons', () => {
    render(<TaskInnerForm {...defaultProps} />);
    expect(screen.getByText('test1@example.com')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /×/i }));
    expect(mockOnRemove).toHaveBeenCalledWith('1');
  });

  test('renders add button when not selecting', () => {
    render(<TaskInnerForm {...defaultProps} isSelecting={false} />);
    const addBtn = screen.getByRole('button', { name: '+' });
    expect(addBtn).toBeInTheDocument();
    fireEvent.click(addBtn);
    expect(mockSetIsSelecting).toHaveBeenCalledWith(true);
  });

  test('renders loading indicator when loadingInnerForm is true', () => {
    render(
      <TaskInnerForm
        {...defaultProps}
        isSelecting={true}
        loadingInnerForm={true}
      />
    );
    expect(screen.getByRole('img')).toHaveAttribute('src', '/assets/loading.gif');
  });

  test('renders select dropdown and handles selection', () => {
    render(
      <TaskInnerForm
        {...defaultProps}
        isSelecting={true}
        loadingInnerForm={false}
      />
    );
    const select = screen.getByRole('combobox');
    expect(select).toBeInTheDocument();

    fireEvent.change(select, { target: { value: '2' } });
    expect(mockOnAdd).toHaveBeenCalledWith('2');
    expect(mockSetIsSelecting).toHaveBeenCalledWith(false);
  });

  test('calls setIsSelecting(false) onBlur of select', () => {
    render(
      <TaskInnerForm
        {...defaultProps}
        isSelecting={true}
        loadingInnerForm={false}
      />
    );
    const select = screen.getByRole('combobox');
    fireEvent.blur(select);
    expect(mockSetIsSelecting).toHaveBeenCalledWith(false);
  });
});
