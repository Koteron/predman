package com.predman.content.service;

import com.predman.content.dto.task_dependency.TaskDependencyDto;
import com.predman.content.entity.TaskDependency;
import com.predman.content.exception.ForbiddenException;
import com.predman.content.exception.NotFoundException;
import com.predman.content.repository.TaskDependencyRepository;
import com.predman.content.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskDependencyServiceImpl implements TaskDependencyService {
    private final TaskDependencyRepository taskDependencyRepository;
    private final TaskRepository taskRepository;

    public List<TaskDependencyDto> getTaskDependencies(UUID taskId) {
        return taskDependencyRepository.findAllByTaskId(taskId)
                .stream().map(taskDependency -> TaskDependencyDto
                .builder()
                .taskId(taskDependency.getTask().getId())
                .dependencyId(taskDependency.getTask().getId())
                .build()).toList();
    }

    public List<TaskDependencyDto> getAllProjectDependencies(UUID projectId) {
        return taskDependencyRepository.findAllByProjectId(projectId)
                .stream().map(taskDependency -> TaskDependencyDto
                .builder()
                .taskId(taskDependency.getTask().getId())
                .dependencyId(taskDependency.getTask().getId())
                .build()).toList();
    }

    public TaskDependencyDto createTaskDependency(TaskDependencyDto taskDependencyDto) {
        if (taskDependencyDto.dependencyId().equals(taskDependencyDto.taskId())) {
            throw new ForbiddenException("Task dependency with id " + taskDependencyDto.taskId() + " already exists");
        }
        taskDependencyRepository.save(TaskDependency
                .builder()
                .dependency(taskRepository.findById(taskDependencyDto.dependencyId())
                        .orElseThrow(() -> new NotFoundException("Dependency task not found!")))
                .task(taskRepository.findById(taskDependencyDto.taskId())
                        .orElseThrow(() -> new NotFoundException("Dependency task not found!")))
                .build());
        return taskDependencyDto;
    }

    public void deleteTaskDependency(TaskDependencyDto taskDependencyDto) {
        taskDependencyRepository.deleteByIdPair(taskDependencyDto.taskId(), taskDependencyDto.dependencyId());
    }
}
