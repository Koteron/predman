package com.predman.content.service;

import com.predman.content.dto.task.TaskDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Task;
import com.predman.content.entity.TaskAssignment;
import com.predman.content.entity.User;
import com.predman.content.exception.NotFoundException;
import com.predman.content.mapper.TaskMapper;
import com.predman.content.mapper.UserMapper;
import com.predman.content.repository.TaskAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskAssignmentServiceImplTest {
    @Mock private TaskAssignmentRepository taskAssignmentRepository;
    @Mock private UserMapper userMapper;
    @Mock private TaskMapper taskMapper;
    @Mock private TaskService taskService;

    @InjectMocks
    private TaskAssignmentServiceImpl taskAssignmentService;

    private final UUID taskId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID assignmentId = UUID.randomUUID();

    @Test
    void getAllAssigneesByTaskId_shouldReturnUserDtos() {
        User user = User.builder().id(userId).email("email").login("login").build();
        TaskAssignment assignment = TaskAssignment.builder().user(user).build();
        UserDto userDto = new UserDto(userId, "email", "login");

        when(taskAssignmentRepository.findAllByTask_Id(taskId)).thenReturn(List.of(assignment));
        when(userMapper.convertToUserDto(user)).thenReturn(userDto);

        List<UserDto> result = taskAssignmentService.getAllAssigneesByTaskId(taskId);

        assertEquals(List.of(userDto), result);
        verify(taskAssignmentRepository).findAllByTask_Id(taskId);
        verify(userMapper).convertToUserDto(user);
    }

    @Test
    void getAllTasksByUserId_shouldReturnTaskDtos() {
        TaskDto taskDto = TaskDto.builder().id(taskId).name("Task").build();
        TaskAssignment assignment = TaskAssignment.builder().task(
                Task.builder().id(taskId).name("Task").build()).build();

        when(taskAssignmentRepository.findAllByUser_Id(userId)).thenReturn(List.of(assignment));
        when(taskMapper.convertToTaskDto(any())).thenReturn(taskDto);

        List<TaskDto> result = taskAssignmentService.getAllTasksByUserId(userId);

        assertEquals(List.of(taskDto), result);
        verify(taskAssignmentRepository).findAllByUser_Id(userId);
        verify(taskMapper).convertToTaskDto(assignment.getTask());
    }

    @Test
    void assignTask_shouldSaveAssignmentAndReturnUserDto() {
        User user = User.builder().id(userId).email("email").login("login").build();
        TaskDto taskDto = TaskDto.builder().id(taskId).name("Task").build();

        when(taskService.getEntityById(taskId)).thenReturn(Task.builder().id(taskId).name("Task").build());

        UserDto result = taskAssignmentService.assignTask(taskId, user);

        assertEquals(userId, result.id());
        assertEquals("email", result.email());
        assertEquals("login", result.login());
        verify(taskAssignmentRepository).save(any(TaskAssignment.class));
    }

    @Test
    void removeAssignment_shouldDeleteAssignmentIfExists() {
        User user = User.builder().id(userId).build();
        TaskAssignment assignment = TaskAssignment.builder()
                .id(assignmentId)
                .user(user)
                .build();

        when(taskAssignmentRepository.findAllByTask_Id(taskId)).thenReturn(List.of(assignment));

        taskAssignmentService.removeAssignment(taskId, user);

        verify(taskAssignmentRepository).deleteById(assignmentId);
    }

    @Test
    void removeAssignment_shouldThrowIfAssignmentNotFound() {
        User user = User.builder().id(userId).build();
        when(taskAssignmentRepository.findAllByTask_Id(taskId)).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> taskAssignmentService.removeAssignment(taskId, user));
        verify(taskAssignmentRepository, never()).deleteById(any());
    }
}
