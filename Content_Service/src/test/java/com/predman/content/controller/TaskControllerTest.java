package com.predman.content.controller;

import com.predman.content.common.ProjectMembershipUtil;
import com.predman.content.common.TaskStatus;
import com.predman.content.dto.task.*;
import com.predman.content.dto.task_dependency.TaskDependencyDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.Task;
import com.predman.content.entity.User;
import com.predman.content.service.TaskAssignmentService;
import com.predman.content.service.TaskDependencyService;
import com.predman.content.service.TaskService;
import com.predman.content.service.UserService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {
    @Mock private TaskService taskService;
    @Mock private UserService userService;
    @Mock private ProjectMembershipUtil projectMembershipUtil;
    @Mock private TaskAssignmentService taskAssignmentService;
    @Mock private TaskDependencyService taskDependencyService;

    @InjectMocks
    private TaskController taskController;

    @Test
    void createTask_shouldCallMembershipCheckAndReturnCreatedTask() {
        UUID projectId = UUID.randomUUID();
        TaskCreationDto creationDto = TaskCreationDto
                .builder()
                .projectId(projectId)
                .name("Task name")
                .description("desc")
                .storyPoints(3.0)
                .build();
        TaskDto expectedDto = TaskDto
                .builder()
                .projectId(projectId)
                .name("Task name")
                .description("desc")
                .storyPoints(3.0)
                .status(TaskStatus.PLANNED)
                .build();

        when(taskService.create(creationDto)).thenReturn(expectedDto);

        TaskDto result = taskController.createTask(creationDto);

        verify(projectMembershipUtil).checkSelfProjectMembership(projectId);
        assertEquals(expectedDto, result);
    }

    @Test
    void deleteTask_shouldCheckMembershipAndDeleteTask() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        TaskProjectPairDto pair = new TaskProjectPairDto(taskId, projectId);

        taskController.deleteTask(pair);

        verify(projectMembershipUtil).checkSelfProjectMembership(projectId);
        verify(taskService).deleteById(taskId);
    }

    @Test
    void getTask_shouldReturnFullInfoDto() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TaskDto taskDto = TaskDto
                .builder()
                .projectId(projectId)
                .name("Task name")
                .description("desc")
                .storyPoints(3.0)
                .build();
        List<TaskDependencyDto> deps = List.of(new TaskDependencyDto(taskId, UUID.randomUUID()));

        when(taskService.getById(taskId)).thenReturn(taskDto);
        when(taskDependencyService.getTaskDependencies(taskId)).thenReturn(deps);

        TaskFullInfoDto result = taskController.getTask(taskId);

        verify(projectMembershipUtil).checkSelfProjectMembership(projectId);
        assertEquals("Task name", result.name());
        assertEquals(3.0, result.storyPoints());
        assertEquals(1, result.dependencies().size());
    }

    @Test
    void getAllTasksByProjectId_shouldCheckMembershipAndReturnSorted() {
        UUID projectId = UUID.randomUUID();
        SortedTasksDto sortedTasksDto =
                new SortedTasksDto(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        when(taskService.getSortedAllByProjectId(projectId)).thenReturn(sortedTasksDto);

        SortedTasksDto result = taskController.getAllTasksByProjectId(projectId);

        verify(projectMembershipUtil).checkSelfProjectMembership(projectId);
        assertSame(sortedTasksDto, result);
    }

    @Test
    void updateTask_shouldReturnUpdatedTask() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Task task = Task.builder().status(TaskStatus.IN_PROGRESS).build();
        task.setProject(Project.builder().id(projectId).build());
        TaskUpdateDto updateDto = TaskUpdateDto
                .builder()
                .isNextUpdated(false)
                .storyPoints(4.0)
                .name("Updated name")
                .description("desc")
                .status(TaskStatus.COMPLETED)
                .build();
        TaskDto updated = TaskDto
                .builder()
                .storyPoints(4.0)
                .name("Updated name")
                .description("desc")
                .status(TaskStatus.COMPLETED)
                .build();

        when(taskService.getEntityById(taskId)).thenReturn(task);
        when(taskService.update(task, updateDto)).thenReturn(updated);

        TaskDto result = taskController.updateTask(taskId, updateDto);

        verify(projectMembershipUtil).checkSelfProjectMembership(projectId);
        assertEquals(updated, result);
    }

    @Test
    void getAllAssigneesByTaskId_shouldReturnList() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TaskProjectPairDto pair = new TaskProjectPairDto(taskId, projectId);
        List<UserDto> users = List.of(UserDto.builder().id(UUID.randomUUID()).build());

        when(taskAssignmentService.getAllAssigneesByTaskId(taskId)).thenReturn(users);

        List<UserDto> result = taskController.getAllAssigneesByTaskId(pair);

        verify(projectMembershipUtil).checkSelfProjectMembership(projectId);
        assertEquals(users, result);
    }

    @Test
    void getAllTasksByUserId_shouldCheckBothMemberships() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        TaskProjectPairDto pair = new TaskProjectPairDto(taskId, projectId);
        List<TaskDto> taskDtos = List.of(TaskDto.builder().status(TaskStatus.PLANNED).build());

        when(taskAssignmentService.getAllTasksByUserId(userId)).thenReturn(taskDtos);

        List<TaskDto> result = taskController.getAllTasksByUserId(userId, pair);

        verify(projectMembershipUtil).checkSelfProjectMembership(projectId);
        verify(projectMembershipUtil).checkProjectMembership(userId, projectId);
        assertEquals(taskDtos, result);
    }

    @Test
    void assignUserToTask_shouldAssignAndReturnUserDto() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        TaskProjectPairDto pair = new TaskProjectPairDto(taskId, projectId);
        UserDto expected = UserDto.builder().id(userId).build();

        when(userService.getEntityById(userId)).thenReturn(user);
        when(taskAssignmentService.assignTask(taskId, user)).thenReturn(expected);

        UserDto result = taskController.assignUserToTask(userId, pair);

        verify(projectMembershipUtil).checkSelfProjectMembership(projectId);
        verify(projectMembershipUtil).checkProjectMembership(userId, projectId);
        assertEquals(expected, result);
    }

    @Test
    void removeAssignment_shouldRemoveCorrectly() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        TaskProjectPairDto pair = new TaskProjectPairDto(taskId, projectId);

        when(userService.getEntityById(userId)).thenReturn(user);

        taskController.removeAssignment(userId, pair);

        verify(taskAssignmentService).removeAssignment(taskId, user);
    }

    @Test
    void addDependency_shouldCallService() {
        TaskDependencyDto dto = new TaskDependencyDto(UUID.randomUUID(), UUID.randomUUID());

        when(taskDependencyService.createTaskDependency(dto)).thenReturn(dto);

        TaskDependencyDto result = taskController.addDependency(dto);

        assertEquals(dto, result);
    }

    @Test
    void deleteDependency_shouldCallService() {
        TaskDependencyDto dto = new TaskDependencyDto(UUID.randomUUID(), UUID.randomUUID());

        taskController.deleteDependency(dto);

        verify(taskDependencyService).deleteTaskDependency(dto);
    }

    @Test
    void getTaskDependencies_shouldReturnList() {
        UUID taskId = UUID.randomUUID();
        UUID dependencyId = UUID.randomUUID();
        List<TaskDependencyDto> deps = List.of(TaskDependencyDto
                .builder()
                .dependencyId(dependencyId)
                .taskId(taskId)
                .build());

        when(taskDependencyService.getTaskDependencies(taskId)).thenReturn(deps);

        List<TaskDependencyDto> result = taskController.getTaskDependencies(taskId);

        assertEquals(deps, result);
    }
}