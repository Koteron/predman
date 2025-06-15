import { MemoryRouter } from "react-router-dom";
import { render } from "@testing-library/react"
export const renderWithRouter = (ui, initialEntriesProp = ["/"]) => {
    return render(<MemoryRouter
        initialEntries={initialEntriesProp} 
        future={{
        v7_relativeSplatPath: true,
        v7_startTransition: true
    }}>{ui}</MemoryRouter>);
};