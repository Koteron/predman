import { render, screen, fireEvent } from '@testing-library/react';
import Confirmation from './Confirmation';

describe('Confirmation component', () => {
  const questionText = 'Are you sure you want to proceed?';

  test('renders question', () => {
    render(<Confirmation question={questionText} onConfirm={() => null} onDeny={() => {}} />);
    expect(screen.getByRole('heading', { level: 2 })).toHaveTextContent(questionText);
  });

  test('calls onConfirm when "Yes" is clicked', () => {
    const onConfirm = jest.fn(() => null);
    render(<Confirmation question={questionText} onConfirm={onConfirm} onDeny={() => {}} />);
    
    fireEvent.click(screen.getByText('Yes'));
    expect(onConfirm).toHaveBeenCalledTimes(1);
  });

  test('displays error returned by onConfirm', () => {
    const errorMessage = 'Something went wrong';
    const onConfirm = jest.fn(() => errorMessage);
    render(<Confirmation question={questionText} onConfirm={onConfirm} onDeny={() => {}} />);

    fireEvent.click(screen.getByText('Yes'));
    expect(onConfirm).toHaveBeenCalled();
    expect(screen.getByText(errorMessage)).toBeInTheDocument();
  });

  test('does not display error if onConfirm returns null', () => {
    const onConfirm = jest.fn(() => null);
    render(<Confirmation question={questionText} onConfirm={onConfirm} onDeny={() => {}} />);

    fireEvent.click(screen.getByText('Yes'));
    expect(screen.queryByText(/something went wrong/i)).not.toBeInTheDocument();
  });

  test('calls onDeny when "No" is clicked', () => {
    const onDeny = jest.fn();
    render(<Confirmation question={questionText} onConfirm={() => null} onDeny={onDeny} />);

    fireEvent.click(screen.getByText('No'));
    expect(onDeny).toHaveBeenCalledTimes(1);
  });
});
