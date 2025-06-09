package com.predman.content.controller;

import com.predman.content.common.ProjectMembershipUtil;
import com.predman.content.dto.project.*;
import com.predman.content.dto.project_member.ProjectMemberUpdateDto;
import com.predman.content.dto.project_member.ProjectMemberDto;
import com.predman.content.dto.project_statistics.ProjectStatisticsDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Project;
import com.predman.content.service.ProjectMemberService;
import com.predman.content.service.ProjectService;
import com.predman.content.service.ProjectStatisticsService;
import com.predman.content.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final UserService userService;
    private final ProjectStatisticsService projectStatisticsService;
    private final ProjectMembershipUtil projectMembershipUtil;

    @PostMapping
    public ProjectDto createProject(@Valid @RequestBody ProjectCreationDto projectCreationDto) {
        return projectService.create(projectCreationDto);
    }

    @GetMapping("/owner")
    public List<ProjectDto> getAllProjectsByOwnerId() {
        return projectService.getAllByOwnerId(userService.getAuthenticatedUser().getId());
    }

    @GetMapping("/info/{project-id}")
    public ProjectFullInfoDto getProjectFullInfoById(@PathVariable("project-id") UUID projectId) {
        projectMembershipUtil.checkSelfProjectMembership(projectId);
        return projectService.getFullInfoById(projectId);
    }

    @GetMapping("/{project-id}")
    public ProjectDto getProjectById(@PathVariable("project-id") UUID projectId) {
        projectMembershipUtil.checkSelfProjectMembership(projectId);
        return projectService.getById(projectId);
    }

    @GetMapping("/task_list/{project-id}")
    public ProjectTaskListDto getProjectWithTaskListById(@PathVariable("project-id") UUID projectId) {
        projectMembershipUtil.checkSelfProjectMembership(projectId);
        return projectService.getWithTaskListById(projectId);
    }

    @DeleteMapping("/{project-id}")
    public void deleteProject(@PathVariable("project-id") UUID projectId) {
        Project project = projectService.getEntityById(projectId);
        userService.checkAuthenticatedUser(project.getOwner().getId());
        projectService.delete(project);
    }

    @PatchMapping("/{project-id}")
    public ProjectFullInfoDto updateProject(@PathVariable("project-id") UUID projectId,
                                            @RequestBody ProjectUpdateDto projectUpdateDto) {
        Project project = projectService.getEntityById(projectId);
        projectMembershipUtil.checkSelfProjectMembership(projectId);
        return projectService.update(project, projectUpdateDto);
    }

    @PostMapping("/members")
    public UserDto addMemberToProject(@RequestBody ProjectMemberUpdateDto projectMemberUpdateDto) {
        Project project = projectService.getEntityById(projectMemberUpdateDto.projectId());
        userService.checkAuthenticatedUser(project.getOwner().getId());
        return projectMemberService.addProjectMemberStatUpdate(
                userService.getEntityByEmail(projectMemberUpdateDto.userEmail()), project);
    }

    @DeleteMapping("/members")
    public void removeUserFromProject(@RequestBody @Valid ProjectMemberDto projectMemberDto) {
        Project project = projectService.getEntityById(projectMemberDto.projectId());
        if (!userService.getAuthenticatedUser().getId().equals(projectMemberDto.userId())) {
            userService.checkAuthenticatedUser(project.getOwner().getId());
        }
        projectMemberService.removeProjectMember(projectMemberDto);
    }

    @GetMapping("/members/{project-id}")
    public List<UserDto> getAllUsersByProjectId(@PathVariable("project-id") UUID projectId) {
        projectMembershipUtil.checkSelfProjectMembership(projectId);
        return projectMemberService.getUsersByProjectId(projectId);
    }

    @PatchMapping("/owner")
    public ProjectDto changeOwner(@RequestBody ProjectMemberUpdateDto projectMemberUpdateDto) {
        Project project = projectService.getEntityById(projectMemberUpdateDto.projectId());
        userService.checkAuthenticatedUser(project.getOwner().getId());
        return projectService.changeOwnerChecked(project, projectMemberUpdateDto.userEmail());
    }

    @GetMapping("/user")
    public List<ProjectDto> getAllProjectsByUserId() {
        return projectMemberService.getProjectsByUserId(userService.getAuthenticatedUser().getId());
    }

    @GetMapping("/statistics/{project-id}")
    public List<ProjectStatisticsDto> getProjectStatistics(@PathVariable("project-id") UUID projectId) {
        projectMembershipUtil.checkSelfProjectMembership(projectId);
        return projectStatisticsService.getProjectStatisticsByProjectId(projectId);
    }
}
