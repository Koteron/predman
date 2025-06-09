package com.predman.content.service;

import com.predman.content.dto.task.TaskDto;
import com.predman.content.dto.task_assignment.TaskAssignmentDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.User;

import java.util.List;
import java.util.UUID;

public interface TaskAssignmentService {
    List<UserDto> getAllAssigneesByTaskId(UUID taskId);
    List<TaskDto> getAllTasksByUserId(UUID userId);
    UserDto assignTask(UUID taskId, User user);
    void removeAssignment(UUID taskId, User user);
}
