package com.predman.content.service;

import com.predman.content.dto.task.SortedTasksDto;
import com.predman.content.dto.task.TaskCreationDto;
import com.predman.content.dto.task.TaskDto;
import com.predman.content.dto.task.TaskUpdateDto;
import com.predman.content.entity.Task;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskDto create(TaskCreationDto taskCreationDto);
    void deleteById(UUID id);
    TaskDto update(Task task, TaskUpdateDto taskUpdateDto);
    TaskDto getById(UUID id);
    Task getEntityById(UUID id);
    List<TaskDto> getAllByProjectId(UUID projectId);
    SortedTasksDto getSortedAllByProjectId(UUID projectId);
}
