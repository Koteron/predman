package com.predman.content.service;

import com.predman.content.dto.task.TaskCreationDto;
import com.predman.content.dto.task.TaskDto;
import com.predman.content.dto.task.TaskUpdateDto;
import com.predman.content.entity.Task;
import com.predman.content.exception.NotFoundException;
import com.predman.content.mapper.TaskMapper;
import com.predman.content.repository.TaskRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    public final TaskRepository taskRepository;
    public final TaskMapper taskMapper;
    public final EntityManager entityManager;

    @Override
    public TaskDto create(TaskCreationDto taskCreationDto) {
        Task newTask = taskMapper.convertToNewTaskEntity(taskCreationDto);
        return taskMapper.convertToTaskDto(taskRepository.save(newTask));
    }

    @Override
    public TaskDto update(Task originalTask, TaskUpdateDto taskUpdateDto) {
        Task updatedTask = Task.builder()
                        .id(originalTask.getId())
                        .project(originalTask.getProject())
                        .name(taskUpdateDto.name() == null ? originalTask.getName() : taskUpdateDto.name())
                        .description(taskUpdateDto.description() == null ?
                                originalTask.getDescription() : taskUpdateDto.description())
                        .status(taskUpdateDto.status() == null ? originalTask.getStatus() : taskUpdateDto.status())
                        .storyPoints(taskUpdateDto.storyPoints() == null ?
                                originalTask.getStoryPoints() : taskUpdateDto.storyPoints())
                        .updatedAt(LocalDateTime.now())
                        .createdAt(originalTask.getCreatedAt())
                        .build();
        return taskMapper.convertToTaskDto(taskRepository.save(updatedTask));
    }

    @Override
    public TaskDto getById(UUID id) {
        return taskMapper.convertToTaskDto(taskRepository.findById(id).orElseThrow(()
                -> new NotFoundException("Task not found")));
    }

    @Override
    public Task getEntityById(UUID id) {
        Task task = entityManager.find(Task.class, id);
        if (task == null) {
            throw new NotFoundException("Task not found");
        }
        return task;
    }

    @Override
    public List<TaskDto> getAllByProjectId(UUID projectId) {
        return taskRepository.findByProjectId(projectId).stream().map(taskMapper::convertToTaskDto).toList();
    }

    @Override
    public void deleteById(UUID taskId) {
        taskRepository.deleteById(taskId);
    }
}
