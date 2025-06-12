package com.predman.content.service;

import com.predman.content.common.TaskStatus;
import com.predman.content.dto.task.SortedTasksDto;
import com.predman.content.dto.task.TaskCreationDto;
import com.predman.content.dto.task.TaskDto;
import com.predman.content.dto.task.TaskUpdateDto;
import com.predman.content.entity.Task;
import com.predman.content.exception.ForbiddenException;
import com.predman.content.mapper.TaskMapper;
import com.predman.content.repository.TaskRepository;
import com.predman.content.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {
    @Mock public TaskRepository taskRepository;
    @Mock public TaskMapper taskMapper;
    @Mock public EntityManager entityManager;

    @InjectMocks
    TaskServiceImpl taskService;

    @Test
    void create_withNextNotPlanned_throwsForbidden() {
        UUID someId = UUID.randomUUID();
        TaskCreationDto dto = TaskCreationDto.builder()
                .next(someId)
                .name("name")
                .description("desc")
                .build();

        Task nextTask = Task.builder().id(someId).status(TaskStatus.IN_PROGRESS).build();
        when(taskRepository.findById(someId)).thenReturn(Optional.of(nextTask));
        when(taskRepository.findByNext(nextTask)).thenReturn(List.of());

        assertThrows(ForbiddenException.class, () -> taskService.create(dto));
    }

    @Test
    void create_withPrev_setsPrevNext_andSavesBoth() {
        UUID nextId = UUID.randomUUID(), prevId = UUID.randomUUID();
        TaskCreationDto dto = TaskCreationDto.builder()
                .name("name")
                .description("desc")
                .next(nextId)
                .build();

        Task nextPlanned = Task.builder().id(nextId).status(TaskStatus.PLANNED).build();
        Task prev = Task.builder().id(prevId).next(nextPlanned).status(TaskStatus.PLANNED).build();
        when(taskRepository.findById(nextId)).thenReturn(Optional.of(nextPlanned));
        when(taskRepository.findByNext(nextPlanned)).thenReturn(List.of(prev));

        Task newTask = Task.builder().id(UUID.randomUUID()).build();
        when(taskMapper.convertToNewTaskEntity(dto, nextPlanned)).thenReturn(newTask);
        when(taskRepository.save(prev)).thenReturn(prev);
        when(taskRepository.save(newTask)).thenReturn(newTask);

        TaskDto dtoResult = TaskDto.builder().id(newTask.getId()).build();
        when(taskMapper.convertToTaskDto(newTask)).thenReturn(dtoResult);

        TaskDto result = taskService.create(dto);
        assertEquals(newTask.getId(), result.id());

        verify(taskRepository).save(prev);
        verify(taskRepository).save(newTask);
    }

    @Test
    void update_changesNext_chainIsUpdatedAndSaved() {
        Task current = Task.builder().id(UUID.randomUUID()).status(TaskStatus.PLANNED).build();
        Task newNext = Task.builder().id(UUID.randomUUID()).status(TaskStatus.PLANNED).build();
        Task prev = Task.builder().id(UUID.randomUUID()).next(current).status(TaskStatus.PLANNED).build();

        TaskUpdateDto dto = TaskUpdateDto
                .builder()
                .isNextUpdated(true)
                .next(newNext.getId())
                .build();
        when(taskRepository.findByNext(current)).thenReturn(List.of(prev));
        when(taskRepository.findById(newNext.getId())).thenReturn(Optional.of(newNext));
        when(taskRepository.findByNext(newNext)).thenReturn(List.of());

        Task updated = Task.builder().id(current.getId()).build();
        when(taskMapper.convertToTaskDto(any())).thenReturn(TaskDto.builder().id(updated.getId()).build());
        when(taskRepository.save(prev)).thenReturn(prev);
        when(taskRepository.save(argThat(task -> task.getId().equals(current.getId())))).thenReturn(updated);

        TaskDto result = taskService.update(current, dto);
        assertEquals(current.getId(), result.id());

        verify(taskRepository).save(prev);
        verify(taskRepository).save(argThat(task -> task.getId().equals(current.getId())));
    }

    @Test
    void deleteById_relinksPrev_thenDeletes() {
        UUID id = UUID.randomUUID(), prevId = UUID.randomUUID(), nextId = UUID.randomUUID();
        Task next = Task.builder().id(nextId).build();
        Task current = Task.builder().id(id).status(TaskStatus.PLANNED).next(next).build();
        Task prev = Task.builder().id(prevId).next(current).status(current.getStatus()).build();

        when(taskRepository.findById(id)).thenReturn(Optional.of(current));
        when(taskRepository.findByNext(current)).thenReturn(List.of(prev));

        when(entityManager.merge(next)).thenReturn(next);
        when(taskRepository.save(prev)).thenReturn(prev);

        taskService.deleteById(id);

        verify(taskRepository).deleteById(id);
        verify(taskRepository).save(prev);
    }

    @Test
    void getEntityById_existing_returnsTask() {
        UUID id = UUID.randomUUID();
        Task t = new Task(); t.setId(id);
        when(entityManager.find(Task.class, id)).thenReturn(t);

        Task result = taskService.getEntityById(id);
        assertEquals(t, result);
    }

    @Test
    void getEntityById_nonExisting_throws() {
        UUID testUUID = UUID.randomUUID();
        when(entityManager.find(Task.class, testUUID)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> taskService.getEntityById(testUUID));
    }

    @Test
    void getSortedAllByProjectId_returnsSortedTasks() {
        when(taskMapper.convertToTaskDto(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task task = invocation.getArgument(0);
                    return TaskDto.builder().id(task.getId()).build();
                });
        UUID projectId = UUID.randomUUID();

        Task task1 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.PLANNED).build();
        Task task2 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.IN_PROGRESS).build();
        Task task3 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.PLANNED).build();
        Task task4 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.COMPLETED).build();
        Task task5 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.IN_PROGRESS).build();
        Task task6 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.COMPLETED).build();
        Task task7 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.COMPLETED).build();
        Task task8 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.IN_PROGRESS).build();
        Task task9 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.PLANNED).build();

        task1.setNext(task3);
        task2.setNext(task5);
        task3.setNext(task9);
        task4.setNext(task6);
        task5.setNext(task8);
        task6.setNext(task7);

        List<Task> testTaskList = List.of(
            task1, task2, task3, task4, task5, task6, task7, task8, task9
        );
        SortedTasksDto sortedTestTaskList = SortedTasksDto.builder()
                .planned(testTaskList.stream().filter((task -> task.getStatus().equals(TaskStatus.PLANNED)))
                        .map(taskMapper::convertToTaskDto).toList())
                .inprogress(testTaskList.stream().filter((task -> task.getStatus().equals(TaskStatus.IN_PROGRESS)))
                        .map(taskMapper::convertToTaskDto).toList())
                .completed(testTaskList.stream().filter((task -> task.getStatus().equals(TaskStatus.COMPLETED)))
                        .map(taskMapper::convertToTaskDto).toList())
                .build();

        when(taskRepository.findByProjectId(projectId)).thenReturn(testTaskList);

        assertEquals(sortedTestTaskList, taskService.getSortedAllByProjectId(projectId));
    }

    @Test
    void getSortedAllByProjectId_throwsExceptionsOnCycles() {
        UUID projectId = UUID.randomUUID();

        Task task1 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.PLANNED).build();
        Task task2 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.IN_PROGRESS).build();
        Task task3 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.PLANNED).build();
        Task task4 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.COMPLETED).build();
        Task task5 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.IN_PROGRESS).build();
        Task task6 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.COMPLETED).build();
        Task task7 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.COMPLETED).build();
        Task task8 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.IN_PROGRESS).build();
        Task task9 = Task.builder().id(UUID.randomUUID()).status(TaskStatus.PLANNED).build();

        task1.setNext(task3);
        task2.setNext(task5);
        task3.setNext(task9);
        task4.setNext(task6);
        task5.setNext(task8);
        task6.setNext(task7);

        List<Task> testTaskList = List.of(
                task1, task2, task3, task4, task5, task6, task7, task8, task9
        );

        when(taskRepository.findByProjectId(projectId)).thenReturn(testTaskList);

        task7.setNext(task4);
        assertThrows(IllegalStateException.class, () -> taskService.getSortedAllByProjectId(projectId));
        task7.setNext(null);

        task8.setNext(task2);
        assertThrows(IllegalStateException.class, () -> taskService.getSortedAllByProjectId(projectId));
        task8.setNext(null);

        task9.setNext(task1);
        assertThrows(IllegalStateException.class, () -> taskService.getSortedAllByProjectId(projectId));
    }

    @Test
    void update_withNullFields_retainsOriginalValues() {
        Task original = Task.builder()
                .id(UUID.randomUUID())
                .name("Original Name")
                .description("Original Desc")
                .status(TaskStatus.PLANNED)
                .storyPoints(5.0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        TaskUpdateDto dto = TaskUpdateDto.builder()
                .isNextUpdated(false)
                .build();

        Task saved = Task.builder().id(original.getId()).build();
        when(taskRepository.save(any())).thenReturn(saved);
        when(taskMapper.convertToTaskDto(saved)).thenReturn(TaskDto.builder().id(saved.getId()).build());

        TaskDto result = taskService.update(original, dto);
        assertEquals(original.getId(), result.id());
    }

    @Test
    void update_isNextUpdatedAndPrevIsNull_doesNotThrow() {
        Task original = Task.builder()
                .id(UUID.randomUUID())
                .status(TaskStatus.PLANNED)
                .build();

        Task newNext = Task.builder().id(UUID.randomUUID()).status(TaskStatus.PLANNED).build();
        when(taskRepository.findByNext(original)).thenReturn(List.of());
        when(taskRepository.findById(newNext.getId())).thenReturn(Optional.of(newNext));
        when(taskRepository.findByNext(newNext)).thenReturn(List.of());

        TaskUpdateDto dto = TaskUpdateDto.builder()
                .isNextUpdated(true)
                .next(newNext.getId())
                .build();

        Task saved = Task.builder().id(original.getId()).build();
        when(taskRepository.save(any())).thenReturn(saved);
        when(taskMapper.convertToTaskDto(saved)).thenReturn(TaskDto.builder().id(saved.getId()).build());

        TaskDto result = taskService.update(original, dto);
        assertEquals(original.getId(), result.id());
    }

    @Test
    void update_isNextUpdated_statusMismatch_throwsForbidden() {
        Task original = Task.builder()
                .id(UUID.randomUUID())
                .status(TaskStatus.PLANNED)
                .build();

        Task newNext = Task.builder()
                .id(UUID.randomUUID())
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskRepository.findByNext(original)).thenReturn(List.of());
        when(taskRepository.findById(newNext.getId())).thenReturn(Optional.of(newNext));
        when(taskRepository.findByNext(newNext)).thenReturn(List.of());

        TaskUpdateDto dto = TaskUpdateDto.builder()
                .isNextUpdated(true)
                .next(newNext.getId())
                .status(TaskStatus.PLANNED) // mismatch with newNext
                .build();

        assertThrows(ForbiddenException.class, () -> taskService.update(original, dto));
    }

    @Test
    void update_isNextUpdated_andNewPrevExists_savesPrev() {
        Task original = Task.builder().id(UUID.randomUUID()).status(TaskStatus.PLANNED).build();
        Task newNext = Task.builder().id(UUID.randomUUID()).status(TaskStatus.PLANNED).build();
        Task newPrev = Task.builder().id(UUID.randomUUID()).next(newNext).status(TaskStatus.PLANNED).build();

        when(taskRepository.findByNext(original)).thenReturn(List.of());
        when(taskRepository.findById(newNext.getId())).thenReturn(Optional.of(newNext));
        when(taskRepository.findByNext(newNext)).thenReturn(List.of(newPrev));


        TaskUpdateDto dto = TaskUpdateDto.builder()
                .isNextUpdated(true)
                .next(newNext.getId())
                .build();

        Task saved = Task.builder().id(original.getId()).build();
        when(taskRepository.save(any())).thenReturn(saved);
        when(taskMapper.convertToTaskDto(saved)).thenReturn(TaskDto.builder().id(saved.getId()).build());

        TaskDto result = taskService.update(original, dto);
        assertEquals(saved.getId(), result.id());

        verify(taskRepository).save(newPrev);
    }

    @Test
    void getById_existing_returnsDto() {
        UUID id = UUID.randomUUID();
        Task entity = Task.builder().id(id).build();
        TaskDto dto = TaskDto.builder().id(id).build();

        when(taskRepository.findById(id)).thenReturn(Optional.of(entity));
        when(taskMapper.convertToTaskDto(entity)).thenReturn(dto);

        assertEquals(dto, taskService.getById(id));
    }

    @Test
    void getById_missing_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.getById(id));
    }

    @Test
    void getAllByProjectId_returnsMappedDtos() {
        UUID projectId = UUID.randomUUID();
        Task task1 = Task.builder().id(UUID.randomUUID()).build();
        Task task2 = Task.builder().id(UUID.randomUUID()).build();

        TaskDto dto1 = TaskDto.builder().id(task1.getId()).build();
        TaskDto dto2 = TaskDto.builder().id(task2.getId()).build();

        when(taskRepository.findByProjectId(projectId)).thenReturn(List.of(task1, task2));
        when(taskMapper.convertToTaskDto(task1)).thenReturn(dto1);
        when(taskMapper.convertToTaskDto(task2)).thenReturn(dto2);

        List<TaskDto> result = taskService.getAllByProjectId(projectId);
        assertEquals(List.of(dto1, dto2), result);
    }

    @Test
    void create_withoutNext_insertsWithoutChainChanges() {
        TaskCreationDto dto = TaskCreationDto.builder()
                .name("New Task")
                .description("A task without a next")
                .build();

        Task newTask = Task.builder().id(UUID.randomUUID()).build();
        when(taskMapper.convertToNewTaskEntity(eq(dto), isNull())).thenReturn(newTask);
        when(taskRepository.save(newTask)).thenReturn(newTask);
        when(taskMapper.convertToTaskDto(newTask)).thenReturn(TaskDto.builder().id(newTask.getId()).build());

        TaskDto result = taskService.create(dto);

        assertEquals(newTask.getId(), result.id());
        verify(taskRepository).save(newTask);
    }

    @Test
    void deleteById_taskNotFound_throws() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.deleteById(id));
    }

}