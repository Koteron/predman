package com.predman.content.mapper;

import com.predman.content.common.TaskStatus;
import com.predman.content.dto.task.TaskCreationDto;
import com.predman.content.dto.task.TaskDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.Task;
import com.predman.content.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskMapperTest {

    @Mock
    ProjectService projectService;

    @InjectMocks
    TaskMapper taskMapper;

    UUID projectId;
    Project project;
    UUID taskId;
    Task nextTask;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        project = Project.builder().id(projectId).build();
        nextTask = Task.builder().id(UUID.randomUUID()).build();
    }

    @Test
    void convertToTaskDto_shouldMapTaskToDtoCorrectly_whenNextIsPresent() {
        Task task = Task.builder()
                .id(taskId)
                .name("Test Task")
                .description("Description")
                .status(TaskStatus.IN_PROGRESS)
                .next(nextTask)
                .storyPoints(5.0)
                .project(project)
                .build();

        TaskDto dto = taskMapper.convertToTaskDto(task);

        assertEquals(task.getId(), dto.id());
        assertEquals(task.getName(), dto.name());
        assertEquals(task.getDescription(), dto.description());
        assertEquals(task.getStatus(), dto.status());
        assertEquals(nextTask.getId(), dto.next());
        assertEquals(task.getStoryPoints(), dto.storyPoints());
        assertEquals(projectId, dto.projectId());
    }

    @Test
    void convertToTaskDto_shouldMapTaskToDtoCorrectly_whenNextIsNull() {
        Task task = Task.builder()
                .id(taskId)
                .name("Test Task")
                .description("Description")
                .status(TaskStatus.IN_PROGRESS)
                .next(null)
                .storyPoints(3.0)
                .project(project)
                .build();

        TaskDto dto = taskMapper.convertToTaskDto(task);

        assertEquals(task.getId(), dto.id());
        assertEquals(task.getName(), dto.name());
        assertEquals(task.getDescription(), dto.description());
        assertEquals(task.getStatus(), dto.status());
        assertNull(dto.next());
        assertEquals(task.getStoryPoints(), dto.storyPoints());
        assertEquals(projectId, dto.projectId());
    }

    @Test
    void convertToNewTaskEntity_shouldMapDtoToNewTaskEntityCorrectly() {
        TaskCreationDto dto = TaskCreationDto.builder()
                .storyPoints(8.0)
                .name("New Task")
                .description("Some desc")
                .projectId(projectId)
                .build();
        when(projectService.getEntityById(projectId)).thenReturn(project);

        Task task = taskMapper.convertToNewTaskEntity(dto, nextTask);

        assertEquals(dto.name(), task.getName());
        assertEquals(dto.description(), task.getDescription());
        assertEquals(dto.storyPoints(), task.getStoryPoints());
        assertEquals(TaskStatus.PLANNED, task.getStatus());
        assertEquals(project, task.getProject());
        assertEquals(nextTask, task.getNext());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());

        verify(projectService).getEntityById(projectId);
    }
}
