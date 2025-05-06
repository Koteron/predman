package com.predman.content.service;

import com.predman.content.dto.task.TaskDto;
import com.predman.content.dto.task_assignment.TaskAssignmentDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.TaskAssignment;
import com.predman.content.entity.User;
import com.predman.content.exception.NotFoundException;
import com.predman.content.mapper.TaskMapper;
import com.predman.content.mapper.UserMapper;
import com.predman.content.repository.TaskAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskAssignmentServiceImpl implements TaskAssignmentService {
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final TaskService taskService;

    @Override
    public List<UserDto> getAllAssigneesByTaskId(UUID taskId) {
        return taskAssignmentRepository.findAllByTask_Id(taskId).stream().map(taskAssignment ->
                userMapper.convertToUserDto(taskAssignment.getUser())).toList();
    }

    @Override
    public List<TaskDto> getAllTasksByUserId(UUID userId) {
        return taskAssignmentRepository.findAllByUser_Id(userId).stream().map(taskAssignment ->
                taskMapper.convertToTaskDto(taskAssignment.getTask())).toList();
    }

    @Override
    public TaskAssignmentDto assignTask(UUID taskId, User user) {
        TaskAssignment newEntry = TaskAssignment.builder()
                .joinedAt(LocalDateTime.now())
                .task(taskService.getEntityById(taskId))
                .user(user)
                .build();
        taskAssignmentRepository.save(newEntry);
        return TaskAssignmentDto
                .builder()
                .taskId(taskId)
                .userId(user.getId())
                .build();
    }

    @Override
    public void removeAssignment(UUID taskId, User user) {
        TaskAssignment entry = taskAssignmentRepository.findAllByTask_Id(taskId).stream().filter(taskAssignment ->
                taskAssignment.getUser().getId().equals(user.getId())).findAny().orElseThrow(() ->
                new NotFoundException("User is not assigned to this task"));
        taskAssignmentRepository.deleteById(entry.getId());
    }
}
