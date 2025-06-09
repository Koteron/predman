package com.predman.content.controller;

import com.predman.content.common.ProjectMembershipUtil;
import com.predman.content.dto.task.*;
import com.predman.content.dto.task_assignment.TaskAssignmentDto;
import com.predman.content.dto.task_dependency.TaskDependencyDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Task;
import com.predman.content.entity.User;
import com.predman.content.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final UserService userService;
    private final ProjectMembershipUtil projectMembershipUtil;
    private final TaskAssignmentService taskAssignmentService;
    private final TaskDependencyService taskDependencyService;

    @PostMapping
    public TaskDto createTask(@RequestBody @Valid TaskCreationDto taskCreationDto) {
        projectMembershipUtil.checkSelfProjectMembership(taskCreationDto.projectId());
        return taskService.create(taskCreationDto);
    }

    @DeleteMapping
    public void deleteTask(@RequestBody @Valid TaskProjectPairDto taskProjectPairDto) {
        projectMembershipUtil.checkSelfProjectMembership(taskProjectPairDto.projectId());
        taskService.deleteById(taskProjectPairDto.taskId());
    }

    @GetMapping("/{task-id}")
    public TaskFullInfoDto getTask(@PathVariable("task-id") UUID taskId) {
        TaskDto taskDto = taskService.getById(taskId);
        projectMembershipUtil.checkSelfProjectMembership(taskDto.projectId());
        return TaskFullInfoDto
                .builder()
                .name(taskDto.name())
                .description(taskDto.description())
                .status(taskDto.status())
                .storyPoints(taskDto.storyPoints())
                .dependencies(taskDependencyService.getTaskDependencies(taskId)
                        .stream().map(TaskDependencyDto::dependencyId).toList())
                .build();
    }

    @GetMapping("/project/{project-id}")
    public SortedTasksDto getAllTasksByProjectId(@PathVariable("project-id") UUID projectId) {
        projectMembershipUtil.checkSelfProjectMembership(projectId);
        return taskService.getSortedAllByProjectId(projectId);
    }

    @PatchMapping("/{task-id}")
    public TaskDto updateTask(@PathVariable("task-id") UUID taskId, @RequestBody @Valid TaskUpdateDto taskUpdateDto)
    {
        Task task = taskService.getEntityById(taskId);
        projectMembershipUtil.checkSelfProjectMembership(task.getProject().getId());
        return taskService.update(task, taskUpdateDto);
    }

    @PostMapping("/assignments/task")
    public List<UserDto> getAllAssigneesByTaskId(@RequestBody @Valid TaskProjectPairDto taskProjectPairDto) {
        projectMembershipUtil.checkSelfProjectMembership(taskProjectPairDto.projectId());
        return taskAssignmentService.getAllAssigneesByTaskId(taskProjectPairDto.taskId());
    }

    @PostMapping("/assignments/{user-id}")
    public List<TaskDto> getAllTasksByUserId(@PathVariable("user-id") UUID userId,
                                             @RequestBody @Valid TaskProjectPairDto taskProjectPairDto) {
        projectMembershipUtil.checkSelfProjectMembership(taskProjectPairDto.projectId());
        projectMembershipUtil.checkProjectMembership(userId, taskProjectPairDto.projectId());
        return taskAssignmentService.getAllTasksByUserId(userId);
    }

    @PostMapping("/assignments/new/{user-id}")
    public UserDto assignUserToTask(@PathVariable("user-id") UUID userId,
                                              @RequestBody @Valid TaskProjectPairDto taskProjectPairDto) {
        User user = userService.getEntityById(userId);
        projectMembershipUtil.checkSelfProjectMembership(taskProjectPairDto.projectId());
        projectMembershipUtil.checkProjectMembership(user.getId(), taskProjectPairDto.projectId());
        return taskAssignmentService.assignTask(taskProjectPairDto.taskId(), user);
    }

    @DeleteMapping("/assignments/{user-id}")
    public void removeAssignment(@PathVariable("user-id") UUID userId,
                                 @RequestBody @Valid TaskProjectPairDto taskProjectPairDto) {
        User user = userService.getEntityById(userId);
        projectMembershipUtil.checkSelfProjectMembership(taskProjectPairDto.projectId());
        projectMembershipUtil.checkProjectMembership(user.getId(), taskProjectPairDto.projectId());
        taskAssignmentService.removeAssignment(taskProjectPairDto.taskId(), user);
    }

    @PostMapping("/dependency")
    public TaskDependencyDto addDependency(@RequestBody @Valid TaskDependencyDto taskDependencyDto) {
        return taskDependencyService.createTaskDependency(taskDependencyDto);
    }

    @DeleteMapping("/dependency")
    public void deleteDependency(@RequestBody @Valid TaskDependencyDto taskDependencyDto) {
        taskDependencyService.deleteTaskDependency(taskDependencyDto);
    }

    @GetMapping("/dependency/{task-id}")
    public List<TaskDependencyDto> getTaskDependencies(@PathVariable("task-id") UUID taskId) {
        return taskDependencyService.getTaskDependencies(taskId);
    }
}
