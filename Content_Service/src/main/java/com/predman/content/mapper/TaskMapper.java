package com.predman.content.mapper;

import com.predman.content.common.TaskStatus;
import com.predman.content.dto.task.TaskCreationDto;
import com.predman.content.dto.task.TaskDto;
import com.predman.content.entity.Task;
import com.predman.content.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TaskMapper {
    private final ProjectService projectService;

    public TaskDto convertToTaskDto(Task task) {
        return TaskDto
                .builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .status(task.getStatus())
                .storyPoints(task.getStoryPoints())
                .projectId(task.getProject().getId())
                .build();
    }

    public Task convertToNewTaskEntity(TaskCreationDto taskCreationDto) {
        return Task
                .builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .storyPoints(taskCreationDto.storyPoints())
                .description(taskCreationDto.description())
                .name(taskCreationDto.name())
                .status(TaskStatus.PLANNED)
                .project(projectService.getEntityById(taskCreationDto.projectId()))
                .build();
    }
}
