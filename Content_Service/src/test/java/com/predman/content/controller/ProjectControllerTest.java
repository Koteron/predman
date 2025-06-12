package com.predman.content.controller;

import com.predman.content.common.ProjectMembershipUtil;
import com.predman.content.dto.project.*;
import com.predman.content.dto.project_member.ProjectMemberDto;
import com.predman.content.dto.project_member.ProjectMemberUpdateDto;
import com.predman.content.dto.project_statistics.ProjectStatisticsDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.User;
import com.predman.content.service.ProjectMemberService;
import com.predman.content.service.ProjectService;
import com.predman.content.service.ProjectStatisticsService;
import com.predman.content.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {
    @Mock private ProjectService projectService;
    @Mock private ProjectMemberService projectMemberService;
    @Mock private UserService userService;
    @Mock private ProjectStatisticsService projectStatisticsService;
    @Mock private ProjectMembershipUtil projectMembershipUtil;

    @InjectMocks
    private ProjectController projectController;

    private final UUID projectId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void createProject_shouldReturnCreatedProject() {
        ProjectCreationDto creationDto = ProjectCreationDto
                .builder()
                .name("Test Project")
                .description("desc")
                .build();
        ProjectDto expected = ProjectDto.builder()
                .id(projectId)
                .name("Test Project")
                .description("desc")
                .build();

        when(projectService.create(creationDto)).thenReturn(expected);

        ProjectDto result = projectController.createProject(creationDto);

        assertEquals(expected, result);
    }

    @Test
    void getAllProjectsByOwnerId_shouldReturnList() {
        User user = User.builder().id(userId).build();
        when(userService.getAuthenticatedUser()).thenReturn(user);

        List<ProjectDto> projects = List.of(ProjectDto.builder()
                .id(projectId)
                .name("Test Project")
                .description("desc")
                .build());
        when(projectService.getAllByOwnerId(userId)).thenReturn(projects);

        List<ProjectDto> result = projectController.getAllProjectsByOwnerId();

        assertEquals(projects, result);
    }

    @Test
    void getProjectFullInfoById_shouldCheckMembershipAndReturnInfo() {
        ProjectFullInfoDto dto = ProjectFullInfoDto.builder()
                .id(projectId)
                .name("Full Info")
                .build();
        doNothing().when(projectMembershipUtil).checkSelfProjectMembership(projectId);
        when(projectService.getFullInfoById(projectId)).thenReturn(dto);

        ProjectFullInfoDto result = projectController.getProjectFullInfoById(projectId);

        assertEquals(dto, result);
    }

    @Test
    void deleteProject_shouldCallDeleteIfOwner() {
        Project project = Project.builder().id(projectId).owner(User.builder().id(userId).build()).build();
        when(projectService.getEntityById(projectId)).thenReturn(project);
        doNothing().when(userService).checkAuthenticatedUser(userId);
        doNothing().when(projectService).delete(project);

        projectController.deleteProject(projectId);

        verify(projectService).delete(project);
    }

    @Test
    void updateProject_shouldUpdateProjectInfo() {
        Project project = Project.builder().id(projectId).build();
        ProjectUpdateDto updateDto = ProjectUpdateDto.builder()
                .name("new name")
                .description("new desc")
                .build();
        ProjectFullInfoDto fullInfo = ProjectFullInfoDto.builder()
                .id(projectId)
                .name("new name")
                .description("new desc")
                .build();

        when(projectService.getEntityById(projectId)).thenReturn(project);
        doNothing().when(projectMembershipUtil).checkSelfProjectMembership(projectId);
        when(projectService.update(project, updateDto)).thenReturn(fullInfo);

        ProjectFullInfoDto result = projectController.updateProject(projectId, updateDto);

        assertEquals(fullInfo, result);
    }

    @Test
    void addMemberToProject_shouldReturnAddedUser() {
        ProjectMemberUpdateDto dto = ProjectMemberUpdateDto.builder()
                .projectId(projectId)
                .userEmail("test@example.com")
                .build();
        Project project = Project.builder().id(projectId).owner(User.builder().id(userId).build()).build();
        User user = User.builder().id(userId).build();
        UserDto userDto = new UserDto(userId, "test@example.com", "name");

        when(projectService.getEntityById(projectId)).thenReturn(project);
        doNothing().when(userService).checkAuthenticatedUser(userId);
        when(userService.getEntityByEmail(dto.userEmail())).thenReturn(user);
        when(projectMemberService.addProjectMemberStatUpdate(user, project)).thenReturn(userDto);

        UserDto result = projectController.addMemberToProject(dto);

        assertEquals(userDto, result);
    }

    @Test
    void removeUserFromProject_sameUser_shouldNotCallCheckAuthenticatedUser() {
        UUID memberId = userId; // same as the authenticated user
        ProjectMemberDto dto = new ProjectMemberDto(memberId, projectId);
        Project project = Project.builder().id(projectId).owner(User.builder().id(UUID.randomUUID()).build()).build();

        when(projectService.getEntityById(projectId)).thenReturn(project);
        when(userService.getAuthenticatedUser()).thenReturn(User.builder().id(memberId).build());
        doNothing().when(projectMemberService).removeProjectMember(dto);

        projectController.removeUserFromProject(dto);

        verify(projectService).getEntityById(projectId);
        verify(userService).getAuthenticatedUser();
        verify(projectMemberService).removeProjectMember(dto);
        verify(userService, never()).checkAuthenticatedUser(any());
    }


    @Test
    void removeUserFromProject_differentUser_shouldCallCheckAuthenticatedUser() {
        UUID memberId = UUID.randomUUID(); // different from authenticated user
        ProjectMemberDto dto = new ProjectMemberDto(memberId, projectId);
        UUID ownerId = UUID.randomUUID();
        Project project = Project.builder().id(projectId).owner(User.builder().id(ownerId).build()).build();

        when(projectService.getEntityById(projectId)).thenReturn(project);
        when(userService.getAuthenticatedUser()).thenReturn(User.builder().id(userId).build());
        doNothing().when(userService).checkAuthenticatedUser(ownerId);
        doNothing().when(projectMemberService).removeProjectMember(dto);

        projectController.removeUserFromProject(dto);

        verify(projectService).getEntityById(projectId);
        verify(userService).getAuthenticatedUser();
        verify(userService).checkAuthenticatedUser(ownerId);
        verify(projectMemberService).removeProjectMember(dto);
    }


    @Test
    void getAllUsersByProjectId_shouldCheckMembershipAndReturnUsers() {
        doNothing().when(projectMembershipUtil).checkSelfProjectMembership(projectId);
        List<UserDto> users = List.of(new UserDto(userId, "email", "name"));
        when(projectMemberService.getUsersByProjectId(projectId)).thenReturn(users);

        List<UserDto> result = projectController.getAllUsersByProjectId(projectId);

        assertEquals(users, result);
    }

    @Test
    void changeOwner_shouldUpdateOwner() {
        Project project = Project.builder().id(projectId).owner(User.builder().id(userId).build()).build();
        ProjectMemberUpdateDto dto = ProjectMemberUpdateDto.builder()
                .projectId(projectId)
                .userEmail("newowner@example.com")
                .build();
        ProjectDto updatedProject = ProjectDto.builder()
                .id(projectId)
                .name("Updated")
                .description("Desc")
                .build();

        when(projectService.getEntityById(projectId)).thenReturn(project);
        doNothing().when(userService).checkAuthenticatedUser(userId);
        when(projectService.changeOwnerChecked(project, dto.userEmail())).thenReturn(updatedProject);

        ProjectDto result = projectController.changeOwner(dto);

        assertEquals(updatedProject, result);
    }

    @Test
    void getAllProjectsByUserId_shouldReturnUserProjects() {
        when(userService.getAuthenticatedUser()).thenReturn(User.builder().id(userId).build());
        List<ProjectDto> projects = List.of(ProjectDto.builder()
                .id(projectId)
                .name("name")
                .description("desc")
                .build());
        when(projectMemberService.getProjectsByUserId(userId)).thenReturn(projects);

        List<ProjectDto> result = projectController.getAllProjectsByUserId();

        assertEquals(projects, result);
    }

    @Test
    void getProjectStatistics_shouldReturnStats() {
        doNothing().when(projectMembershipUtil).checkSelfProjectMembership(projectId);
        List<ProjectStatisticsDto> stats = List.of(ProjectStatisticsDto.builder()
                .build());
        when(projectStatisticsService.getProjectStatisticsByProjectId(projectId)).thenReturn(stats);

        List<ProjectStatisticsDto> result = projectController.getProjectStatistics(projectId);

        assertEquals(stats, result);
    }

    @Test
    void getProjectById_shouldCheckMembershipAndReturnProject() {
        doNothing().when(projectMembershipUtil).checkSelfProjectMembership(projectId);
        ProjectDto expected = ProjectDto.builder()
                .id(projectId)
                .name("Test Project")
                .description("Description")
                .build();

        when(projectService.getById(projectId)).thenReturn(expected);

        ProjectDto result = projectController.getProjectById(projectId);

        verify(projectMembershipUtil).checkSelfProjectMembership(projectId);
        verify(projectService).getById(projectId);
        assertEquals(expected, result);
    }

    @Test
    void getProjectWithTaskListById_shouldCheckMembershipAndReturnProjectWithTasks() {
        doNothing().when(projectMembershipUtil).checkSelfProjectMembership(projectId);
        ProjectTaskListDto expected = ProjectTaskListDto.builder()
                .id(projectId)
                .tasks(Collections.emptyList())
                .name("Test Project")
                .build();

        when(projectService.getWithTaskListById(projectId)).thenReturn(expected);

        ProjectTaskListDto result = projectController.getProjectWithTaskListById(projectId);

        verify(projectMembershipUtil).checkSelfProjectMembership(projectId);
        verify(projectService).getWithTaskListById(projectId);
        assertEquals(expected, result);
    }


}