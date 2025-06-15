import { render, screen } from '@testing-library/react';
import Prediction from './Prediction';

describe('Prediction component', () => {
    const baseProjectInfo = {
        predicted_deadline: '2025-12-31',
        due_date: '2025-12-20',
        certainty_percent: 0.8456
    };

    test('renders predicted deadline and certainty percent correctly', () => {
        render(<Prediction projectInfo={baseProjectInfo} />);

        expect(screen.getByTestId("completion_date")).toHaveTextContent('2025-12-31');
        expect(screen.getByTestId("certainty")).toHaveTextContent('84.56%');
    });

    test('formats very small percent as 0.00%', () => {
        render(<Prediction projectInfo={{ ...baseProjectInfo, certainty_percent: 0.00001 }} />);

        expect(screen.getByTestId("certainty")).toHaveTextContent('0.00%');
    });

    test('renders nothing if projectInfo is null', () => {
        const { container } = render(<Prediction projectInfo={null} />);
        expect(container).toBeEmptyDOMElement();
    });

    test('renders exact due date in parentheses', () => {
        render(<Prediction projectInfo={baseProjectInfo} />);
        expect(screen.getByTestId("certainty")).toHaveTextContent('(2025-12-20)');
    });
});
