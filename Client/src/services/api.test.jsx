const mockAxiosInstance = {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    patch: jest.fn(),
  };
  
jest.mock("axios", () => ({
    create: () => ({
        get: (...args) => mockAxiosInstance.get(...args),
        post: (...args) => mockAxiosInstance.post(...args),
        put: (...args) => mockAxiosInstance.put(...args),
        delete: (...args) => mockAxiosInstance.delete(...args),
        patch: (...args) => mockAxiosInstance.patch(...args),
    }),
}));
  
import axios from "axios";
import {
    loginUser,
    registerUser,
    deleteUser,
    getProjectsByUserToken,
    createProject,
    getTasksByProjectId,
    updateTask,
    createTask,
    deleteTask,
    getTaskAssignees,
    assignUserToTask,
    removeAssignment,
    getProjectMembers,
    getTaskDependencies,
    addTaskDependency,
    removeTaskDependency,
    getStatistics,
    getFullProjectInfo,
    updateProject,
    changeProjectOwner,
    removeProjectMember,
    addProjectMember,
    deleteProject
} from './api';
  
describe("API service functions", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    test("register sends correct payload", async () => {
        mockAxiosInstance.post.mockResolvedValue({ data: { registered: true } });
        const result = await registerUser("test@test.com", "testuser", "pass123");
        expect(mockAxiosInstance.post).toHaveBeenCalledWith(
        "/users/register",
        {
            email: "test@test.com",
            login: "testuser",
            password: "pass123",
        }
        );
        expect(result).toEqual({ registered: true });
    });
    test("login sends correct payload for email login", async () => {
        mockAxiosInstance.post.mockResolvedValue({ data: { token: "abc" } });
        const result = await loginUser("test@test.com", "pass123");
        expect(mockAxiosInstance.post).toHaveBeenCalledWith(
        "/users/login",
        {
            email: "test@test.com",
            password: "pass123"
        }
        );
        expect(result).toEqual({ token: "abc" });
    });

    test("deleteUser sends DELETE request with correct Authorization header", async () => {
        mockAxiosInstance.delete.mockResolvedValue({ data: { deleted: true } });

        const result = await deleteUser("jwt-token");

        expect(mockAxiosInstance.delete).toHaveBeenCalledWith("/users", {
            headers: {
            Authorization: "Bearer jwt-token",
            },
        });
        expect(result).toEqual({ deleted: true });
    });

    test("getProjectsByUserToken sends GET request with correct Authorization header", async () => {
        mockAxiosInstance.get.mockResolvedValue({ data: [{ id: "project-1" }] });

        const result = await getProjectsByUserToken("jwt-token");

        expect(mockAxiosInstance.get).toHaveBeenCalledWith("/projects/user", {
            headers: {
            Authorization: "Bearer jwt-token",
            },
        });
        expect(result).toEqual([{ id: "project-1" }]);
    });

    test("createProject sends POST request with correct body and Authorization header", async () => {
        mockAxiosInstance.post.mockResolvedValue({ data: { id: "project-1" } });

        const result = await createProject(
            "jwt-token",
            "Project Name",
            "Project Description",
            "2025-07-01"
        );

        expect(mockAxiosInstance.post).toHaveBeenCalledWith(
            "/projects",
            {
            name: "Project Name",
            description: "Project Description",
            due_date: "2025-07-01",
            },
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ id: "project-1" });
    });

    test("getTasksByProjectId sends GET request with correct URL and Authorization header", async () => {
        mockAxiosInstance.get.mockResolvedValue({ data: [{ id: "task-1" }] });

        const result = await getTasksByProjectId("jwt-token", "project-1");

        expect(mockAxiosInstance.get).toHaveBeenCalledWith(
            "/tasks/project/project-1",
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual([{ id: "task-1" }]);
    });

    test("updateTask sends PATCH request with correct fields and Authorization header", async () => {
        mockAxiosInstance.patch.mockResolvedValue({ data: { updated: true } });

        const updateData = {
            name: "New Task Name",
            description: "Updated desc",
            story_points: 5,
            status: "IN_PROGRESS",
            next: "task-2",
            isNextUpdated: true,
        };

        const result = await updateTask("jwt-token", "task-1", updateData);

        expect(mockAxiosInstance.patch).toHaveBeenCalledWith(
            "/tasks/task-1",
            {
            name: "New Task Name",
            description: "Updated desc",
            story_points: 5,
            status: "IN_PROGRESS",
            next: "task-2",
            isNextUpdated: true,
            },
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ updated: true });
    });

    test("createTask sends POST request with correct payload and Authorization header", async () => {
        mockAxiosInstance.post.mockResolvedValue({ data: { id: "task-1" } });

        const result = await createTask(
            "jwt-token",
            "project-1",
            "Task name",
            "Some task description",
            3
        );

        expect(mockAxiosInstance.post).toHaveBeenCalledWith(
            "/tasks",
            {
            project_id: "project-1",
            name: "Task name",
            description: "Some task description",
            story_points: 3,
            },
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ id: "task-1" });
    });

    test("deleteTask sends DELETE request with body and Authorization header", async () => {
        mockAxiosInstance.delete.mockResolvedValue({ data: { deleted: true } });

        const result = await deleteTask("jwt-token", "task-1", "project-1");

        expect(mockAxiosInstance.delete).toHaveBeenCalledWith(
            "/tasks",
            {
            data: {
                project_id: "project-1",
                task_id: "task-1",
            },
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ deleted: true });
    });

    test("getTaskAssignees sends POST request with correct payload and Authorization header", async () => {
        mockAxiosInstance.post.mockResolvedValue({ data: [{ user_id: "user-1" }] });

        const result = await getTaskAssignees("jwt-token", "project-1", "task-1");

        expect(mockAxiosInstance.post).toHaveBeenCalledWith(
            "tasks/assignments/task",
            {
            project_id: "project-1",
            task_id: "task-1",
            },
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual([{ user_id: "user-1" }]);
    });

    test("assignUserToTask sends POST request to correct endpoint with body and Authorization header", async () => {
        mockAxiosInstance.post.mockResolvedValue({ data: { assigned: true } });

        const result = await assignUserToTask("jwt-token", "user-1", "project-1", "task-1");

        expect(mockAxiosInstance.post).toHaveBeenCalledWith(
            "tasks/assignments/new/user-1",
            {
            project_id: "project-1",
            task_id: "task-1",
            },
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ assigned: true });
    });

    test("removeAssignment sends DELETE request with body and Authorization header", async () => {
        mockAxiosInstance.delete.mockResolvedValue({ data: { removed: true } });

        const result = await removeAssignment("jwt-token", "user-1", "project-1", "task-1");

        expect(mockAxiosInstance.delete).toHaveBeenCalledWith(
            "tasks/assignments/user-1",
            {
            data: {
                project_id: "project-1",
                task_id: "task-1",
            },
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ removed: true });
    });

    test("getProjectMembers sends GET request with correct URL and Authorization header", async () => {
        mockAxiosInstance.get.mockResolvedValue({ data: [{ user_id: "user-1" }] });

        const result = await getProjectMembers("jwt-token", "project-1");

        expect(mockAxiosInstance.get).toHaveBeenCalledWith(
            "projects/members/project-1",
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual([{ user_id: "user-1" }]);
    });

    test("getTaskDependencies sends GET request with correct URL and Authorization header", async () => {
        mockAxiosInstance.get.mockResolvedValue({ data: [{ depends_on: "task-2" }] });

        const result = await getTaskDependencies("jwt-token", "task-1");

        expect(mockAxiosInstance.get).toHaveBeenCalledWith(
            "tasks/dependency/task-1",
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual([{ depends_on: "task-2" }]);
    });

    test("addTaskDependency sends POST request with correct payload and Authorization header", async () => {
        mockAxiosInstance.post.mockResolvedValue({ data: { added: true } });

        const result = await addTaskDependency("jwt-token", "task-1", "dep-1");

        expect(mockAxiosInstance.post).toHaveBeenCalledWith(
            "tasks/dependency",
            {
            dependency_id: "dep-1",
            task_id: "task-1",
            },
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ added: true });
    });

    test("removeTaskDependency sends DELETE request with correct body and Authorization header", async () => {
        mockAxiosInstance.delete.mockResolvedValue({ data: { removed: true } });

        const result = await removeTaskDependency("jwt-token", "task-1", "dep-1");

        expect(mockAxiosInstance.delete).toHaveBeenCalledWith(
            "tasks/dependency",
            {
            data: {
                dependency_id: "dep-1",
                task_id: "task-1",
            },
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ removed: true });
    });

    test("getStatistics sends GET request with correct URL and Authorization header", async () => {
        mockAxiosInstance.get.mockResolvedValue({ data: { completedTasks: 5 } });

        const result = await getStatistics("jwt-token", "project-1");

        expect(mockAxiosInstance.get).toHaveBeenCalledWith(
            "projects/statistics/project-1",
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ completedTasks: 5 });
    });

    test("getFullProjectInfo sends GET request with correct URL and Authorization header", async () => {
        mockAxiosInstance.get.mockResolvedValue({ data: { id: "project-1", name: "Test Project" } });

        const result = await getFullProjectInfo("jwt-token", "project-1");

        expect(mockAxiosInstance.get).toHaveBeenCalledWith(
            "projects/info/project-1",
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ id: "project-1", name: "Test Project" });
    });

    test("updateProject sends PATCH request with correct body and Authorization header", async () => {
        mockAxiosInstance.patch.mockResolvedValue({ data: { updated: true } });

        const updateData = {
            name: "Updated Name",
            description: "Updated description",
            dueDate: "2025-07-01",
            availableHours: 120,
            sumExperience: 30,
            externalRiskProbability: 0.25,
        };

        const result = await updateProject("jwt-token", "project-1", updateData);

        expect(mockAxiosInstance.patch).toHaveBeenCalledWith(
            "projects/project-1",
            {
            name: "Updated Name",
            description: "Updated description",
            due_date: "2025-07-01",
            available_hours: 120,
            sum_experience: 30,
            external_risk_probability: 0.25,
            },
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ updated: true });
    });

    test("changeProjectOwner sends PATCH request with correct payload and Authorization header", async () => {
        mockAxiosInstance.patch.mockResolvedValue({ data: { changed: true } });

        const result = await changeProjectOwner("jwt-token", "user@example.com", "project-1");

        expect(mockAxiosInstance.patch).toHaveBeenCalledWith(
            "projects/owner",
            {
            user_email: "user@example.com",
            project_id: "project-1",
            },
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ changed: true });
    });

    test("removeProjectMember sends DELETE request with correct body and Authorization header", async () => {
        mockAxiosInstance.delete.mockResolvedValue({ data: { removed: true } });

        const result = await removeProjectMember("jwt-token", "user-1", "project-1");

        expect(mockAxiosInstance.delete).toHaveBeenCalledWith(
            "projects/members",
            {
            data: {
                user_id: "user-1",
                project_id: "project-1",
            },
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ removed: true });
    });

    test("addProjectMember sends POST request with correct body and Authorization header", async () => {
        mockAxiosInstance.post.mockResolvedValue({ data: { added: true } });

        const result = await addProjectMember("jwt-token", "user@example.com", "project-1");

        expect(mockAxiosInstance.post).toHaveBeenCalledWith(
            "projects/members",
            {
            user_email: "user@example.com",
            project_id: "project-1",
            },
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ added: true });
    });

    test("deleteProject sends DELETE request with correct URL and Authorization header", async () => {
        mockAxiosInstance.delete.mockResolvedValue({ data: { deleted: true } });

        const result = await deleteProject("jwt-token", "project-1");

        expect(mockAxiosInstance.delete).toHaveBeenCalledWith(
            "projects/project-1",
            {
            headers: {
                Authorization: "Bearer jwt-token",
            },
            }
        );
        expect(result).toEqual({ deleted: true });
    });

    test("registerUser throws error on failure", async () => {
        mockAxiosInstance.post.mockRejectedValue(new Error("Registration failed"));
        await expect(registerUser("test@test.com", "testuser", "pass123")).rejects.toThrow("Registration failed");
    });

    test("loginUser throws error on failure", async () => {
        mockAxiosInstance.post.mockRejectedValue(new Error("Login failed"));
        await expect(loginUser("test@test.com", "pass123")).rejects.toThrow("Login failed");
    });

    test("deleteUser throws error on failure", async () => {
        mockAxiosInstance.delete.mockRejectedValue(new Error("Delete failed"));
        await expect(deleteUser("jwt-token")).rejects.toThrow("Delete failed");
    });

    test("getProjectsByUserToken throws error on failure", async () => {
        mockAxiosInstance.get.mockRejectedValue(new Error("Get projects failed"));
        await expect(getProjectsByUserToken("jwt-token")).rejects.toThrow("Get projects failed");
    });

    test("createProject throws error on failure", async () => {
        mockAxiosInstance.post.mockRejectedValue(new Error("Create project failed"));
        await expect(createProject("jwt-token", "name", "desc", "2025-07-01")).rejects.toThrow("Create project failed");
    });

    test("getTasksByProjectId throws error on failure", async () => {
        mockAxiosInstance.get.mockRejectedValue(new Error("Get tasks failed"));
        await expect(getTasksByProjectId("jwt-token", "project-1")).rejects.toThrow("Get tasks failed");
    });

    test("updateTask throws error on failure", async () => {
        mockAxiosInstance.patch.mockRejectedValue(new Error("Update failed"));
        await expect(updateTask("jwt-token", "task-1", {})).rejects.toThrow("Update failed");
    });

    test("createTask throws error on failure", async () => {
        mockAxiosInstance.post.mockRejectedValue(new Error("Create task failed"));
        await expect(createTask("jwt-token", "project-1", "name", "desc", 5)).rejects.toThrow("Create task failed");
    });

    test("deleteTask throws error on failure", async () => {
        mockAxiosInstance.delete.mockRejectedValue(new Error("Delete task failed"));
        await expect(deleteTask("jwt-token", "task-1", "project-1")).rejects.toThrow("Delete task failed");
    });

    test("getTaskAssignees throws error on failure", async () => {
        mockAxiosInstance.post.mockRejectedValue(new Error("Get assignees failed"));
        await expect(getTaskAssignees("jwt-token", "project-1", "task-1")).rejects.toThrow("Get assignees failed");
    });

    test("assignUserToTask throws error on failure", async () => {
        mockAxiosInstance.post.mockRejectedValue(new Error("Assign user failed"));
        await expect(assignUserToTask("jwt-token", "user-1", "project-1", "task-1")).rejects.toThrow("Assign user failed");
    });

    test("removeAssignment throws error on failure", async () => {
        mockAxiosInstance.delete.mockRejectedValue(new Error("Remove assignment failed"));
        await expect(removeAssignment("jwt-token", "user-1", "project-1", "task-1")).rejects.toThrow("Remove assignment failed");
    });

    test("getProjectMembers throws error on failure", async () => {
        mockAxiosInstance.get.mockRejectedValue(new Error("Get members failed"));
        await expect(getProjectMembers("jwt-token", "project-1")).rejects.toThrow("Get members failed");
    });

    test("getTaskDependencies throws error on failure", async () => {
        mockAxiosInstance.get.mockRejectedValue(new Error("Get dependencies failed"));
        await expect(getTaskDependencies("jwt-token", "task-1")).rejects.toThrow("Get dependencies failed");
    });

    test("addTaskDependency throws error on failure", async () => {
        mockAxiosInstance.post.mockRejectedValue(new Error("Add dependency failed"));
        await expect(addTaskDependency("jwt-token", "task-1", "dep-1")).rejects.toThrow("Add dependency failed");
    });

    test("removeTaskDependency throws error on failure", async () => {
        mockAxiosInstance.delete.mockRejectedValue(new Error("Remove dependency failed"));
        await expect(removeTaskDependency("jwt-token", "task-1", "dep-1")).rejects.toThrow("Remove dependency failed");
    });

    test("getStatistics throws error on failure", async () => {
        mockAxiosInstance.get.mockRejectedValue(new Error("Get statistics failed"));
        await expect(getStatistics("jwt-token", "project-1")).rejects.toThrow("Get statistics failed");
    });

    test("getFullProjectInfo throws error on failure", async () => {
        mockAxiosInstance.get.mockRejectedValue(new Error("Get full info failed"));
        await expect(getFullProjectInfo("jwt-token", "project-1")).rejects.toThrow("Get full info failed");
    });

    test("updateProject throws error on failure", async () => {
        mockAxiosInstance.patch.mockRejectedValue(new Error("Update project failed"));
        await expect(updateProject("jwt-token", "project-1", {})).rejects.toThrow("Update project failed");
    });

    test("changeProjectOwner throws error on failure", async () => {
        mockAxiosInstance.patch.mockRejectedValue(new Error("Change owner failed"));
        await expect(changeProjectOwner("jwt-token", "user@email.com", "project-1")).rejects.toThrow("Change owner failed");
    });

    test("removeProjectMember throws error on failure", async () => {
        mockAxiosInstance.delete.mockRejectedValue(new Error("Remove member failed"));
        await expect(removeProjectMember("jwt-token", "user-1", "project-1")).rejects.toThrow("Remove member failed");
    });

    test("addProjectMember throws error on failure", async () => {
        mockAxiosInstance.post.mockRejectedValue(new Error("Add member failed"));
        await expect(addProjectMember("jwt-token", "user@email.com", "project-1")).rejects.toThrow("Add member failed");
    });

    test("deleteProject throws error on failure", async () => {
        mockAxiosInstance.delete.mockRejectedValue(new Error("Delete project failed"));
        await expect(deleteProject("jwt-token", "project-1")).rejects.toThrow("Delete project failed");
    });

});