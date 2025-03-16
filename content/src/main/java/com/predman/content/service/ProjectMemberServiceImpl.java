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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;

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
    public ProjectMemberDto addProjectMember(User fetchedUser, Project fetchedProject)
    {
        if (fetchedProject == null)
        {
            throw new NotFoundException("Project cannot be null");
        }
        if (fetchedUser == null)
        {
            throw new NotFoundException("User cannot be null");
        }

        ProjectMember projectMember = projectMemberRepository.save(ProjectMember
                .builder()
                .user(fetchedUser)
                .project(fetchedProject)
                .joinedAt(LocalDateTime.now())
                .build());

        return ProjectMemberDto
                .builder()
                .projectId(projectMember.getProject().getId())
                .userId(projectMember.getUser().getId())
                .build();
    }

    @Override
    @Transactional
    public void removeProjectMember(ProjectMemberDto projectMemberDto)
    {
        ProjectMember entry = projectMemberRepository.findAllByUser_Id(projectMemberDto.userId())
                .stream().filter((ProjectMember projectMember) ->
                        projectMember.getProject().getId() == projectMemberDto.projectId()).findAny()
                .orElseThrow(() -> new NotFoundException("User is not a part of this project!"));
        projectMemberRepository.deleteById(entry.getId());
    }


    @Override
    public List<ProjectMember> findAllByProjectIds(List<UUID> projectIds)
    {
        return projectMemberRepository.findAllByProjectIds(projectIds);
    }
}
