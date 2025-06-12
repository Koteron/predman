package com.predman.content.service;

import com.predman.content.dto.project.ProjectDto;
import com.predman.content.dto.project_member.ProjectMemberDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.ProjectMember;
import com.predman.content.entity.User;
import com.predman.content.exception.NotFoundException;
import com.predman.content.mapper.ProjectMapper;
import com.predman.content.mapper.UserMapper;
import com.predman.content.repository.ProjectMemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final ProjectService projectService;
    private final ProjectStatisticsService projectStatisticsService;

    public ProjectMemberServiceImpl(ProjectMemberRepository projectMemberRepository,
                                    @Lazy UserService userService,
                                    ProjectMapper projectMapper,
                                    ProjectService projectService,
                                    UserMapper userMapper,
                                    @Lazy ProjectStatisticsService projectStatisticsService) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectMapper = projectMapper;
        this.projectService = projectService;
        this.userMapper = userMapper;
        this.projectStatisticsService = projectStatisticsService;
    }

    @Override
    public List<UserDto> getUsersByProjectId(UUID projectId)
    {
        List<ProjectMember> projectMembers = projectMemberRepository.findAllByProject_Id(projectId);
        return projectMembers.stream().map((ProjectMember projectMember) ->
                userMapper.convertToUserDto(projectMember.getUser())).toList();
    }

    @Override
    public List<ProjectDto> getProjectsByUserId(UUID userId)
    {
        List<ProjectMember> projectMembers = projectMemberRepository.findAllByUser_Id(userId);
        return projectMembers.stream().map((ProjectMember projectMember) ->
                projectMapper.convertToProjectDto(projectMember.getProject())).toList();
    }

    @Override
    @Transactional
    public UserDto addProjectMemberStatUpdate(User fetchedUser, Project fetchedProject)
    {
        UserDto userDto = addProjectMember(fetchedUser, fetchedProject);
        projectStatisticsService.updateStatistics(fetchedProject.getId());
        return userDto;
    }

    @Override
    @Transactional
    public UserDto addProjectMember(User fetchedUser, Project fetchedProject)
    {
        if (fetchedProject == null)
        {
            throw new NotFoundException("Project cannot be null");
        }
        if (fetchedUser == null)
        {
            throw new NotFoundException("User cannot be null");
        }

        projectMemberRepository.save(ProjectMember
                .builder()
                .user(fetchedUser)
                .project(fetchedProject)
                .joinedAt(LocalDateTime.now())
                .build());

        return userMapper.convertToUserDto(fetchedUser);
    }

    @Override
    @Transactional
    public void removeProjectMember(ProjectMemberDto projectMemberDto)
    {
        Project project = projectService.getEntityById(projectMemberDto.projectId());
        ProjectMember entry = projectMemberRepository.findAllByUser_Id(projectMemberDto.userId())
                .stream().filter((ProjectMember projectMember) ->
                        project.getId() == projectMemberDto.projectId()).findAny()
                .orElseThrow(() -> new NotFoundException("User is not a part of this project!"));
        User owner = project.getOwner();
        if (owner.getId().equals(projectMemberDto.userId()))
        {
            projectService.changeOwnerRandom(owner.getId(), project.getId());
            projectMemberRepository.deleteById(entry.getId());
        }
        projectMemberRepository.deleteById(entry.getId());
        if (projectMemberRepository.findAllByProject_Id(projectMemberDto.projectId()).isEmpty()) {
            projectService.delete(projectService.getEntityById(projectMemberDto.projectId()));
        }
        else {
            projectStatisticsService.updateStatistics(project.getId());
        }
    }

    @Override
    public void deleteAllByProjectId (UUID projectId) {
        projectMemberRepository.deleteAllByProject_Id(projectId);
    }

    @Override
    public void deleteAllByUserId (UUID userId) {
        projectMemberRepository.deleteAllByUser_Id(userId);
    }

    @Override
    public List<ProjectMember> findAllByProjectIds(List<UUID> projectIds)
    {
        return projectMemberRepository.findAllByProjectIds(projectIds);
    }
}
