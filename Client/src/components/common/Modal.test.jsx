import { render, screen, fireEvent } from '@testing-library/react';
import Modal from './Modal';

describe('Modal component', () => {
  const modalContent = <div>Modal Body</div>;

  test('does not render when open is false', () => {
    render(<Modal open={false} onClose={jest.fn()}>{modalContent}</Modal>);
    expect(screen.queryByText('Modal Body')).toBeNull();
  });

  test('renders when open is true', () => {
    render(<Modal open={true} onClose={jest.fn()}>{modalContent}</Modal>);
    expect(screen.getByText('Modal Body')).toBeInTheDocument();
  });

  test('calls onClose when clicking on overlay', () => {
    const onClose = jest.fn();
    render(<Modal open={true} onClose={onClose}>{modalContent}</Modal>);

    const overlay = screen.getByText('Modal Body').parentElement.parentElement;
    fireEvent.click(overlay);

    expect(onClose).toHaveBeenCalledTimes(1);
  });

  test('does not call onClose when clicking inside modal content', () => {
    const onClose = jest.fn();
    render(<Modal open={true} onClose={onClose}>{modalContent}</Modal>);

    const content = screen.getByText('Modal Body');
    fireEvent.click(content);

    expect(onClose).not.toHaveBeenCalled();
  });

  test('calls onClose when clicking the close button', () => {
    const onClose = jest.fn();
    render(<Modal open={true} onClose={onClose}>{modalContent}</Modal>);

    const closeButton = screen.getByRole('button');
    fireEvent.click(closeButton);

    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
