package com.predman.content.service;

import com.predman.content.dto.project_member.ProjectMemberDto;
import com.predman.content.dto.project.ProjectDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.ProjectMember;
import com.predman.content.entity.User;
import com.predman.content.exception.NotFoundException;
import com.predman.content.mapper.ProjectMapper;
import com.predman.content.mapper.UserMapper;
import com.predman.content.repository.ProjectMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceImplTest {
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private UserMapper userMapper;
    @Mock private ProjectMapper projectMapper;
    @Mock private ProjectService projectService;
    @Mock private ProjectStatisticsService projectStatisticsService;

    @InjectMocks
    private ProjectMemberServiceImpl projectMemberService;

    @Test
    void addProjectMemberStatUpdate_shouldAddMemberAndUpdateStats() {
        UUID projectId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(projectId).build();
        UserDto userDto = UserDto.builder().id(user.getId()).build();

        when(userMapper.convertToUserDto(user)).thenReturn(userDto);

        UserDto result = projectMemberService.addProjectMemberStatUpdate(user, project);

        verify(projectMemberRepository).save(argThat(member ->
                member.getUser().equals(user) &&
                        member.getProject().equals(project)
        ));
        verify(projectStatisticsService).updateStatistics(projectId);
        assertEquals(userDto, result);
    }

    @Test
    void removeProjectMember_shouldDeleteAndUpdateStatistics_whenNotOwner() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Project project = Project.builder()
                .id(projectId)
                .owner(User.builder().id(UUID.randomUUID()).build()) // другой владелец
                .build();

        ProjectMember member = ProjectMember.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(userId).build())
                .project(project)
                .build();

        when(projectService.getEntityById(projectId)).thenReturn(project);
        when(projectMemberRepository.findAllByUser_Id(userId)).thenReturn(List.of(member));
        when(projectMemberRepository.findAllByProject_Id(projectId)).thenReturn(List.of(member));

        projectMemberService.removeProjectMember(new ProjectMemberDto(userId, projectId));

        verify(projectMemberRepository).deleteById(member.getId());
        verify(projectStatisticsService).updateStatistics(projectId);
    }

    @Test
    void getUsersByProjectId_returnsUserDtoList() {
        UUID projectId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).build();
        ProjectMember member = ProjectMember.builder().user(user).build();
        when(projectMemberRepository.findAllByProject_Id(projectId)).thenReturn(List.of(member));
        UserDto userDto = UserDto.builder().id(user.getId()).build();
        when(userMapper.convertToUserDto(user)).thenReturn(userDto);

        List<UserDto> result = projectMemberService.getUsersByProjectId(projectId);

        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).id());
    }

    @Test
    void getProjectsByUserId_returnsProjectDtoList() {
        UUID userId = UUID.randomUUID();
        Project project = Project.builder().id(UUID.randomUUID()).build();
        ProjectMember member = ProjectMember.builder().project(project).build();
        when(projectMemberRepository.findAllByUser_Id(userId)).thenReturn(List.of(member));
        ProjectDto dto = ProjectDto.builder().id(project.getId()).build();
        when(projectMapper.convertToProjectDto(project)).thenReturn(dto);

        List<ProjectDto> result = projectMemberService.getProjectsByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(project.getId(), result.get(0).id());
    }

    @Test
    void addProjectMemberStatUpdate_addsMemberAndUpdatesStats() {
        User user = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(UUID.randomUUID()).build();
        UserDto userDto = UserDto.builder().id(user.getId()).build();

        when(userMapper.convertToUserDto(user)).thenReturn(userDto);

        UserDto result = projectMemberService.addProjectMemberStatUpdate(user, project);

        verify(projectMemberRepository).save(any(ProjectMember.class));
        verify(projectStatisticsService).updateStatistics(project.getId());
        assertEquals(user.getId(), result.id());
    }

    @Test
    void addProjectMember_throwsNotFound_whenUserNull() {
        Project project = Project.builder().id(UUID.randomUUID()).build();
        assertThrows(NotFoundException.class, () -> projectMemberService.addProjectMember(null, project));
    }

    @Test
    void addProjectMember_throwsNotFound_whenProjectNull() {
        User user = User.builder().id(UUID.randomUUID()).build();
        assertThrows(NotFoundException.class, () -> projectMemberService.addProjectMember(user, null));
    }

    @Test
    void addProjectMember_savesAndReturnsUserDto() {
        User user = User.builder().id(UUID.randomUUID()).build();
        Project project = Project.builder().id(UUID.randomUUID()).build();
        UserDto userDto = UserDto.builder().id(user.getId()).build();

        when(userMapper.convertToUserDto(user)).thenReturn(userDto);

        UserDto result = projectMemberService.addProjectMember(user, project);

        verify(projectMemberRepository).save(any(ProjectMember.class));
        assertEquals(user.getId(), result.id());
    }

    @Test
    void removeProjectMember_removesNonOwnerAndUpdatesStats() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder().id(projectId).owner(User.builder().id(UUID.randomUUID()).build()).build();
        ProjectMember member = ProjectMember.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(userId).build())
                .project(project)
                .build();

        when(projectService.getEntityById(projectId)).thenReturn(project);
        when(projectMemberRepository.findAllByUser_Id(userId)).thenReturn(List.of(member));
        when(projectMemberRepository.findAllByProject_Id(projectId)).thenReturn(List.of(member));

        projectMemberService.removeProjectMember(new ProjectMemberDto(userId, projectId));

        verify(projectMemberRepository, times(1)).deleteById(member.getId());
        verify(projectStatisticsService).updateStatistics(projectId);
    }

    @Test
    void removeProjectMember_deletesProjectIfEmpty() {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder().id(projectId).owner(User.builder().id(UUID.randomUUID()).build()).build();
        ProjectMember member = ProjectMember.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(userId).build())
                .project(project)
                .build();

        when(projectService.getEntityById(projectId)).thenReturn(project);
        when(projectMemberRepository.findAllByUser_Id(userId)).thenReturn(List.of(member));
        when(projectMemberRepository.findAllByProject_Id(projectId)).thenReturn(Collections.emptyList());
        when(projectService.getEntityById(projectId)).thenReturn(project);

        projectMemberService.removeProjectMember(new ProjectMemberDto(userId, projectId));

        verify(projectMemberRepository).deleteById(member.getId());
        verify(projectService).delete(project);
    }

    @Test
    void deleteAllByProjectId_callsRepository() {
        UUID id = UUID.randomUUID();
        projectMemberService.deleteAllByProjectId(id);
        verify(projectMemberRepository).deleteAllByProject_Id(id);
    }

    @Test
    void deleteAllByUserId_callsRepository() {
        UUID id = UUID.randomUUID();
        projectMemberService.deleteAllByUserId(id);
        verify(projectMemberRepository).deleteAllByUser_Id(id);
    }

    @Test
    void findAllByProjectIds_returnsCorrectList() {
        UUID projectId = UUID.randomUUID();
        ProjectMember member = new ProjectMember();
        when(projectMemberRepository.findAllByProjectIds(List.of(projectId))).thenReturn(List.of(member));

        List<ProjectMember> result = projectMemberService.findAllByProjectIds(List.of(projectId));

        assertEquals(1, result.size());
        assertSame(member, result.getFirst());
    }
}