package com.predman.content.service;

import com.predman.content.dto.project.ProjectCreationDto;
import com.predman.content.dto.project.ProjectDto;
import com.predman.content.dto.project.ProjectUpdateDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.User;
import com.predman.content.exception.ForbiddenException;
import com.predman.content.exception.NotFoundException;
import com.predman.content.mapper.ProjectMapper;
import com.predman.content.repository.ProjectRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectMemberService projectMemberService;
    private final EntityManager entityManager;
    private final UserService userService;
    private final ProjectStatisticsService projectStatisticsService;

    ProjectServiceImpl(@Lazy ProjectMemberService projectMemberService,
                       @Lazy UserService userService,
                       ProjectRepository projectRepository,
                       ProjectMapper projectMapper,
                       EntityManager entityManager,
                       @Lazy ProjectStatisticsService projectStatisticsService) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.projectMemberService = projectMemberService;
        this.entityManager = entityManager;
        this.userService = userService;
        this.projectStatisticsService = projectStatisticsService;
    }

    @Override
    @Transactional
    public ProjectDto create(ProjectCreationDto projectCreationDto) {
        Project project = projectRepository.save(Project
                .builder()
                .name(projectCreationDto.name())
                .description(projectCreationDto.description())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .dueDate(projectCreationDto.dueDate())
                .owner(userService.getAuthenticatedUser())
                .build());
        projectMemberService.addProjectMember(userService.getAuthenticatedUser(), project);
        projectStatisticsService.initializeStatistics(project);
        return projectMapper.convertToProjectDto(project);
    }

    @Override
    public ProjectDto update(Project project, ProjectUpdateDto projectUpdateDto) {
        if (projectUpdateDto.name() != null && projectUpdateDto.name().isEmpty())
        {
            throw new ForbiddenException("Project name cannot be empty");
        }
        Project updatedProject = Project.builder()
                .id(project.getId())
                .description(projectUpdateDto.description() == null ? project.getDescription()
                        : projectUpdateDto.description())
                .name(projectUpdateDto.name() == null ? project.getName()
                        : projectUpdateDto.name())
                .dueDate(projectUpdateDto.dueDate() == null ? project.getDueDate()
                        : projectUpdateDto.dueDate())
                .owner(project.getOwner())
                .updatedDate(LocalDateTime.now())
                .createdDate(project.getCreatedDate())
                .build();
        return projectMapper.convertToProjectDto(projectRepository.save(updatedProject));
    }

    @Override
    public ProjectDto getById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project owner not found!"));
        return projectMapper.convertToProjectDto(project);
    }

    @Override
    public Project getEntityById(UUID id) {
        Project project = entityManager.find(Project.class, id);
        if (project == null) {
            throw new NotFoundException("Project not found!");
        }
        return project;
    }

    @Override
    public List<ProjectDto> getAllByOwnerId(UUID ownerId) {
        return projectRepository.findByOwner_Id(ownerId)
                .stream().map(projectMapper::convertToProjectDto).toList();
    }

    @Override
    public List<Project> getAllEntitiesByOwnerId(UUID ownerId) {
        return projectRepository.findByOwner_Id(ownerId);
    }

    @Override
    public void delete(Project project) {
        projectRepository.deleteById(project.getId());
    }

    @Override
    public ProjectDto changeOwnerUnchecked(Project project, User newOwner) {
        project.setOwner(newOwner);
        return projectMapper.convertToProjectDto(projectRepository.save(project));
    }

    @Override
    public ProjectDto changeOwnerChecked(Project project, String newOwnerEmail) {
        User user = userService.getEntityByEmail(newOwnerEmail);
        if (projectMemberService.getUsersByProjectId(project.getId()).stream().noneMatch((UserDto userDto) ->
                userDto.id() == user.getId()))
        {
            throw new NotFoundException("User is not a part of this project!");
        }
        return changeOwnerUnchecked(project, userService.getEntityByEmail(newOwnerEmail));
    }
}
