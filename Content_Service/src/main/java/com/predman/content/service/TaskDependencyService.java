package com.predman.content.service;

import com.predman.content.dto.task_dependency.TaskDependencyDto;

import java.util.List;
import java.util.UUID;

public interface TaskDependencyService {
    List<TaskDependencyDto> getTaskDependencies(UUID taskId);
    List<TaskDependencyDto> getAllProjectDependencies(UUID projectId);
    TaskDependencyDto createTaskDependency(TaskDependencyDto taskDependencyDto);
    void deleteTaskDependency(TaskDependencyDto taskDependencyDto);
}
