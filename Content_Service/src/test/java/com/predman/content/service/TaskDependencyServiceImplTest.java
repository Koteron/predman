package com.predman.content.service;

import com.predman.content.dto.task_dependency.TaskDependencyDto;
import com.predman.content.entity.Task;
import com.predman.content.entity.TaskDependency;
import com.predman.content.exception.ForbiddenException;
import com.predman.content.exception.NotFoundException;
import com.predman.content.repository.TaskDependencyRepository;
import com.predman.content.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskDependencyServiceImplTest {
    @Mock private TaskDependencyRepository taskDependencyRepository;
    @Mock private TaskRepository taskRepository;

    @InjectMocks
    private TaskDependencyServiceImpl taskDependencyService;

    @Test
    void getTaskDependencies_shouldReturnListOfDtos() {
        UUID taskId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Task task = Task.builder().id(taskId).build();
        Task dependency = Task.builder().id(depId).build();
        TaskDependency td = TaskDependency.builder().task(task).dependency(dependency).build();

        when(taskDependencyRepository.findAllByTaskId(taskId)).thenReturn(List.of(td));

        List<TaskDependencyDto> result = taskDependencyService.getTaskDependencies(taskId);

        assertEquals(1, result.size());
        assertEquals(taskId, result.getFirst().taskId());
        assertEquals(depId, result.getFirst().dependencyId());
    }

    @Test
    void getAllProjectDependencies_shouldReturnListOfDtos() {
        UUID taskId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        Task task = Task.builder().id(taskId).build();
        Task dependency = Task.builder().id(depId).build();
        TaskDependency td = TaskDependency.builder().task(task).dependency(dependency).build();

        when(taskDependencyRepository.findAllByProjectId(projectId)).thenReturn(List.of(td));

        List<TaskDependencyDto> result = taskDependencyService.getAllProjectDependencies(projectId);

        assertEquals(1, result.size());
        assertEquals(taskId, result.getFirst().taskId());
        assertEquals(depId, result.getFirst().dependencyId());
    }

    @Test
    void createTaskDependency_shouldCreateSuccessfully() {
        UUID taskId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Task task = Task.builder().id(taskId).build();
        Task dependency = Task.builder().id(depId).build();
        TaskDependencyDto dto = new TaskDependencyDto(taskId, depId);

        when(taskRepository.findById(depId)).thenReturn(Optional.of(dependency));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        TaskDependency saved = TaskDependency.builder().task(task).dependency(dependency).build();

        when(taskDependencyRepository.save(any())).thenReturn(saved);

        TaskDependencyDto result = taskDependencyService.createTaskDependency(dto);

        assertEquals(dto, result);
    }

    @Test
    void createTaskDependency_shouldThrowIfSameId() {
        UUID id = UUID.randomUUID();
        TaskDependencyDto dto = new TaskDependencyDto(id, id);

        ForbiddenException ex = assertThrows(ForbiddenException.class, () ->
                taskDependencyService.createTaskDependency(dto));

        assertTrue(ex.getMessage().contains("cannot be dependent on itself"));
        verify(taskDependencyRepository, never()).save(any());
    }

    @Test
    void createTaskDependency_shouldThrowIfDependencyNotFound() {
        UUID taskId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();
        TaskDependencyDto dto = new TaskDependencyDto(taskId, depId);

        when(taskRepository.findById(depId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                taskDependencyService.createTaskDependency(dto));

        verify(taskRepository, never()).findById(taskId);
        verify(taskDependencyRepository, never()).save(any());
    }

    @Test
    void createTaskDependency_shouldThrowIfTaskNotFound() {
        UUID taskId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();
        Task dependency = Task.builder().id(depId).build();
        TaskDependencyDto dto = new TaskDependencyDto(taskId, depId);

        when(taskRepository.findById(depId)).thenReturn(Optional.of(dependency));
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                taskDependencyService.createTaskDependency(dto));

        verify(taskDependencyRepository, never()).save(any());
    }

    @Test
    void deleteTaskDependency_shouldCallRepository() {
        UUID taskId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();
        TaskDependencyDto dto = new TaskDependencyDto(taskId, depId);

        taskDependencyService.deleteTaskDependency(dto);

        verify(taskDependencyRepository).deleteByIdPair(taskId, depId);
    }
}